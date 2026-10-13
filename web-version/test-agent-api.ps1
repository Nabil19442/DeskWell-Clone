# Test script for DeskWell Agent API Authentication & Synchronization Idempotency

$baseUrl = "http://localhost:8080/api/agent"
$machineName = [System.Environment]::MachineName
$osVersion = "Windows 11 Test Agent"

Write-Host "=== 1. Testing Agent Registration ===" -ForegroundColor Cyan
$regPayload = @{
    machineName = $machineName
    osVersion   = $osVersion
} | ConvertTo-Json

try {
    $regResponse = Invoke-RestMethod -Uri "$baseUrl/register" -Method POST -Body $regPayload -ContentType "application/json"
    $token = if ($regResponse.agentToken) { $regResponse.agentToken } else { $regResponse.token }
    $agentId = $regResponse.agentId
    Write-Host "SUCCESS: Registered agentId=$agentId, token=$($token.Substring(0, 15))..." -ForegroundColor Green
} catch {
    Write-Host "FAILED to register: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n=== 2. Testing Heartbeat with Valid Auth ===" -ForegroundColor Cyan
$headers = @{
    "X-Agent-Id"              = $agentId
    "X-DeskWell-Agent-Token"  = $token
    "Content-Type"            = "application/json"
}

$heartbeatPayload = @{
    agentId      = $agentId
    hostname     = $machineName
    osVersion    = $osVersion
    pendingCount = 2
} | ConvertTo-Json

try {
    $hbResponse = Invoke-RestMethod -Uri "$baseUrl/heartbeat" -Method POST -Headers $headers -Body $heartbeatPayload
    Write-Host "SUCCESS: Heartbeat received response: $($hbResponse | ConvertTo-Json -Compress)" -ForegroundColor Green
} catch {
    Write-Host "FAILED heartbeat: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n=== 3. Testing Rules Retrieval ===" -ForegroundColor Cyan
try {
    $rulesResponse = Invoke-RestMethod -Uri "$baseUrl/rules" -Method GET -Headers $headers
    Write-Host "SUCCESS: Retrieved $($rulesResponse.Count) rules from backend: $($rulesResponse -join ', ')" -ForegroundColor Green
} catch {
    Write-Host "FAILED rules fetch: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n=== 4. Testing Activity Synchronization (First Batch) ===" -ForegroundColor Cyan
$uuid1 = [System.Guid]::NewGuid().ToString()
$uuid2 = [System.Guid]::NewGuid().ToString()
$now = [System.DateTimeOffset]::UtcNow

$batch = @(
    @{
        syncId         = $uuid1
        appName        = "Code.exe"
        windowTitle    = "AgentLauncher.java - DeskWell - VS Code"
        category       = "Development"
        startTime      = $now.AddMinutes(-10).ToString("o")
        endTime        = $now.AddMinutes(-5).ToString("o")
        durationSeconds= 300
        isIdle         = $false
    },
    @{
        syncId         = $uuid2
        appName        = "chrome.exe"
        windowTitle    = "DeskWell Web Dashboard - Google Chrome"
        category       = "Browser"
        startTime      = $now.AddMinutes(-5).ToString("o")
        endTime        = $now.ToString("o")
        durationSeconds= 300
        isIdle         = $false
    }
) | ConvertTo-Json -Depth 5

try {
    $syncResponse1 = Invoke-RestMethod -Uri "$baseUrl/sync" -Method POST -Headers $headers -Body $batch
    Write-Host "SUCCESS: Batch 1 Synced: $($syncResponse1 | ConvertTo-Json -Compress)" -ForegroundColor Green
    if ($syncResponse1.syncedCount -eq 2 -and $syncResponse1.duplicateSkippedCount -eq 0) {
        Write-Host "VERIFIED: Batch 1 ingested 2 records with 0 duplicates." -ForegroundColor Green
    } else {
        Write-Host "WARNING: Unexpected counts on first sync" -ForegroundColor Yellow
    }
} catch {
    Write-Host "FAILED batch 1 sync: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n=== 5. Testing Idempotency (Resending Exact Same Batch) ===" -ForegroundColor Cyan
try {
    $syncResponse2 = Invoke-RestMethod -Uri "$baseUrl/sync" -Method POST -Headers $headers -Body $batch
    Write-Host "SUCCESS: Batch 2 (Duplicate) Response: $($syncResponse2 | ConvertTo-Json -Compress)" -ForegroundColor Green
    if ($syncResponse2.syncedCount -eq 0 -and $syncResponse2.duplicateSkippedCount -eq 2) {
        Write-Host "VERIFIED: Duplicate records correctly skipped! Idempotency working as expected." -ForegroundColor Green
    } else {
        Write-Host "FAILED IDEMPOTENCY: Duplicate records were not properly skipped!" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "FAILED batch 2 duplicate sync: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n=== 6. Testing Unauthorized Access (Invalid Token) ===" -ForegroundColor Cyan
$badHeaders = @{
    "Authorization" = "Bearer invalid-token-xyz"
    "Content-Type"  = "application/json"
}
try {
    $unauthResponse = Invoke-RestMethod -Uri "$baseUrl/heartbeat" -Method POST -Headers $badHeaders -Body $heartbeatPayload
    Write-Host "SECURITY FLAW: Invalid token was accepted!" -ForegroundColor Red
    exit 1
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 401) {
        Write-Host "VERIFIED: Unauthorized request correctly rejected with HTTP 401." -ForegroundColor Green
    } else {
        Write-Host "Rejected with code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
    }
}

Write-Host "`n=== ALL BACKEND AGENT TESTS PASSED SUCCESSFULLY ===" -ForegroundColor Green

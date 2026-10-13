package com.deskwell.agent.sync;

import com.deskwell.agent.blocker.WindowsAppBlocker;
import com.deskwell.agent.config.AgentConfig;
import com.deskwell.agent.model.ActivitySlice;
import com.deskwell.agent.storage.LocalQueueStorage;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SyncService {
    private static final Logger logger = Logger.getLogger(SyncService.class.getName());

    private final AgentConfig config;
    private final LocalQueueStorage storage;
    private final WindowsAppBlocker appBlocker;
    private final HttpClient httpClient;
    private final Gson gson;
    private final ScheduledExecutorService scheduler;

    private volatile boolean isRunning = false;
    private volatile boolean isConnected = false;
    private volatile long lastSuccessfulSyncTime = 0;
    private int backoffSeconds = 2;
    private static final int MAX_BACKOFF_SECONDS = 60;

    public SyncService(AgentConfig config, LocalQueueStorage storage, WindowsAppBlocker appBlocker) {
        this.config = config;
        this.storage = storage;
        this.appBlocker = appBlocker;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
        this.scheduler = Executors.newScheduledThreadPool(2);
    }

    public synchronized void start() {
        if (isRunning) return;
        isRunning = true;

        // Sync task
        scheduler.scheduleWithFixedDelay(this::syncCycle, 1, config.getSyncIntervalSeconds(), TimeUnit.SECONDS);

        // Heartbeat task (every 30 seconds)
        scheduler.scheduleAtFixedRate(this::sendHeartbeat, 2, 30, TimeUnit.SECONDS);

        logger.info("SyncService started against server: " + config.getServerUrl());
    }

    public synchronized void stop() {
        if (!isRunning) return;
        isRunning = false;
        scheduler.shutdown();
        logger.info("SyncService stopped");
    }

    private void syncCycle() {
        try {
            int pendingCount = storage.getPendingCount();
            if (pendingCount == 0) {
                // Nothing to sync, reset backoff
                backoffSeconds = 2;
                return;
            }

            List<ActivitySlice> batch = storage.getPendingBatch(config.getSyncBatchSize());
            if (batch.isEmpty()) return;

            logger.info("Attempting to sync batch of " + batch.size() + " records (pending total: " + pendingCount + ")...");
            boolean success = uploadBatch(batch);

            if (success) {
                List<String> syncIds = new ArrayList<>();
                for (ActivitySlice slice : batch) {
                    syncIds.add(slice.getSyncId());
                }
                storage.markBatchSynced(syncIds);
                isConnected = true;
                lastSuccessfulSyncTime = System.currentTimeMillis();
                backoffSeconds = 2; // Reset backoff on success
                logger.info("Successfully synced and pruned " + syncIds.size() + " activity records.");
            } else {
                handleSyncFailure("Server rejected or failed batch upload");
            }
        } catch (Exception e) {
            handleSyncFailure(e.getMessage());
        }
    }

    private boolean uploadBatch(List<ActivitySlice> batch) {
        try {
            String jsonPayload = gson.toJson(batch);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getServerUrl() + "/agent/sync"))
                    .header("Content-Type", "application/json")
                    .header("X-Agent-Id", config.getAgentId())
                    .header("X-DeskWell-Agent-Token", config.getAgentToken())
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return true;
            } else {
                logger.warning("Sync failed with HTTP " + response.statusCode() + ": " + response.body());
                return false;
            }
        } catch (Exception e) {
            logger.warning("Network error uploading activity batch: " + e.getMessage());
            return false;
        }
    }

    public void sendHeartbeat() {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("agentId", config.getAgentId());
            body.addProperty("hostname", config.getHostname());
            body.addProperty("osVersion", config.getOsVersion());
            body.addProperty("pendingCount", storage.getPendingCount());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getServerUrl() + "/agent/heartbeat"))
                    .header("Content-Type", "application/json")
                    .header("X-Agent-Id", config.getAgentId())
                    .header("X-DeskWell-Agent-Token", config.getAgentToken())
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                isConnected = true;
                logger.fine("Heartbeat acknowledged by server");
                fetchBlockingRules();
            } else {
                isConnected = false;
                logger.warning("Heartbeat failed (HTTP " + response.statusCode() + ")");
            }
        } catch (Exception e) {
            isConnected = false;
            logger.fine("Heartbeat unreachable: " + e.getMessage());
        }
    }

    private void fetchBlockingRules() {
        if (appBlocker == null) return;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getServerUrl() + "/agent/rules"))
                    .header("X-Agent-Id", config.getAgentId())
                    .header("X-DeskWell-Agent-Token", config.getAgentToken())
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonArray arr = JsonParser.parseString(response.body()).getAsJsonArray();
                Set<String> ruleNames = new HashSet<>();
                arr.forEach(elem -> ruleNames.add(elem.getAsString()));
                appBlocker.updateRules(ruleNames);
            }
        } catch (Exception e) {
            logger.fine("Failed to fetch blocking rules: " + e.getMessage());
        }
    }

    private void handleSyncFailure(String reason) {
        isConnected = false;
        logger.warning("[OFFLINE QUEUE ACTIVE] " + reason + " - records safely retained in SQLite. Backing off for " + backoffSeconds + "s");
        try {
            Thread.sleep(backoffSeconds * 1000L);
        } catch (InterruptedException ignored) {}
        backoffSeconds = Math.min(MAX_BACKOFF_SECONDS, backoffSeconds * 2);
    }

    public boolean isConnected() { return isConnected; }
    public long getLastSuccessfulSyncTime() { return lastSuccessfulSyncTime; }
}

# DeskWell — Modern Web Application

A modern, responsive full-stack digital wellbeing and productivity dashboard transformed from the JavaFX desktop application.

---

## 🏗️ Architecture Overview

```
web-version/
├── frontend/               # React 18, Vite, Tailwind CSS, Lucide React, Recharts
│   ├── src/
│   │   ├── api/            # Centralized API Client (Axios)
│   │   ├── components/     # Metric cards, Modals, Sidebar, Header, Layout
│   │   ├── context/        # ThemeContext (Light & Dark modes)
│   │   └── pages/          # Dashboard, Reports, Focus Mode, Goals, Categories, App Blocker, Settings
├── backend/                # Spring Boot 3.2, Spring Data JPA, PostgreSQL / H2, Apache PDFBox
│   ├── src/main/java/com/deskwell/web/
│   │   ├── config/         # WebMvc & CORS configuration
│   │   ├── controller/     # REST Controllers
│   │   ├── dto/            # Data Transfer Objects & Validation
│   │   ├── model/          # JPA Entities
│   │   ├── repository/     # Spring Data JPA Repositories
│   │   └── service/        # Business Logic, Server-authoritative Timers, Exporters
│   └── src/main/resources/
│       ├── application.yml
│       └── db/migration/   # V1__initial_schema.sql (PostgreSQL DDL)
└── README.md
```

---

## 🛠️ Prerequisites

- **Java**: OpenJDK 21 or higher (`java -version`)
- **Maven**: 3.9+ (`mvn -version`)
- **Node.js**: v18+ (tested on Node v24) (`node -v`)
- **npm**: v9+ (`npm -v`)
- **Database**: PostgreSQL 14+ (or zero-config H2 file mode automatically used in development)

---

## 🚀 Quick Start Guide (Windows)

### Terminal 1: Backend (Spring Boot)

```powershell
cd d:\Professional\java\deskwell\web-version\backend

# Compile and run with development profile (zero-config, persists to ./data/deskwell_web)
mvn spring-boot:run

# Or run with PostgreSQL production profile:
# Set environment variables (see below) and run:
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

Backend will be available at: `http://localhost:8080`

### Terminal 2: Frontend (React + Vite)

```powershell
cd d:\Professional\java\deskwell\web-version\frontend

# Install dependencies
npm install

# Start Vite dev server
npm run dev
```

Frontend will be available at: `http://localhost:5173`

---

### Terminal 3: Windows Tracking Agent

```powershell
cd d:\Professional\java\deskwell\web-version\windows-agent

# Build standalone shaded JAR (bundles JNA, SQLite JDBC, and Gson)
mvn clean package

# Launch the tracking agent
java -jar target/deskwell-windows-agent.jar
```

---

## 🔒 Agent Authentication Lifecycle

1. **Registration**:
   On initial startup or manual provisioning, the agent sends `POST /api/agent/register` with its machine hostname and OS. The backend generates a persistent UUID `agentId` and per-installation `agentToken`.

2. **Authentication**:
   Every agent sync and heartbeat request is authenticated via headers:
   - `X-Agent-Id: <agent-id>`
   - `X-DeskWell-Agent-Token: <agent-token>` (or `Authorization: Bearer <agent-token>`)
   Unauthorized or invalid tokens are rejected with `HTTP 401 Unauthorized`.

3. **Revocation**:
   An administrator can revoke an agent by setting `is_active = FALSE` in `agent_installations` or deleting the record. Subsequent agent sync attempts will be rejected.

4. **Reconfiguration**:
   Agent settings are saved in `agent.properties` in the agent working directory:
   ```properties
   server.url=http://localhost:8080/api
   agent.id=0cca1b86-fd7b-4c2d-b6a5-2ad375f81cad
   agent.token=dw_token_...
   tracker.polling.interval=3
   tracker.idle.threshold=60
   sync.batch.size=50
   sync.interval=10
   ```
   To connect to a new server or use a new token, edit `agent.properties` and restart the agent.

---

## 🛡️ Application Blocking: Configured vs. Enforced Rules

- **Configured Rules**: Rules created by users in the Web Dashboard are persisted in the PostgreSQL/database table `blocked_apps`.
- **Enforcement Mechanism**:
  - Web browsers are sandboxed and cannot kill external OS processes.
  - The Windows Tracking Agent polls `GET /api/agent/rules` during heartbeats.
  - When a blocked application enters the Windows foreground, the agent enforces the rule by minimizing the window (`SW_MINIMIZE`) using native User32 Windows APIs.

---

## 💾 Database Configuration & PostgreSQL Migration

### Development Profile (Default)
Runs with an embedded file-backed H2 engine at `./data/deskwell_web.mv.db`, requiring zero external setup.

### Production Profile (PostgreSQL)
1. Create PostgreSQL database:
```sql
CREATE DATABASE deskwell;
CREATE USER deskwell_user WITH ENCRYPTED PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE deskwell TO deskwell_user;
```

2. Run backend with PostgreSQL credentials:
```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:DB_HOST="localhost"
$env:DB_PORT="5432"
$env:DB_NAME="deskwell"
$env:DB_USERNAME="deskwell_user"
$env:DB_PASSWORD="your_secure_password"

cd d:\Professional\java\deskwell\web-version\backend
mvn spring-boot:run
```
Flyway migration script `src/main/resources/db/migration/V1__initial_schema.sql` automatically applies table schemas, indexes, and unique constraints (`sync_id`).

---

## 🧪 Automated Testing

### 1. Test Windows Tracking Agent
```powershell
cd d:\Professional\java\deskwell\web-version\windows-agent
mvn test
```
Tests SQLite offline queue persistence, batch pruning, restart recovery, native foreground detection, JNA idle time calculations, and blocker rules.

### 2. Test Spring Boot Backend
```powershell
cd d:\Professional\java\deskwell\web-version\backend
mvn test
```
Tests agent registration, authentication, batch ingestion, sync idempotency (duplicate rejection), and daily statistics aggregation.

### 3. Test End-to-End Agent API Flow
```powershell
powershell -ExecutionPolicy Bypass -File d:\Professional\java\deskwell\web-version\test-agent-api.ps1
```
Runs registration, heartbeat, rules retrieval, batch sync, duplicate replay, and 401 unauthorized rejection checks against a live backend.


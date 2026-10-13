-- DeskWell PostgreSQL Initial Schema Migration

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    color VARCHAR(30) NOT NULL DEFAULT '#607D8B',
    is_default BOOLEAN NOT NULL DEFAULT TRUE,
    icon VARCHAR(100),
    productivity_weight DOUBLE PRECISION NOT NULL DEFAULT 0.0
);

CREATE TABLE IF NOT EXISTS applications (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    executable_path VARCHAR(1024),
    category_id BIGINT REFERENCES categories(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    start_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_time TIMESTAMP WITHOUT TIME ZONE,
    duration_seconds BIGINT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS usage_tracking (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT REFERENCES sessions(id) ON DELETE SET NULL,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    window_title VARCHAR(1024),
    start_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_time TIMESTAMP WITHOUT TIME ZONE,
    duration_seconds BIGINT DEFAULT 0,
    sync_id VARCHAR(64) UNIQUE
);

CREATE TABLE IF NOT EXISTS daily_stats (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL UNIQUE,
    total_screen_time BIGINT DEFAULT 0,
    productive_time BIGINT DEFAULT 0,
    idle_time BIGINT DEFAULT 0,
    session_count INTEGER DEFAULT 0,
    productivity_score DOUBLE PRECISION DEFAULT 0.0
);

CREATE TABLE IF NOT EXISTS goals (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    target_value INTEGER NOT NULL,
    current_value INTEGER DEFAULT 0,
    start_date DATE NOT NULL,
    end_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS focus_sessions (
    id BIGSERIAL PRIMARY KEY,
    start_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_time TIMESTAMP WITHOUT TIME ZONE,
    duration_minutes INTEGER NOT NULL,
    break_minutes INTEGER DEFAULT 5,
    pomodoro_mode BOOLEAN DEFAULT TRUE,
    completed BOOLEAN DEFAULT FALSE,
    interruptions INTEGER DEFAULT 0,
    state VARCHAR(30) NOT NULL DEFAULT 'IDLE',
    target_end_epoch_millis BIGINT,
    remaining_seconds INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS settings (
    setting_key VARCHAR(100) PRIMARY KEY,
    setting_value TEXT NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS blocked_apps (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    is_permanent BOOLEAN DEFAULT FALSE,
    notes VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS agent_installations (
    id BIGSERIAL PRIMARY KEY,
    agent_id VARCHAR(100) NOT NULL UNIQUE,
    agent_token VARCHAR(255) NOT NULL,
    hostname VARCHAR(255),
    os_version VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    registered_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_heartbeat_at TIMESTAMP WITHOUT TIME ZONE,
    last_sync_at TIMESTAMP WITHOUT TIME ZONE
);

-- Seed Initial Default Categories
INSERT INTO categories (name, color, is_default, productivity_weight) VALUES
    ('Productivity', '#4CAF50', TRUE, 0.0),
    ('Development', '#2196F3', TRUE, 1.0),
    ('Browser', '#FF9800', TRUE, 0.0),
    ('Entertainment', '#F44336', TRUE, 0.0),
    ('Communication', '#9C27B0', TRUE, 0.0),
    ('Gaming', '#795548', TRUE, 0.0),
    ('Other', '#607D8B', TRUE, 0.0)
ON CONFLICT (name) DO NOTHING;

package com.deskwell.agent.storage;

import com.deskwell.agent.model.ActivitySlice;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class LocalQueueStorage {
    private static final Logger logger = Logger.getLogger(LocalQueueStorage.class.getName());
    private static final String DB_FILE_NAME = "agent_queue.db";
    private final String dbUrl;

    public LocalQueueStorage() {
        this(DB_FILE_NAME);
    }

    public LocalQueueStorage(String dbPath) {
        File dbFile = new File(dbPath).getAbsoluteFile();
        this.dbUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        initDatabase();
    }

    public void close() {
        // SQLite file connection is closed per query in try-with-resources
    }

    private synchronized Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    private void initDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS pending_records (
                        sync_id TEXT PRIMARY KEY,
                        process_name TEXT NOT NULL,
                        window_title TEXT,
                        executable_path TEXT,
                        start_time TEXT NOT NULL,
                        end_time TEXT,
                        duration_seconds INTEGER NOT NULL,
                        is_idle INTEGER NOT NULL,
                        created_at INTEGER NOT NULL,
                        status TEXT NOT NULL DEFAULT 'PENDING'
                    )
                """);
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_pending_status ON pending_records(status)");
            }
            logger.info("Local SQLite offline queue initialized at: " + dbUrl);
        } catch (Exception e) {
            logger.severe("Failed to initialize local queue storage: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public synchronized void enqueue(ActivitySlice slice) {
        String sql = """
            INSERT OR REPLACE INTO pending_records 
            (sync_id, process_name, window_title, executable_path, start_time, end_time, duration_seconds, is_idle, created_at, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING')
        """;

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, slice.getSyncId());
            pstmt.setString(2, slice.getProcessName());
            pstmt.setString(3, slice.getWindowTitle());
            pstmt.setString(4, slice.getExecutablePath());
            pstmt.setString(5, slice.getStartTime());
            pstmt.setString(6, slice.getEndTime());
            pstmt.setLong(7, slice.getDurationSeconds());
            pstmt.setInt(8, slice.isIdle() ? 1 : 0);
            pstmt.setLong(9, slice.getCreatedAt());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Failed to enqueue activity record to local storage: " + e.getMessage());
        }
    }

    public synchronized List<ActivitySlice> getPendingBatch(int limit) {
        List<ActivitySlice> list = new ArrayList<>();
        String sql = "SELECT * FROM pending_records WHERE status = 'PENDING' ORDER BY created_at ASC LIMIT ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ActivitySlice slice = new ActivitySlice();
                slice.setSyncId(rs.getString("sync_id"));
                slice.setProcessName(rs.getString("process_name"));
                slice.setWindowTitle(rs.getString("window_title"));
                slice.setExecutablePath(rs.getString("executable_path"));
                slice.setStartTime(rs.getString("start_time"));
                slice.setEndTime(rs.getString("end_time"));
                slice.setDurationSeconds(rs.getLong("duration_seconds"));
                slice.setIdle(rs.getInt("is_idle") == 1);
                slice.setCreatedAt(rs.getLong("created_at"));
                list.add(slice);
            }
        } catch (SQLException e) {
            logger.warning("Failed to fetch pending batch: " + e.getMessage());
        }
        return list;
    }

    public synchronized void markBatchSynced(List<String> syncIds) {
        if (syncIds == null || syncIds.isEmpty()) return;

        String sql = "DELETE FROM pending_records WHERE sync_id = ?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (String id : syncIds) {
                    pstmt.setString(1, id);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            logger.warning("Failed to prune synced batch: " + e.getMessage());
        }
    }

    public synchronized int getPendingCount() {
        String sql = "SELECT COUNT(*) FROM pending_records WHERE status = 'PENDING'";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.warning("Failed to get pending count: " + e.getMessage());
        }
        return 0;
    }
}

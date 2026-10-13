package com.deskwell.agent.storage;

import com.deskwell.agent.model.ActivitySlice;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LocalQueueStorageTest {

    private Path tempDbPath;
    private LocalQueueStorage storage;

    @BeforeEach
    public void setup() throws IOException {
        tempDbPath = Files.createTempFile("test_agent_queue_", ".db");
        storage = new LocalQueueStorage(tempDbPath.toString());
    }

    @AfterEach
    public void cleanup() {
        if (storage != null) {
            storage.close();
        }
        try {
            Files.deleteIfExists(tempDbPath);
        } catch (IOException ignored) {}
    }

    @Test
    public void testEnqueueAndGetPendingBatch() {
        assertEquals(0, storage.getPendingCount());

        ActivitySlice slice1 = new ActivitySlice("notepad.exe", "test.txt - Notepad", "C:\\notepad.exe",
                "2026-10-13T02:00:00", "2026-10-13T02:05:00", 300, false);
        ActivitySlice slice2 = new ActivitySlice("Code.exe", "Agent.java - VS Code", "C:\\Code.exe",
                "2026-10-13T02:05:00", "2026-10-13T02:10:00", 300, false);

        storage.enqueue(slice1);
        storage.enqueue(slice2);

        assertEquals(2, storage.getPendingCount());

        List<ActivitySlice> batch = storage.getPendingBatch(10);
        assertEquals(2, batch.size());
        assertEquals("notepad.exe", batch.get(0).getProcessName());
        assertEquals("Code.exe", batch.get(1).getProcessName());
        assertNotNull(batch.get(0).getSyncId());
        assertNotNull(batch.get(1).getSyncId());
    }

    @Test
    public void testMarkBatchSyncedPrunesQueue() {
        ActivitySlice slice1 = new ActivitySlice("notepad.exe", "Notes", "", "2026-10-13T02:00:00", "2026-10-13T02:01:00", 60, false);
        ActivitySlice slice2 = new ActivitySlice("chrome.exe", "DeskWell", "", "2026-10-13T02:01:00", "2026-10-13T02:02:00", 60, false);

        storage.enqueue(slice1);
        storage.enqueue(slice2);

        assertEquals(2, storage.getPendingCount());

        storage.markBatchSynced(List.of(slice1.getSyncId()));

        assertEquals(1, storage.getPendingCount());
        List<ActivitySlice> remaining = storage.getPendingBatch(10);
        assertEquals(1, remaining.size());
        assertEquals(slice2.getSyncId(), remaining.get(0).getSyncId());
    }

    @Test
    public void testAgentRestartRecovery() {
        ActivitySlice slice = new ActivitySlice("idea64.exe", "Project", "", "2026-10-13T02:00:00", "2026-10-13T02:10:00", 600, false);
        storage.enqueue(slice);
        assertEquals(1, storage.getPendingCount());

        // Simulate agent shutdown
        storage.close();

        // Simulate agent restart opening the same SQLite queue
        LocalQueueStorage recoveredStorage = new LocalQueueStorage(tempDbPath.toString());
        assertEquals(1, recoveredStorage.getPendingCount());

        List<ActivitySlice> batch = recoveredStorage.getPendingBatch(10);
        assertEquals(1, batch.size());
        assertEquals(slice.getSyncId(), batch.get(0).getSyncId());
        assertEquals("idea64.exe", batch.get(0).getProcessName());

        recoveredStorage.close();
    }
}

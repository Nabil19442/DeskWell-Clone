package com.deskwell.agent.tracker;

import com.deskwell.agent.blocker.WindowsAppBlocker;
import com.deskwell.agent.config.AgentConfig;
import com.deskwell.agent.model.ActivitySlice;
import com.deskwell.agent.storage.LocalQueueStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class WindowsTrackingTest {

    private Path tempDbPath;
    private LocalQueueStorage storage;
    private AgentConfig config;
    private WindowsAppBlocker blocker;
    private WindowsActivityTracker tracker;

    @BeforeEach
    public void setup() throws IOException {
        tempDbPath = Files.createTempFile("test_tracker_", ".db");
        storage = new LocalQueueStorage(tempDbPath.toString());
        config = new AgentConfig();
        blocker = new WindowsAppBlocker();
        tracker = new WindowsActivityTracker(config, storage, blocker);
    }

    @AfterEach
    public void teardown() {
        if (tracker != null) tracker.stop();
        if (storage != null) storage.close();
        try {
            Files.deleteIfExists(tempDbPath);
        } catch (IOException ignored) {}
    }

    @Test
    public void testActivitySliceAttributes() {
        ActivitySlice slice = new ActivitySlice("chrome.exe", "Google - Chrome", "C:\\chrome.exe",
                "2026-10-13T01:00:00", "2026-10-13T01:05:00", 300, false);

        assertNotNull(slice.getSyncId());
        assertEquals("chrome.exe", slice.getProcessName());
        assertEquals("Google - Chrome", slice.getWindowTitle());
        assertEquals(300, slice.getDurationSeconds());
        assertFalse(slice.isIdle());
    }

    @Test
    public void testAppBlockerRuleMatching() {
        blocker.updateRules(Set.of("Steam.exe", "discord.exe", "game.exe"));

        assertTrue(blocker.isBlocked("steam.exe"));
        assertTrue(blocker.isBlocked("STEAM.EXE"));
        assertTrue(blocker.isBlocked("Discord.exe"));
        assertFalse(blocker.isBlocked("Code.exe"));
        assertFalse(blocker.isBlocked("notepad.exe"));
    }

    @Test
    public void testForegroundWindowDetection() {
        WindowsActivityTracker.ProcessInfo info = tracker.getActiveWindow();
        assertNotNull(info);
        assertNotNull(info.processName, "Process name should never be null on Windows");
        assertFalse(info.processName.isBlank(), "Process name should not be blank");
        System.out.println("Detected active foreground window: " + info.processName + " - " + info.windowTitle);
    }

    @Test
    public void testIdleTimeCalculation() {
        long idleSeconds = tracker.getIdleTimeSeconds();
        assertTrue(idleSeconds >= 0, "Idle seconds must be non-negative");
        System.out.println("Current user idle seconds: " + idleSeconds);
    }
}

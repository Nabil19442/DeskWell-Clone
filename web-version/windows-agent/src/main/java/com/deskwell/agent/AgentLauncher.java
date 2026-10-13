package com.deskwell.agent;

import com.deskwell.agent.blocker.WindowsAppBlocker;
import com.deskwell.agent.config.AgentConfig;
import com.deskwell.agent.storage.LocalQueueStorage;
import com.deskwell.agent.sync.SyncService;
import com.deskwell.agent.tracker.WindowsActivityTracker;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class AgentLauncher {
    private static final Logger logger = Logger.getLogger(AgentLauncher.class.getName());

    public static void main(String[] args) {
        setupLogging();

        System.out.println("=====================================================");
        System.out.println("       DeskWell Windows Tracking Agent v1.0.0        ");
        System.out.println("=====================================================");

        AgentConfig config = new AgentConfig();

        // Parse optional CLI overrides
        for (int i = 0; i < args.length; i++) {
            if ("--url".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                config.setServerUrl(args[++i]);
            } else if ("--token".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                config.setAgentToken(args[++i]);
            }
        }
        config.save();

        System.out.println("Hostname    : " + config.getHostname());
        System.out.println("OS          : " + config.getOsVersion());
        System.out.println("Agent ID    : " + config.getAgentId());
        System.out.println("Server URL  : " + config.getServerUrl());
        System.out.println("Poll Rate   : " + config.getPollingIntervalSeconds() + "s");
        System.out.println("Idle Thresh : " + config.getIdleThresholdSeconds() + "s");
        System.out.println("=====================================================");

        LocalQueueStorage storage = new LocalQueueStorage();
        int pending = storage.getPendingCount();
        System.out.println("Offline Queue: " + pending + " records pending synchronization");

        WindowsAppBlocker appBlocker = new WindowsAppBlocker();
        WindowsActivityTracker tracker = new WindowsActivityTracker(config, storage, appBlocker);
        SyncService syncService = new SyncService(config, storage, appBlocker);

        // Register clean shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down DeskWell Windows Tracking Agent...");
            tracker.stop();
            syncService.stop();
            System.out.println("All active activity records saved to local queue.");
        }));

        // Start services
        tracker.start();
        syncService.start();

        System.out.println("\n[STATUS: RUNNING] Monitoring Windows active windows & syncing to server.");
        System.out.println("Press Ctrl+C to terminate the agent.\n");

        // Keep main thread alive
        try {
            Thread.currentThread().join();
        } catch (InterruptedException ignored) {}
    }

    private static void setupLogging() {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.INFO);
        for (var handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleFormatter());
        rootLogger.addHandler(handler);
    }
}

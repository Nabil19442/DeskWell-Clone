package com.deskwell.agent.config;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

public class AgentConfig {
    private static final Logger logger = Logger.getLogger(AgentConfig.class.getName());
    private static final String CONFIG_FILE_NAME = "agent.properties";

    private String serverUrl = "http://localhost:8080/api";
    private String agentId;
    private String agentToken;
    private int pollingIntervalSeconds = 3;
    private int idleThresholdSeconds = 60;
    private int syncBatchSize = 50;
    private int syncIntervalSeconds = 10;
    private String hostname = "localhost";
    private String osVersion = "Windows";

    private final Path configPath;

    public AgentConfig() {
        this.configPath = Paths.get(CONFIG_FILE_NAME).toAbsolutePath();
        initHostDetails();
        loadOrCreate();
    }

    private void initHostDetails() {
        try {
            this.hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            this.hostname = "Windows-PC";
        }
        this.osVersion = System.getProperty("os.name") + " " + System.getProperty("os.version");
    }

    public synchronized void loadOrCreate() {
        Properties props = new Properties();
        if (Files.exists(configPath)) {
            try (InputStream in = Files.newInputStream(configPath)) {
                props.load(in);
                this.serverUrl = props.getProperty("server.url", this.serverUrl);
                this.agentId = props.getProperty("agent.id", UUID.randomUUID().toString());
                this.agentToken = props.getProperty("agent.token", "");
                this.pollingIntervalSeconds = Integer.parseInt(props.getProperty("tracker.polling.interval", String.valueOf(this.pollingIntervalSeconds)));
                this.idleThresholdSeconds = Integer.parseInt(props.getProperty("tracker.idle.threshold", String.valueOf(this.idleThresholdSeconds)));
                this.syncBatchSize = Integer.parseInt(props.getProperty("sync.batch.size", String.valueOf(this.syncBatchSize)));
                this.syncIntervalSeconds = Integer.parseInt(props.getProperty("sync.interval", String.valueOf(this.syncIntervalSeconds)));
            } catch (Exception e) {
                logger.warning("Failed to load agent.properties, using defaults: " + e.getMessage());
            }
        } else {
            this.agentId = UUID.randomUUID().toString();
            this.agentToken = "deskwell-agent-local-token"; // Default pairing token
            save();
        }
    }

    public synchronized void save() {
        Properties props = new Properties();
        props.setProperty("server.url", this.serverUrl);
        props.setProperty("agent.id", this.agentId != null ? this.agentId : UUID.randomUUID().toString());
        props.setProperty("agent.token", this.agentToken != null ? this.agentToken : "deskwell-agent-local-token");
        props.setProperty("tracker.polling.interval", String.valueOf(this.pollingIntervalSeconds));
        props.setProperty("tracker.idle.threshold", String.valueOf(this.idleThresholdSeconds));
        props.setProperty("sync.batch.size", String.valueOf(this.syncBatchSize));
        props.setProperty("sync.interval", String.valueOf(this.syncIntervalSeconds));

        try (OutputStream out = Files.newOutputStream(configPath)) {
            props.store(out, "DeskWell Windows Tracking Agent Configuration");
            logger.info("Saved agent configuration to: " + configPath);
        } catch (IOException e) {
            logger.severe("Failed to save agent.properties: " + e.getMessage());
        }
    }

    public String getServerUrl() { return serverUrl; }
    public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public String getAgentToken() { return agentToken; }
    public void setAgentToken(String agentToken) { this.agentToken = agentToken; }

    public int getPollingIntervalSeconds() { return pollingIntervalSeconds; }
    public int getIdleThresholdSeconds() { return idleThresholdSeconds; }
    public int getSyncBatchSize() { return syncBatchSize; }
    public int getSyncIntervalSeconds() { return syncIntervalSeconds; }
    public String getHostname() { return hostname; }
    public String getOsVersion() { return osVersion; }
}

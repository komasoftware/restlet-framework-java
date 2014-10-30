package org.restlet.ext.apispark;

/**
 * @author Manuel Boillod
 */
public class ConnectorAgentConfig {

    public static final String AGENT_VERSION = "1.0";

    public static final String DEFAULT_AGENT_SERVICE_PATH = "https://apispark.restlet.com/agent/";

    private Integer cellId;
    private Integer cellVersion;
    private String agentServicePath = DEFAULT_AGENT_SERVICE_PATH;
    private String agentUsername;
    private char[] agentSecretKey;


    public Integer getCellId() {
        return cellId;
    }

    public ConnectorAgentConfig setCellId(Integer cellId) {
        this.cellId = cellId;
        return this;
    }

    public Integer getCellVersion() {
        return cellVersion;
    }

    public ConnectorAgentConfig setCellVersion(Integer cellVersion) {
        this.cellVersion = cellVersion;
        return this;
    }

    public String getAgentServicePath() {
        return agentServicePath;
    }

    public ConnectorAgentConfig setAgentServicePath(String agentServicePath) {
        this.agentServicePath = agentServicePath;
        return this;
    }

    public String getAgentUsername() {
        return agentUsername;
    }

    public ConnectorAgentConfig setAgentUsername(String agentUsername) {
        this.agentUsername = agentUsername;
        return this;
    }

    public String getAgentSecretKey() {
        return new String(agentSecretKey);
    }

    public ConnectorAgentConfig setAgentSecretKey(String agentSecretKey) {
        this.agentSecretKey = agentSecretKey.toCharArray();
        return this;
    }

    public void validate() {
        if (cellId == null) {
            throw new IllegalArgumentException("Cell id is required");
        }
        if (cellVersion == null) {
            throw new IllegalArgumentException("Cell version is required");
        }
        if (agentServicePath == null) {
            throw new IllegalArgumentException("Agent service path is required");
        }
        if (agentUsername == null) {
            throw new IllegalArgumentException("Agent username is required");
        }
        if (agentSecretKey == null) {
            throw new IllegalArgumentException("Agent secret key is required");
        }
    }


}

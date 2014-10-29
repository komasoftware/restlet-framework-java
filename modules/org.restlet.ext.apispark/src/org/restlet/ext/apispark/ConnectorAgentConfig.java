package org.restlet.ext.apispark;

/**
 * @author Manuel Boillod
 */
public class ConnectorAgentConfig {

    public static final String AGENT_VERSION = "1.0";

    private Integer apisparkCellId;
    private Integer apisparkCellVersion;
    private String apisparkUsername;
    private char[] apisparkSecretkey;

    public Integer getApisparkCellId() {
        return apisparkCellId;
    }

    public Integer getApisparkCellVersion() {
        return apisparkCellVersion;
    }

    public String getApisparkUsername() {
        return apisparkUsername;
    }

    public String getApisparkSecretkey() {
        return new String(apisparkSecretkey);
    }

    public ConnectorAgentConfig setApisparkCellId(Integer apisparkCellId) {
        this.apisparkCellId = apisparkCellId;
        return this;
    }

    public ConnectorAgentConfig setApisparkCellVersion(Integer apisparkCellVersion) {
        this.apisparkCellVersion = apisparkCellVersion;
        return this;
    }

    public ConnectorAgentConfig setApisparkUsername(String apisparkUsername) {
        this.apisparkUsername = apisparkUsername;
        return this;
    }

    public ConnectorAgentConfig setApisparkSecretkey(String apisparkSecretkey) {
        this.apisparkSecretkey = apisparkSecretkey.toCharArray();
        return this;
    }

    public void validate() {
        if (apisparkCellId == null) {
            throw new IllegalArgumentException("Apispark cell id is required");
        }
        if (apisparkCellVersion == null) {
            throw new IllegalArgumentException("Apispark cell version is required");
        }
        if (apisparkUsername == null) {
            throw new IllegalArgumentException("Apispark username is required");
        }
        if (apisparkSecretkey == null) {
            throw new IllegalArgumentException("Apispark secret key is required");
        }
    }


}

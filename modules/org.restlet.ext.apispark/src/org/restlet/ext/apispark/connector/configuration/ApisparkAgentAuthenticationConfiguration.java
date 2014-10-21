package org.restlet.ext.apispark.connector.configuration;


public class ApisparkAgentAuthenticationConfiguration {

    private boolean enabled;

    private int cacheRefreshRate;

    private int cacheSize;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getCacheRefreshRate() {
        return cacheRefreshRate;
    }

    public void setCacheRefreshRate(int cacheRefreshRate) {
        this.cacheRefreshRate = cacheRefreshRate;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }
}

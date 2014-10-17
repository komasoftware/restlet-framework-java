package org.restlet.ext.apispark.connector.configuration;

public class RateLimitation {
    
    public static final int GLOBAL = 0;
    
    public static final int INDIVIDUAL = 1;

    private String name;

    private int type;

    private int period;

    private int limit;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}

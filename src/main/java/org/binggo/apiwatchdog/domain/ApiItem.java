package org.binggo.apiwatchdog.domain;

public class ApiItem {
    private Integer apiId;

    private String name;

    private String path;

    private String type;

    private String version;

    private Integer providerId;

    private Byte state;

    private Byte metricNot200;

    private Byte metric200Not0;

    private Byte metricResptimeThreshold;

    private Byte alarmType;

    private String description;

    public Integer getApiId() {
        return apiId;
    }

    public void setApiId(Integer apiId) {
        this.apiId = apiId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path == null ? null : path.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version == null ? null : version.trim();
    }

    public Integer getProviderId() {
        return providerId;
    }

    public void setProviderId(Integer providerId) {
        this.providerId = providerId;
    }

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }

    public Byte getMetricNot200() {
        return metricNot200;
    }

    public void setMetricNot200(Byte metricNot200) {
        this.metricNot200 = metricNot200;
    }

    public Byte getMetric200Not0() {
        return metric200Not0;
    }

    public void setMetric200Not0(Byte metric200Not0) {
        this.metric200Not0 = metric200Not0;
    }

    public Byte getMetricResptimeThreshold() {
        return metricResptimeThreshold;
    }

    public void setMetricResptimeThreshold(Byte metricResptimeThreshold) {
        this.metricResptimeThreshold = metricResptimeThreshold;
    }

    public Byte getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(Byte alarmType) {
        this.alarmType = alarmType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}
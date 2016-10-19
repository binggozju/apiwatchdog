package org.binggo.apiwatchdog.domain;

public class ApiProvider {
    private Integer providerId;

    private String name;

    private String version;

    private Byte state;

    private String weixinReceivers;

    private String mailReceivers;

    private String phoneReceivers;

    private String description;

    public Integer getProviderId() {
        return providerId;
    }

    public void setProviderId(Integer providerId) {
        this.providerId = providerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version == null ? null : version.trim();
    }

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }

    public String getWeixinReceivers() {
        return weixinReceivers;
    }

    public void setWeixinReceivers(String weixinReceivers) {
        this.weixinReceivers = weixinReceivers == null ? null : weixinReceivers.trim();
    }

    public String getMailReceivers() {
        return mailReceivers;
    }

    public void setMailReceivers(String mailReceivers) {
        this.mailReceivers = mailReceivers == null ? null : mailReceivers.trim();
    }

    public String getPhoneReceivers() {
        return phoneReceivers;
    }

    public void setPhoneReceivers(String phoneReceivers) {
        this.phoneReceivers = phoneReceivers == null ? null : phoneReceivers.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}
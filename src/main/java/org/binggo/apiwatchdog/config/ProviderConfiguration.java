package org.binggo.apiwatchdog.config;

public class ProviderConfiguration {
	
	private Integer providerId;
	
	private Byte state;
	
    private String weixinReceivers;

    private String mailReceivers;

    private String phoneReceivers;

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

	public String getWeixinReceivers() {
		return weixinReceivers;
	}

	public void setWeixinReceivers(String weixinReceivers) {
		this.weixinReceivers = weixinReceivers;
	}

	public String getMailReceivers() {
		return mailReceivers;
	}

	public void setMailReceivers(String mailReceivers) {
		this.mailReceivers = mailReceivers;
	}

	public String getPhoneReceivers() {
		return phoneReceivers;
	}

	public void setPhoneReceivers(String phoneReceivers) {
		this.phoneReceivers = phoneReceivers;
	}

}

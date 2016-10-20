package org.binggo.apiwatchdog.config;

public class ApiConfiguration {
	
	private Integer apiId;
	
	private Integer providerId;
	
	private Byte state;

    private Byte metricNot200;

    private Byte metric200Not0;

    private Byte metricResptimeThreshold;

    private Byte alarmType;

	public Integer getApiId() {
		return apiId;
	}

	public void setApiId(Integer apiId) {
		this.apiId = apiId;
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

}

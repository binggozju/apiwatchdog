package org.binggo.apiwatchdog.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ApiCall {
    private Integer apiId;
    
    private String callUuid;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date requestTime;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date responseTime;

    private String httpReponseCode;

    private String apiReturnCode;

    private String apiReturnMessage;

    private String sourceService;

    private String sourceHost;

    private String destService;

    private String destHost;

    private String requestBody;

    private String responseBody;

    public Integer getApiId() {
        return apiId;
    }

    public void setApiId(Integer apiId) {
        this.apiId = apiId;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }

    public Date getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Date responseTime) {
        this.responseTime = responseTime;
    }

    public String getHttpReponseCode() {
        return httpReponseCode;
    }

    public void setHttpReponseCode(String httpReponseCode) {
        this.httpReponseCode = httpReponseCode == null ? null : httpReponseCode.trim();
    }

    public String getApiReturnCode() {
        return apiReturnCode;
    }

    public void setApiReturnCode(String apiReturnCode) {
        this.apiReturnCode = apiReturnCode == null ? null : apiReturnCode.trim();
    }

    public String getApiReturnMessage() {
        return apiReturnMessage;
    }

    public void setApiReturnMessage(String apiReturnMessage) {
        this.apiReturnMessage = apiReturnMessage == null ? null : apiReturnMessage.trim();
    }

    public String getSourceService() {
        return sourceService;
    }

    public void setSourceService(String sourceService) {
        this.sourceService = sourceService == null ? null : sourceService.trim();
    }

    public String getSourceHost() {
        return sourceHost;
    }

    public void setSourceHost(String sourceHost) {
        this.sourceHost = sourceHost == null ? null : sourceHost.trim();
    }

    public String getDestService() {
        return destService;
    }

    public void setDestService(String destService) {
        this.destService = destService == null ? null : destService.trim();
    }

    public String getDestHost() {
        return destHost;
    }

    public void setDestHost(String destHost) {
        this.destHost = destHost == null ? null : destHost.trim();
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody == null ? null : requestBody.trim();
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody == null ? null : responseBody.trim();
    }

    public String getCallUuid() {
        return callUuid;
    }

    public void setCallUuid(String callUuid) {
        this.callUuid = callUuid;
    }
    
    @Override
    public String toString() {
    	return callUuid;
    }
}
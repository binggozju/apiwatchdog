package org.binggo.apiwatchdog.domain;

import java.util.Date;

public class ApiStatData {
    private Integer apiId;

    private Date startTime;

    private Integer countTotal;

    private Integer countTimeout;

    private Integer countNot200;

    private Integer count200Not0;

    private Integer resptimeTotal;

    private Integer resptime0s1s;

    private Integer resptime1s2s;

    private Integer resptime2s3s;

    private Integer resptime3s4s;

    private Integer resptime4s5s;

    private Integer resptime5s6s;

    private Integer resptime6s7s;

    private Integer resptime7s8s;

    private Integer resptime8s9s;

    private Integer resptime10s11s;

    private Integer resptime11s12s;

    private Integer resptime12sMax;
    
    public ApiStatData() {
    	countTotal = countTimeout = countNot200 = count200Not0 = resptimeTotal
    			= resptime0s1s = resptime1s2s = resptime2s3s = resptime3s4s
    			= resptime4s5s = resptime5s6s = resptime6s7s = resptime7s8s
    			= resptime8s9s = resptime10s11s = resptime11s12s = resptime12sMax
    			= 0;
    }

    public Integer getApiId() {
        return apiId;
    }

    public void setApiId(Integer apiId) {
        this.apiId = apiId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Integer getCountTotal() {
        return countTotal;
    }

    public void setCountTotal(Integer countTotal) {
        this.countTotal = countTotal;
    }

    public Integer getCountTimeout() {
        return countTimeout;
    }

    public void setCountTimeout(Integer countTimeout) {
        this.countTimeout = countTimeout;
    }

    public Integer getCountNot200() {
        return countNot200;
    }

    public void setCountNot200(Integer countNot200) {
        this.countNot200 = countNot200;
    }

    public Integer getCount200Not0() {
        return count200Not0;
    }

    public void setCount200Not0(Integer count200Not0) {
        this.count200Not0 = count200Not0;
    }

    public Integer getResptimeTotal() {
        return resptimeTotal;
    }

    public void setResptimeTotal(Integer resptimeTotal) {
        this.resptimeTotal = resptimeTotal;
    }

    public Integer getResptime0s1s() {
        return resptime0s1s;
    }

    public void setResptime0s1s(Integer resptime0s1s) {
        this.resptime0s1s = resptime0s1s;
    }

    public Integer getResptime1s2s() {
        return resptime1s2s;
    }

    public void setResptime1s2s(Integer resptime1s2s) {
        this.resptime1s2s = resptime1s2s;
    }

    public Integer getResptime2s3s() {
        return resptime2s3s;
    }

    public void setResptime2s3s(Integer resptime2s3s) {
        this.resptime2s3s = resptime2s3s;
    }

    public Integer getResptime3s4s() {
        return resptime3s4s;
    }

    public void setResptime3s4s(Integer resptime3s4s) {
        this.resptime3s4s = resptime3s4s;
    }

    public Integer getResptime4s5s() {
        return resptime4s5s;
    }

    public void setResptime4s5s(Integer resptime4s5s) {
        this.resptime4s5s = resptime4s5s;
    }

    public Integer getResptime5s6s() {
        return resptime5s6s;
    }

    public void setResptime5s6s(Integer resptime5s6s) {
        this.resptime5s6s = resptime5s6s;
    }

    public Integer getResptime6s7s() {
        return resptime6s7s;
    }

    public void setResptime6s7s(Integer resptime6s7s) {
        this.resptime6s7s = resptime6s7s;
    }

    public Integer getResptime7s8s() {
        return resptime7s8s;
    }

    public void setResptime7s8s(Integer resptime7s8s) {
        this.resptime7s8s = resptime7s8s;
    }

    public Integer getResptime8s9s() {
        return resptime8s9s;
    }

    public void setResptime8s9s(Integer resptime8s9s) {
        this.resptime8s9s = resptime8s9s;
    }

    public Integer getResptime10s11s() {
        return resptime10s11s;
    }

    public void setResptime10s11s(Integer resptime10s11s) {
        this.resptime10s11s = resptime10s11s;
    }

    public Integer getResptime11s12s() {
        return resptime11s12s;
    }

    public void setResptime11s12s(Integer resptime11s12s) {
        this.resptime11s12s = resptime11s12s;
    }

    public Integer getResptime12sMax() {
        return resptime12sMax;
    }

    public void setResptime12sMax(Integer resptime12sMax) {
        this.resptime12sMax = resptime12sMax;
    }
}
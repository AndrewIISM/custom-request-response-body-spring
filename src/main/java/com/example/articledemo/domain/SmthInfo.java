package com.example.articledemo.domain;

public class SmthInfo {

    private String additionalInfo;

    public SmthInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public SmthInfo() {
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @Override
    public String toString() {
        return "SmthInfo{" +
                "additionalInfo='" + additionalInfo + '\'' +
                '}';
    }
}

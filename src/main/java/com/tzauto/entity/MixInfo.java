package com.tzauto.entity;

public class MixInfo {
    /**
     * 批次
     */
    private String lot;
    /**
     * 层别
     */
    private String layer;
    /**
     * 设备编号
     */
    private String deviceCode;
    /**
     * 是否手动解混了
     */
    private Integer complete;

    public MixInfo(String lot, String layer, String deviceCode) {
        this.lot = lot;
        this.layer = layer;
        this.deviceCode = deviceCode;
        this.complete = 0;
    }

    public String getLot() {
        return lot;
    }

    public void setLot(String lot) {
        this.lot = lot;
    }

    public String getLayer() {
        return layer;
    }

    public void setLayer(String layer) {
        this.layer = layer;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public Integer getComplete() {
        return complete;
    }

    public void setComplete(Integer complete) {
        this.complete = complete;
    }
}

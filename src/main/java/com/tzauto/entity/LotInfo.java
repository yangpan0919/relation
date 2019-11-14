package com.tzauto.entity;

public class LotInfo {



    public LotInfo() {
    }

    private String lotid;

    private String starttime;

    private String endTime;//结束时间

    private String lotnum;

    private Integer complete;

    private String firstnum = "0";
    private String targetNum;
    private String paperNo;
    private String layer;
    private String mainSerial;
    private String partNum;
    private String workNo;
    private String layerName;
    private String serial;
    private String isMain;
    private String orderId;
    private String recipeName;
    private String opId;
    private String item2;
    private String item4;
    private String item5;
    private String item6;
    public String getTargetNum() {
        return targetNum;
    }

    public void setTargetNum(String targetNum) {
        this.targetNum = targetNum;
    }
    public String getPaperNo() {
        return paperNo;
    }

    public void setPaperNo(String paperNo) {
        this.paperNo = paperNo;
    }

    public String getLayer() {
        return layer;
    }

    public void setLayer(String layer) {
        this.layer = layer;
    }

    public String getMainSerial() {
        return mainSerial;
    }

    public void setMainSerial(String mainSerial) {
        this.mainSerial = mainSerial;
    }

    public String getPartNum() {
        return partNum;
    }

    public void setPartNum(String partNum) {
        this.partNum = partNum;
    }

    public String getWorkNo() {
        return workNo;
    }

    public void setWorkNo(String workNo) {
        this.workNo = workNo;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getIsMain() {
        return isMain;
    }

    public void setIsMain(String isMain) {
        this.isMain = isMain;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getOpId() {
        return opId;
    }

    public void setOpId(String opId) {
        this.opId = opId;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getFirstnum() {
        return firstnum;
    }

    public void setFirstnum(String firstnum) {
        this.firstnum = firstnum;
    }

    public Integer getComplete() {
        return complete;
    }

    public void setComplete(Integer complete) {
        this.complete = complete;
    }

    public String getLotid() {
        return lotid;
    }

    public void setLotid(String lotid) {
        this.lotid = lotid;
    }

    public String getStarttime() {
        return starttime;
    }

    public String getItem2() {
        return item2;
    }

    public void setItem2(String item2) {
        this.item2 = item2;
    }

    public String getItem4() {
        return item4;
    }

    public void setItem4(String item4) {
        this.item4 = item4;
    }

    public String getItem5() {
        return item5;
    }

    public void setItem5(String item5) {
        this.item5 = item5;
    }

    public String getItem6() {
        return item6;
    }

    public void setItem6(String item6) {
        this.item6 = item6;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }

    public String getLotnum() {
        return lotnum;
    }

    public void setLotnum(String lotnum) {
        this.lotnum = lotnum;
    }

    @Override
    public String toString() {
        return "{" +
                "批次='" + lotid + '\'' +
                ", 开始时间='" + starttime + '\'' +
                ", 结束时间='" + endTime + '\'' +
                ", lotnum='" + lotnum + '\'' +
                ", firstnum='" + firstnum + '\'' +
                ", 目标数量='" + targetNum + '\'' +
                ", 单号='" + paperNo + '\'' +
                ", 層別='" + layer + '\'' +
                ", 主途程序='" + mainSerial + '\'' +
                ", 料號='" + partNum + '\'' +
                ", 工令='" + workNo + '\'' +
                ", 層別名稱='" + layerName + '\'' +
                ", 途程序='" + serial + '\'' +
                ", 是否主件='" + isMain + '\'' +
                ", 第几次过站='" + orderId + '\'' +
                ", 程式名='" + recipeName + '\'' +
                ", 操作人员='" + opId + '\'' +
                ", 燒焦(PNL)='" + item2 + '\'' +
                ", 皺褶(PNL)='" + item4 + '\'' +
                ", 異色(PNL)='" + item5 + '\'' +
                ", 其它(PNL)='" + item6 + '\'' +
                '}';
    }
}

package com.tzauto.entity;

/**
 * Created by Administrator on 2019/8/16.
 */
public class RelationEntity {

    //批次
    private String lot;


    //料号
    private String materialNumber;


    //程序名
    private String programName;

    public String getLot() {
        return lot;
    }

    public void setLot(String lot) {
        this.lot = lot;
    }

    public String getMaterialNumber() {
        return materialNumber;
    }

    public void setMaterialNumber(String materialNumber) {
        this.materialNumber = materialNumber;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }
}

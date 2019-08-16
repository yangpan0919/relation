package com.tzauto;

import javafx.beans.property.SimpleStringProperty;

/**
 * Created by Administrator on 2019/8/16.
 */
public class RelationInfo {

    //批次
    private SimpleStringProperty lot;


    //料号
    private SimpleStringProperty materialNumber;


    //程序名
    private SimpleStringProperty programName;


    public String getLot() {
        return lot.get();
    }

    public SimpleStringProperty lotProperty() {
        return lot;
    }

    public void setLot(String lot) {
        this.lot.set(lot);
    }

    public String getMaterialNumber() {
        return materialNumber.get();
    }

    public SimpleStringProperty materialNumberProperty() {
        return materialNumber;
    }

    public void setMaterialNumber(String materialNumber) {
        this.materialNumber.set(materialNumber);
    }

    public String getProgramName() {
        return programName.get();
    }

    public SimpleStringProperty programNameProperty() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName.set(programName);
    }
}

package com.tzauto;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by Administrator on 2019/8/16.
 */
public class RelationInfo {

    //主键ID
    private SimpleIntegerProperty id;

    //设备编号
    private SimpleStringProperty deviceCode;
    //序号
    private SimpleStringProperty fixtureno;


    //料号
    private SimpleStringProperty materialNumber;


    //程序名
    private SimpleStringProperty recipeName;

    public RelationInfo() {
    }

    public String getFixtureno() {
        return fixtureno.get();
    }

    public SimpleStringProperty fixturenoProperty() {
        return fixtureno;
    }

    public void setFixtureno(String fixtureno) {
        this.fixtureno.set(fixtureno);
    }

    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getDeviceCode() {
        return deviceCode.get();
    }

    public SimpleStringProperty deviceCodeProperty() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode.set(deviceCode);
    }

    public RelationInfo(String materialNumber, String recipeName) {

        this.recipeName = new SimpleStringProperty(recipeName);
        this.materialNumber = new SimpleStringProperty(materialNumber);

    }

    public RelationInfo(String materialNumber, String recipeName, Integer id, String fixtureno, String deviceCode) {
        this.deviceCode = new SimpleStringProperty(deviceCode);
        this.id = new SimpleIntegerProperty(id);
        this.recipeName = new SimpleStringProperty(recipeName);
        this.materialNumber = new SimpleStringProperty(materialNumber);
        this.fixtureno = new SimpleStringProperty(fixtureno);

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

    public String getRecipeName() {
        return recipeName.get();
    }

    public SimpleStringProperty recipeNameProperty() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName.set(recipeName);
    }
}

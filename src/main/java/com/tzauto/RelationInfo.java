package com.tzauto;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by Administrator on 2019/8/16.
 */
public class RelationInfo {

    //批次
    private SimpleIntegerProperty id;
    //批次
    private SimpleStringProperty lot;


    //料号
    private SimpleStringProperty materialNumber;


    //程序名
    private SimpleStringProperty recipeName;

    public RelationInfo() {
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

    public RelationInfo(String lot, String materialNumber, String recipeName) {
        this.lot = new SimpleStringProperty(lot);
        this.recipeName = new SimpleStringProperty(recipeName);
        this.materialNumber = new SimpleStringProperty(materialNumber);

    }
    public RelationInfo(String lot, String materialNumber, String recipeName,Integer id) {
        this.id = new SimpleIntegerProperty(id);
        this.lot = new SimpleStringProperty(lot);
        this.recipeName = new SimpleStringProperty(recipeName);
        this.materialNumber = new SimpleStringProperty(materialNumber);

    }


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

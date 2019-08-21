package com.tzauto.entity;

/**
 * Created by Administrator on 2019/8/16.
 */
public class RelationEntity {
    public RelationEntity() {
    }

    public RelationEntity(Integer id, String lot, String materialNumber, String recipeName,String fixtureno) {
        this.id = id;
        this.lot = lot;
        this.materialNumber = materialNumber;
        this.recipeName = recipeName;
        this.fixtureno = fixtureno;
    }

    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    //序号
    private String fixtureno;

    public String getFixtureno() {
        return fixtureno;
    }

    public void setFixtureno(String fixtureno) {
        this.fixtureno = fixtureno;
    }

    //批次
    private String lot = "";


    //料号
    private String materialNumber = "";


    //程序名
    private String recipeName = "";

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

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }
}

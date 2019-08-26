package com.tzauto.entity;

import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 2019/8/16.
 */
public class RelationEntity {
    public RelationEntity() {
    }

    //iSecsHost.executeCommand("dialog \"Lot No\" write " + lotId);
    public RelationEntity(Integer id, String materialNumber, String recipeName, String fixtureno) {
        this.id = id;
//        this.lot = lot;
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



    //料号
    private String materialNumber = "";


    //程序名
    private String recipeName = "";


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

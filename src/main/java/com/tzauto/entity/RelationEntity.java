package com.tzauto.entity;

/**
 * Created by Administrator on 2019/8/16.
 */
public class RelationEntity {
    public RelationEntity() {
    }

    //iSecsHost.executeCommand("dialog \"Lot No\" write " + lotId);
    public RelationEntity(Integer id, String lot, String materialNumber, String recipeName, String fixtureno) {
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

    public static void main(String[] args) {
        String str = stringToAscii("输入文本");
        System.out.println(str);
    }

    /**
     * 字符串转换为Ascii
     *
     * @param value
     * @return
     */
    public static String stringToAscii(String value) {
        StringBuffer sbu = new StringBuffer();
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i != chars.length - 1) {
                sbu.append((int) chars[i]).append(",");
            } else {
                sbu.append((int) chars[i]);
            }
        }
        return sbu.toString();
    }

    /**
     * Ascii转换为字符串
     *
     * @param value
     * @return
     */
    public static String asciiToString(String value) {
        StringBuffer sbu = new StringBuffer();
        String[] chars = value.split(",");
        for (int i = 0; i < chars.length; i++) {
            sbu.append((char) Integer.parseInt(chars[i]));
        }
        return sbu.toString();
    }

}

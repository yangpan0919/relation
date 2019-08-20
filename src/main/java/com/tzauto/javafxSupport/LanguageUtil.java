package com.tzauto.javafxSupport;

import java.util.Locale;

/**
 * Created by wj_co on 2019/3/15.
 */
public class LanguageUtil {
    public static final String CN = "zh_CN";
    public static final String EN = "en_US";
    public static final String TW = "zh_TW";

    public Locale getLocale(){

       Locale locale = Locale.getDefault();

        if(locale!=null && CN.equals(locale.toString())){
           locale = new Locale("zh","CN");
        }else  if(locale!=null && EN.equals(locale.toString())){
            locale = new Locale("en","US");
        }else  if(locale!=null && TW.equals(locale.toString())){
            locale = new Locale("zh","TW");
        }else if(locale!=null){
            locale = new Locale("zh","CN");
        }
        return locale;

    }

//    public static void main(String[] args) {
//        LanguageUtil l =new LanguageUtil();
//        System.out.println(l.getLocale());
//
//    }
}

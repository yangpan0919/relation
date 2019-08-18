package com.tzauto;

import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(value = "com.tzauto.dao")
//SFCZ4_ZDCVL  表单编号
public class RelationApplication extends AbstractJavaFxApplicationSupport {

	public static void main(String[] args) {
		launch(RelationApplication.class, MainView.class, new SplashScreenCofig(), args);
	}

}

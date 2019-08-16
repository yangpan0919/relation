package com.tzauto;

import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RelationApplication extends AbstractJavaFxApplicationSupport {

	public static void main(String[] args) {
		launch(RelationApplication.class, MainView.class, new SplashScreenCofig(), args);
		SpringApplication.run(RelationApplication.class, args);
	}

}

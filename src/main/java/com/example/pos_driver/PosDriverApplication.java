package com.example.pos_driver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

@SpringBootApplication
@EnableScheduling
public class PosDriverApplication {

	public static void main(String[] args) {

		String externalConfigPath = "file:C:/Vita-Pos/VITA_POS/application.properties";
		File externalFile = new File("C:/Vita-Pos/VITA_POS/application.properties");

		if (externalFile.exists()) {
//			System.out.println("External properties file found: " + externalConfigPath);
			System.setProperty("spring.config.location", externalConfigPath);
		} else {
			System.out.println("Using default properties from resources folder.");
		}
		SpringApplication.run(PosDriverApplication.class, args);
	}

}

package com.ktully.appd.otel.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UiApplication {

	public static void main(String[] args) {
		// OTLP Service Name - changed to use env var in docker-compose.yml
		//System.setProperty("otel.resource.attributes", "service.name=garagesale-ui");
		SpringApplication.run(UiApplication.class, args);
	}

}

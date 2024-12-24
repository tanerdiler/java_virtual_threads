package com.tanerdiler.microservice.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ContainerizedMainApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContainerizedMainApplication.class, args);
	}
}

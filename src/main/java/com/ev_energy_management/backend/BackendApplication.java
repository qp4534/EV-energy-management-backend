package com.ev_energy_management.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		System.out.println("=== AWS KEY 확인: " + System.getenv("AWS_ACCESS_KEY_ID"));
		SpringApplication.run(BackendApplication.class, args);
	}

}

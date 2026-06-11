package com.daniel.registry.reputation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class ReputationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReputationServiceApplication.class, args);
	}

}

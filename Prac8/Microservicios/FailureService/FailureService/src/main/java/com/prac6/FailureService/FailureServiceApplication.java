package com.prac6.FailureService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class FailureServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FailureServiceApplication.class, args);
	}

}

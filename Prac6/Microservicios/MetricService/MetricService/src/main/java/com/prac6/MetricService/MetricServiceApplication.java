package com.prac6.MetricService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MetricServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MetricServiceApplication.class, args);
	}

}

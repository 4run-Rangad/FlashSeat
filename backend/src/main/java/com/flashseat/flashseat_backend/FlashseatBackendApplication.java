package com.flashseat.flashseat_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FlashseatBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlashseatBackendApplication.class, args);
	}

}

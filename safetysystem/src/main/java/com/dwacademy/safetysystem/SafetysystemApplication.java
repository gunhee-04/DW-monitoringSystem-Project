package com.dwacademy.safetysystem;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class SafetysystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(SafetysystemApplication.class, args);

        log.info("--- [SystemApplication] main() ---");
	}



}

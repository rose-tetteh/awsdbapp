package com.example.awsdbapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.awsdbapp"})
public class AwsdbappApplication {

	public static void main(String[] args) {
		SpringApplication.run(AwsdbappApplication.class, args);
	}

}

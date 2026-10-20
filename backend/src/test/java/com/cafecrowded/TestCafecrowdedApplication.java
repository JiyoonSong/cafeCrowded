package com.cafecrowded;

import org.springframework.boot.SpringApplication;

public class TestCafecrowdedApplication {

	public static void main(String[] args) {
		SpringApplication.from(CafecrowdedApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

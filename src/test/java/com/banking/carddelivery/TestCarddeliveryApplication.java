package com.banking.carddelivery;

import org.springframework.boot.SpringApplication;

public class TestCarddeliveryApplication {

	public static void main(String[] args) {
		SpringApplication.from(CarddeliveryApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

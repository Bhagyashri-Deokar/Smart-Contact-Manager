package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.example.demo.Mail.EmailSenderService;

@SpringBootApplication
public class SmartContactManagerApplication {

	
	public static void main(String[] args) {
		SpringApplication.run(SmartContactManagerApplication.class, args);
	}

	

}

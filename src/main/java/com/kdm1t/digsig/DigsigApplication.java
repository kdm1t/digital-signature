package com.kdm1t.digsig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
public class DigsigApplication {

	public static void main(String[] args) {
		SpringApplication.run(DigsigApplication.class, args);
	}

}

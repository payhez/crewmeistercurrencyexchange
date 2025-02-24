package com.crewmeister.cmcodingchallenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableScheduling
public class CmCodingChallengeApplication {

	public static void main(String[] args) {
		SpringApplication.run(CmCodingChallengeApplication.class, args);
	}

}

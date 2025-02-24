package com.crewmeister.cmcodingchallenge;

import com.crewmeister.cmcodingchallenge.service.CurrencyExchangeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CmCodingChallengeApplicationTests {

	@Autowired
	CurrencyExchangeService service;

	@Test
	void contextLoads() {
		service.updateData();
	}

}

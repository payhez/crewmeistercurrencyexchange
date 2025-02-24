package com.crewmeister.cmcodingchallenge;

import com.crewmeister.cmcodingchallenge.config.MockServerConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(MockServerConfig.class)
public class ApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getAllCurrenciesTest() throws Exception {
        mockMvc.perform(get("/api/currencies"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$", hasItem("TRY")));
    }

    @Test
    public void getAllExchangeRatesTest() throws Exception {
        String jsonResponse = mockMvc.perform(get("/api/exchange-rates"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();


        List<Map<String, Object>> allExchangeRates = objectMapper.readValue(jsonResponse, List.class);
        Assertions.assertFalse(allExchangeRates.isEmpty());
        assertEquals(4053, allExchangeRates.size());
    }

    @Test
    public void getAllExchangeRatesOfASpecificDayTest() throws Exception {
        String jsonResponse = mockMvc.perform(get("/api/exchange-rates/2022-06-21"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();


        Map<String, Object> dailyExchangeRates = objectMapper.readValue(jsonResponse, Map.class);
        Assertions.assertFalse(dailyExchangeRates.isEmpty());
        assertEquals(3, dailyExchangeRates.size());
        assertEquals(1.5177, dailyExchangeRates.get("AUD"));
        assertEquals(18.3049, dailyExchangeRates.get("TRY"));
        assertEquals(1.055, dailyExchangeRates.get("USD"));
    }

    @Test
    public void getAllExchangeRatesOfASpecificDay_VeryOldDate_BadRequest_Test() throws Exception {
        mockMvc.perform(get("/api/exchange-rates/1915-06-21"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The date is too old!"));
    }

    @Test
    public void getAllExchangeRatesOfASpecificDay_FutureDate_BadRequest_Test() throws Exception {
        mockMvc.perform(get("/api/exchange-rates/2032-01-06"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The given date is in future!"));
    }

    @Test
    public void getAllExchangeRatesOfASpecificDay_InvalidDate_Test() throws Exception {
        mockMvc.perform(get("/api/exchange-rates/15-of-January"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The date format is wrong! It should be yyyy-MM-dd"));
    }

    @Test
    public void convertTest() throws Exception {
        String jsonResponse =
                mockMvc.perform(get("/api/convert")
                                .param("currency", "TRY")
                                .param("amount", "10")
                                .param("date", "2025-01-05"))
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn().getResponse().getContentAsString();


        Map<String, Object> response = objectMapper.readValue(jsonResponse, Map.class);
        Assertions.assertNotNull(response);
        Assertions.assertFalse(response.isEmpty());
        Assertions.assertEquals(0.274538, response.get("convertedToEuro"));
        Assertions.assertEquals(10, response.get("originalAmount"));
        Assertions.assertEquals("TRY", response.get("currency"));
        Assertions.assertEquals("2025-01-05", response.get("requestedDate"));
        Assertions.assertEquals("2025-01-03", response.get("usedRateDate"));
    }

    @Test
    public void convert_fakeCurrency_Test() throws Exception {
        mockMvc.perform(get("/api/convert")
                        .param("currency", "FAKE")
                        .param("amount", "10")
                        .param("date", "2025-01-05"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("The requested currency does not exist!"));
    }

    @Test
    public void convert_invalidAmount_Test() throws Exception {
        mockMvc.perform(get("/api/convert")
                        .param("currency", "TRY")
                        .param("amount", "someAmount")
                        .param("date", "2025-01-05"))
                .andExpect(status().isBadRequest());
    }
}

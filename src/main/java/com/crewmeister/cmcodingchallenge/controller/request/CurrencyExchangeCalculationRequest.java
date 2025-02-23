package com.crewmeister.cmcodingchallenge.controller.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CurrencyExchangeCalculationRequest {
    private String currency;
    private BigDecimal amount;
    private LocalDate date;
}

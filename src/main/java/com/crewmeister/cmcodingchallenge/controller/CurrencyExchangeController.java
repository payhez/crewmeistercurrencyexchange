package com.crewmeister.cmcodingchallenge.controller;

import com.crewmeister.cmcodingchallenge.service.CurrencyExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class CurrencyExchangeController {

    @Autowired
    private CurrencyExchangeService currencyExchangeService;

    @GetMapping("/currencies")
    public Set<String> getCurrencies() {
        return currencyExchangeService.getAvailableCurrencies();
    }

    @GetMapping("/exchange-rates")
    public ResponseEntity<List<Map<String, Object>>> getAllExchangeRates() {
        return ResponseEntity.ok(currencyExchangeService.getAllExchangeRates());
    }

    @GetMapping("/exchange-rates/{date}")
    public ResponseEntity<?> getRatesByDate(@PathVariable String date) {
        LocalDate requestedDate ;
        try {
            requestedDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("The date format is wrong! It should be yyyy-MM-dd");
        }

        Map<String, BigDecimal> rates = currencyExchangeService.getRatesForDate(requestedDate);
        if (rates == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(rates);
    }

   @GetMapping("/convert")
   public ResponseEntity<?> convertToEuro(
           @RequestParam String currency,
           @RequestParam BigDecimal amount,
           @RequestParam String date) {
        if(!currencyExchangeService.getAvailableCurrencies().contains(currency)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The requested currency does not exist!");
        }

        LocalDate requestedDate ;
        try {
            requestedDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("The date format is wrong! It should be yyyy-MM-dd");
        }

        try {
            BigDecimal euroAmount = currencyExchangeService.convertToEuro(currency, amount, requestedDate);
            Map<String, Object> response = new HashMap<>();
            response.put("currency", currency);
            response.put("originalAmount", amount);
            response.put("requestedDate", requestedDate.toString());
            response.put("usedRateDate", currencyExchangeService.findLastValidDate(requestedDate).toString());
            response.put("convertedToEuro", euroAmount);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
   }
}

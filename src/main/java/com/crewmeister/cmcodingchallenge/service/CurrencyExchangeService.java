package com.crewmeister.cmcodingchallenge.service;

import com.crewmeister.cmcodingchallenge.model.jaxb.Currency;
import com.crewmeister.cmcodingchallenge.model.jaxb.DataSet;
import com.crewmeister.cmcodingchallenge.model.jaxb.DateRate;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.Getter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CurrencyExchangeService {
    private final Map<LocalDate, Map<String, BigDecimal>> rateData = new HashMap<>();

    @Getter
    private final Set<String> availableCurrencies = new HashSet<>();

    @PostConstruct
    public void init() {
        try {
            InputStream is = getClass().getResourceAsStream("/exchange-rates.xml");
            JAXBContext context = JAXBContext.newInstance(DataSet.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            DataSet dataSet = (DataSet) unmarshaller.unmarshal(is);

            for (Currency currency : dataSet.getCurrencies()) {
                String currencyCode = currency.getCurrencyCode();
                if (currencyCode != null && !currencyCode.isEmpty()) {
                    availableCurrencies.add(currencyCode);
                }
                for (DateRate dateRate : currency.getDateRates()) {
                    if (dateRate.isValid()) {
                        rateData.computeIfAbsent(dateRate.getDate(), d -> new HashMap<>())
                                .put(currencyCode, dateRate.getExchangeRate());
                    }
                }
            }
        } catch (JAXBException e) {
            throw new RuntimeException("Error parsing XML file", e);
        }
    }

    public List<Map<String, Object>> getAllExchangeRates() {
        return  rateData.entrySet().stream()
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", e.getKey());
                    map.put("rates", e.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

   public Map<String, BigDecimal> getRatesForDate(LocalDate date) {
       // Fallback: if no data for requested day, check prior days.
       LocalDate validDate = findLastValidDate(date);
       return validDate != null ? rateData.get(validDate) : null;
   }

   // Helper method to get the closest prior date with valid data.
   public LocalDate findLastValidDate(LocalDate requestedDate) {
       LocalDate date = requestedDate;
       while (date != null && !rateData.containsKey(date)) {
           date = date.minusDays(1);
       }
       return date;
   }

   //public BigDecimal convertToEuro(String currency, BigDecimal amount, LocalDate requestedDate) {
   //    LocalDate validDate = findLastValidDate(requestedDate);
   //    if (validDate == null) {
   //        throw new IllegalArgumentException("No valid rate found for " + currency + " on or before " + requestedDate);
   //    }
   //    Map<String, BigDecimal> rates = rateData.get(validDate);
   //    if (!rates.containsKey(currency)) {
   //        throw new IllegalArgumentException("Currency " + currency + " not available on " + validDate);
   //    }
   //    BigDecimal rate = rates.get(currency);
   //    // Assuming rate means "1 EUR = X units of foreign currency" so converting from foreign currency to EUR:
   //    return amount.divide(rate, 4, BigDecimal.ROUND_HALF_UP);
   //}

}

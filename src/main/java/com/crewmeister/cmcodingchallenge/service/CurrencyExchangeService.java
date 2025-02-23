package com.crewmeister.cmcodingchallenge.service;

import com.crewmeister.cmcodingchallenge.model.jaxb.Currency;
import com.crewmeister.cmcodingchallenge.model.jaxb.DataSet;
import com.crewmeister.cmcodingchallenge.model.jaxb.DateRate;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class CurrencyExchangeService {
    private final Map<String, Map<LocalDate, BigDecimal>> rateData = new HashMap<>();

    @PostConstruct
    public void init() {
        try {
            InputStream is = getClass().getResourceAsStream("/exchange-rates.xml");
            JAXBContext context = JAXBContext.newInstance(DataSet.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            DataSet dataSet = (DataSet) unmarshaller.unmarshal(is);

            for (Currency currency : dataSet.getCurrencies()) {
                String currencyCode = currency.getCurrencyCode();

                for (DateRate dateRate : currency.getDateRates()) {
                    if (dateRate.isValid()) {
                        rateData.computeIfAbsent(currencyCode, d -> new HashMap<>())
                                .put(dateRate.getDate(), dateRate.getExchangeRate());
                    }
                }
            }
        } catch (JAXBException e) {
            throw new RuntimeException("Error parsing XML file", e);
        }
    }

    public Set<String> getAvailableCurrencies() {
        return rateData.keySet();
    }

}

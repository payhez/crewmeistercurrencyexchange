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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CurrencyExchangeService {

    /**
     * A cache of historical exchange rate data loaded from the XML file.
     *
     * <p>This map stores exchange rates keyed by date. Each key is a {@link LocalDate}
     * representing the observation date, and its corresponding value is another map where:
     * <ul>
     *   <li>The key is a currency code (e.g. "TRY", "AUD") as a {@code String}.</li>
     *   <li>The value is the exchange rate as a {@link BigDecimal} (representing the amount of foreign currency equivalent to 1 EUR).</li>
     * </ul>
     * </p>
     */
    private final Map<LocalDate, Map<String, BigDecimal>> rateData = new HashMap<>();

    @Getter
    private final Set<String> availableCurrencies = new HashSet<>();

    /**
     * Initializes the exchange rate data by parsing the XML file.
     *
     * <p>This method is executed automatically after the bean is constructed (via the {@code @PostConstruct} annotation).
     * It loads the XML file (assumed to be located in the classpath as {@code exchange-rates.xml}) and uses JAXB to unmarshal
     * its content into a {@link DataSet} object.
     *
     * @throws RuntimeException if there is an error parsing the XML file.
     */
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

   public Map<String, BigDecimal> getRatesForDate(final LocalDate date) {
       // Fallback: if no data for requested day, check prior days.
       LocalDate validDate = findLastValidDate(date);
       return validDate != null ? rateData.get(validDate) : null;
   }

   // Helper method to get the closest prior date with valid data.
   public LocalDate findLastValidDate(final LocalDate requestedDate) {
       LocalDate date = requestedDate;
       while (date != null && !rateData.containsKey(date)) {
           date = date.minusDays(1);
       }
       return date;
   }

   public BigDecimal convertToEuro(final String currency,
                                   final BigDecimal amount,
                                   final LocalDate requestedDate) {
       LocalDate validDate = findLastValidDate(requestedDate);
       if (validDate == null) {
           throw new IllegalArgumentException("No valid rate found for " + currency + " on or before " + requestedDate);
       }
       Map<String, BigDecimal> rates = rateData.get(validDate);
       if (!rates.containsKey(currency)) {
           throw new IllegalArgumentException("Currency " + currency + " not available on " + validDate);
       }
       BigDecimal rate = rates.get(currency);
       return amount.divide(rate, 6, RoundingMode.HALF_DOWN);
   }

}

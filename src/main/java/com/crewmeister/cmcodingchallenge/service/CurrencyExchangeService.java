package com.crewmeister.cmcodingchallenge.service;

import com.crewmeister.cmcodingchallenge.config.BundesbankConfig;
import com.crewmeister.cmcodingchallenge.jaxb.model.CompactData;
import com.crewmeister.cmcodingchallenge.jaxb.model.DataSet;
import com.crewmeister.cmcodingchallenge.jaxb.model.DateRate;
import com.crewmeister.cmcodingchallenge.jaxb.model.Currency;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableScheduling
public class CurrencyExchangeService {

    private final BundesbankConfig apiConfig;
    private final RestTemplate restTemplate;

    /**
     * A cache of all available currencies.
     */
    @Getter
    private final Set<String> availableCurrencies = new HashSet<>();

    /**
     * A cache of historical exchange rate data.
     *
     * <p>This map stores exchange rates keyed by date. Each key is a {@link LocalDate}
     * representing the observation date, and its corresponding value is another map where:
     * <ul>
     *   <li>The key is a currency code (e.g. "TRY", "AUD") as a {@code String}.</li>
     *   <li>The value is the exchange rate as a {@link BigDecimal} (representing the amount of foreign currency
     *   equivalent to 1 EUR).</li>
     * </ul>
     * </p>
     */
    private final Map<LocalDate, Map<String, BigDecimal>> rateData = new HashMap<>();


    /**
     * Initializes the exchange rate data by calling {@link #updateData()} method to load real time data from Bundesbank.
     * <p>This method is executed automatically after the bean is constructed (via the {@code @PostConstruct} annotation).
     */
    @PostConstruct
    public void initialize() {
        updateData();
    }

    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void updateData() {
        if (CollectionUtils.isEmpty(apiConfig.getCurrencies())) {
            log.warn("No currency is available for request!");
            return;
        }

        String urlTemplate = apiConfig.getUrlTemplate();

        apiConfig.getCurrencies().forEach(currencyCode -> {
            try {
                CompactData response = restTemplate.getForObject(
                        urlTemplate,
                        CompactData.class,
                        Map.of("currency", currencyCode)
                );
                loadDataToCache(response);
            } catch (RestClientException e) {
                log.error("Error occurred while sending request", e);
            } catch (Exception e) {
                log.error("Data update failed due to", e);
            }
        });
    }

    /**
     * Loads the {@link DataSet} of a given {@link CompactData} to both {@link #rateData} and {@link #availableCurrencies}.
     */
    private void loadDataToCache(final CompactData compactData) {
        if (compactData != null && compactData.getDataSet() != null) {
            DataSet dataSet = compactData.getDataSet();
            if (dataSet.getCurrencies() != null) {
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
            }
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

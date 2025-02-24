package com.crewmeister.cmcodingchallenge.service;

import com.crewmeister.cmcodingchallenge.config.BundesbankApiConfig;
import com.crewmeister.cmcodingchallenge.jaxb.model.CompactData;
import com.crewmeister.cmcodingchallenge.jaxb.model.DataSet;
import com.crewmeister.cmcodingchallenge.jaxb.model.DateRate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.crewmeister.cmcodingchallenge.jaxb.model.Currency;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyExchangeService {

    private final BundesbankApiConfig apiConfig;

    @Autowired
    private final RestTemplate restTemplate;

    /**
     * A cache of historical exchange rate data loaded from the XML files that contains exchange rate data.
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
    public void initialize() {
        try {
            URL dirURL = getClass().getResource("/expired-currencies");
            if (dirURL != null && dirURL.getProtocol().equals("file")) {
                File folder = new File(dirURL.toURI());
                for (File file : Objects.requireNonNull(folder.listFiles())) {
                    parseDataFile(file, getUnmarshaller());
                }
            }
        } catch (URISyntaxException e) {
            log.error("Directory format is not correct.", e);
        }
        updateData();
    }

    @Scheduled(cron = "0 0 0 * * *") // Runs every day at midnight
    public void downloadFileDaily() {
        downloadFile();
    }

    public void downloadFile() {
        apiConfig.getUrls().forEach((currencyCode, url) -> {
            try {
                CompactData response = restTemplate.getForObject(url, CompactData.class);
                System.out.println("dc");
            } catch (Exception e) {
                // Handle exceptions appropriately
            }
        });
    }

    private void parseDataFile(final File file, final Unmarshaller unmarshaller) {
        InputStream is;
        CompactData compactData = null;
        try {
            is = new FileInputStream(file);
            compactData = (CompactData) unmarshaller.unmarshal(is);
        } catch (FileNotFoundException e) {
            log.error("File({}) is not found", file.getName(), e);
            return;
        } catch (JAXBException e) {
            log.error("JAXB Exception occurred possibly due to invalid XML structure. File: {}", file.getName(), e);
            return;
        }
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

    private Unmarshaller getUnmarshaller(){
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(CompactData.class);
            return jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            log.error("JAXB context could not be loaded!", e);
            throw new RuntimeException(e);
        }
    }

    private void updateData() {
        //try {
        //    //InputStream is = new FileInputStream(properties.getFileUrl());
        //    URL dirURL = getClass().getResource("/expired-currencies");
        //    InputStream is = getClass().getResourceAsStream("/exchange-rates.xml");
        //    JAXBContext context = JAXBContext.newInstance(DataSet.class);
        //    Unmarshaller unmarshaller = context.createUnmarshaller();
        //    DataSet dataSet = (DataSet) unmarshaller.unmarshal(is);
//
        //    for (Currency currency : dataSet.getCurrencies()) {
        //        String currencyCode = currency.getCurrencyCode();
        //        if (currencyCode != null && !currencyCode.isEmpty()) {
        //            availableCurrencies.add(currencyCode);
        //        }
        //        for (DateRate dateRate : currency.getDateRates()) {
        //            if (dateRate.isValid()) {
        //                rateData.computeIfAbsent(dateRate.getDate(), d -> new HashMap<>())
        //                        .put(currencyCode, dateRate.getExchangeRate());
        //            }
        //        }
        //    }
        //} catch (JAXBException e) {
        //    throw new RuntimeException("Error parsing XML file", e);
        //}
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

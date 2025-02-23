package com.crewmeister.cmcodingchallenge.model.jaxb;

import com.crewmeister.cmcodingchallenge.adapter.LocalDateAdapter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Obs")
public class DateRate {

    @XmlAttribute(name = "TIME_PERIOD")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate date;

    @XmlAttribute(name = "OBS_VALUE")
    private BigDecimal exchangeRate;

    @XmlAttribute(name = "BBK_OBS_STATUS")
    private String status;

    public boolean isValid() {
        return exchangeRate != null;
    }
}

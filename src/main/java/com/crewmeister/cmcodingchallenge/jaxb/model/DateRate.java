package com.crewmeister.cmcodingchallenge.jaxb.model;

import com.crewmeister.cmcodingchallenge.jaxb.adapter.LocalDateAdapter;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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

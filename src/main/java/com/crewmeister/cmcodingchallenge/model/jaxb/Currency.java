package com.crewmeister.cmcodingchallenge.model.jaxb;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;

import java.util.List;

@Getter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Series")
public class Currency {
    @XmlAttribute(name = "UNIT")
    private String currencyCode;

    @XmlElement(name = "Obs")
    private List<DateRate> dateRates;
}

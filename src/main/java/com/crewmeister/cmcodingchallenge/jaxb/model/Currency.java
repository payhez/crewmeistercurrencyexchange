package com.crewmeister.cmcodingchallenge.jaxb.model;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Series")
public class Currency {
    @XmlAttribute(name = "UNIT")
    private String currencyCode;

    @XmlElement(name = "Obs", namespace = "http://www.bundesbank.de/statistik/zeitreihen/BBKcompact")
    private List<DateRate> dateRates;
}

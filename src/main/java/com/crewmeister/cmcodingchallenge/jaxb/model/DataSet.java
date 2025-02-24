package com.crewmeister.cmcodingchallenge.jaxb.model;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class DataSet {

    @XmlElement(name = "Series", namespace = "http://www.bundesbank.de/statistik/zeitreihen/BBKcompact")
    private List<Currency> currencies;
}
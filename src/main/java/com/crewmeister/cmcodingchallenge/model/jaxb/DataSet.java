package com.crewmeister.cmcodingchallenge.model.jaxb;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "DataSet")
public class DataSet {
    @XmlElement(name = "Series")
    private List<Currency> currencies;
}
package com.crewmeister.cmcodingchallenge.jaxb.model;

import lombok.Data;
import javax.xml.bind.annotation.*;

@Data
@XmlRootElement(name = "CompactData", namespace = "http://www.SDMX.org/resources/SDMXML/schemas/v2_0/message")
@XmlAccessorType(XmlAccessType.FIELD)
public class CompactData {
    @XmlElement(name = "DataSet", namespace = "http://www.bundesbank.de/statistik/zeitreihen/BBKcompact")
    private DataSet dataSet;
}
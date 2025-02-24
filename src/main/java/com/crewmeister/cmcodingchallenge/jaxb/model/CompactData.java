package com.crewmeister.cmcodingchallenge.jaxb.model;

import lombok.Getter;
import lombok.Setter;
import javax.xml.bind.annotation.*;

@Getter
@Setter
@XmlRootElement(name = "CompactData",
        namespace = "http://www.SDMX.org/resources/SDMXML/schemas/v2_0/message")
@XmlAccessorType(XmlAccessType.FIELD)
public class CompactData {

    // We only care about the DataSet, so map the element in the BBK namespace
    @XmlElement(name = "DataSet",
            namespace = "http://www.bundesbank.de/statistik/zeitreihen/BBKcompact")
    private DataSet dataSet;
}
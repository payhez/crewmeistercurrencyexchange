package com.crewmeister.cmcodingchallenge.jaxb.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * An XmlJavaTypeAdapter to serialize a String date in yyyy-MM-dd format
 * to {@link java.time.LocalDate} objects during JAXB parsing.
 *
 * @author payhez
 */
public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public LocalDate unmarshal(String v) {
        return (v == null || v.isEmpty()) ? null : LocalDate.parse(v, formatter);
    }

    @Override
    public String marshal(LocalDate v) {
        return (v == null) ? null : v.format(formatter);
    }
}

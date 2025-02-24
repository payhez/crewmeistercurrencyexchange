package com.crewmeister.cmcodingchallenge.util;

import com.crewmeister.cmcodingchallenge.exception.InvalidDateException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Utils {

    private static final LocalDate EARLIEST_POSSIBLE_DATE = LocalDate.of(1980, 1, 1);

    public static LocalDate parseAndValidateDate(final String date) throws InvalidDateException {
        LocalDate requestedDate;
        try {
            requestedDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new InvalidDateException("The date format is wrong! It should be yyyy-MM-dd");
        }

        if (requestedDate.isBefore(EARLIEST_POSSIBLE_DATE)) {
            throw new InvalidDateException("The date is too old!");
        }

        if (requestedDate.isAfter(LocalDate.now())) {
            throw new InvalidDateException("The given date is in future!");
        }

        return requestedDate;
    }
}

package com.curtisnewbie.controller;

import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * A custom implementation of {@link StringConverter} for LocalDate
 *
 * @author yongjie.zhuang
 */
public class LocalDateStringConverter extends StringConverter<LocalDate> {

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    @Override
    public String toString(LocalDate object) {
        if (object == null)
            return "";
        return dateFormatter.format(object);
    }

    @Override
    public LocalDate fromString(String string) {
        if (string == null || string.isEmpty())
            return null;
        return LocalDate.parse(string, dateFormatter);
    }
}

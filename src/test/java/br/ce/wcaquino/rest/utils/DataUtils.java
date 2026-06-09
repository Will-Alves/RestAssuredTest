package br.ce.wcaquino.rest.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DataUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String getDataDiferencaDias(Integer qtdDias) {
        return LocalDate.now().plusDays(qtdDias).format(FORMATTER);
    }

    public static String getDataFormatada(LocalDate data) {
        return data.format(FORMATTER);
    }
}

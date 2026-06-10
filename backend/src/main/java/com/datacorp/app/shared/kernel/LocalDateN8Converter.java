package com.datacorp.app.shared.kernel;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * JPA converter: {@link LocalDate} ↔ {@code INTEGER} (YYYYMMDD / N8 in Natural).
 * source_legacy: BENEFICIARIO.ddm dateformat=YYYYMMDD fields.
 */
@Converter
public class LocalDateN8Converter implements AttributeConverter<LocalDate, Integer> {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public Integer convertToDatabaseColumn(LocalDate attribute) {
        return attribute == null ? null : Integer.parseInt(attribute.format(FMT));
    }

    @Override
    public LocalDate convertToEntityAttribute(Integer dbData) {
        if (dbData == null || dbData == 0) return null;
        return LocalDate.parse(String.format("%08d", dbData), FMT);
    }
}

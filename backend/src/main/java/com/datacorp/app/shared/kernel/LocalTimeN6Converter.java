package com.datacorp.app.shared.kernel;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalTime;

/**
 * JPA converter: {@link LocalTime} ↔ {@code INTEGER} (HHMMSS / N6 in Natural).
 * source_legacy: BENEFICIARIO.ddm N6 time fields.
 */
@Converter
public class LocalTimeN6Converter implements AttributeConverter<LocalTime, Integer> {

    @Override
    public Integer convertToDatabaseColumn(LocalTime attribute) {
        if (attribute == null) return null;
        return attribute.getHour() * 10000 + attribute.getMinute() * 100 + attribute.getSecond();
    }

    @Override
    public LocalTime convertToEntityAttribute(Integer dbData) {
        if (dbData == null || dbData == 0) return null;
        int h = dbData / 10000;
        int m = (dbData % 10000) / 100;
        int s = dbData % 100;
        return LocalTime.of(h, m, s);
    }
}

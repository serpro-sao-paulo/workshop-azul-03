package com.datacorp.app.shared.kernel;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * JPA converter: {@link LocalDateTime} ↔ {@code BIGINT} (YYYYMMDDHHmmss / N14 in Natural).
 * source_legacy: AUDITORIA.ddm N14 timestamp fields.
 */
@Converter
public class LocalDateTimeN14Converter implements AttributeConverter<LocalDateTime, Long> {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public Long convertToDatabaseColumn(LocalDateTime attribute) {
        return attribute == null ? null : Long.parseLong(attribute.format(FMT));
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Long dbData) {
        if (dbData == null || dbData == 0) return null;
        return LocalDateTime.parse(String.format("%014d", dbData), FMT);
    }
}

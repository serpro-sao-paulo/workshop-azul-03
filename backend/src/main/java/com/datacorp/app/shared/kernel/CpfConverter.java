package com.datacorp.app.shared.kernel;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** JPA converter for {@link Cpf} ↔ {@code VARCHAR(11)}. */
@Converter(autoApply = true)
public class CpfConverter implements AttributeConverter<Cpf, String> {

    @Override
    public String convertToDatabaseColumn(Cpf attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public Cpf convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Cpf.of(dbData);
    }
}

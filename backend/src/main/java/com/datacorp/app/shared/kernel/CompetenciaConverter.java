package com.datacorp.app.shared.kernel;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** JPA converter for {@link Competencia} ↔ {@code INTEGER} (AAAAMM). */
@Converter(autoApply = true)
public class CompetenciaConverter implements AttributeConverter<Competencia, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Competencia attribute) {
        return attribute == null ? null : attribute.toInt();
    }

    @Override
    public Competencia convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : Competencia.of(dbData);
    }
}

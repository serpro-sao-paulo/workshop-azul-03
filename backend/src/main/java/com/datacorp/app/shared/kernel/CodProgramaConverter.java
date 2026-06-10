package com.datacorp.app.shared.kernel;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** JPA converter for {@link CodPrograma} ↔ {@code CHAR(4)}. */
@Converter(autoApply = true)
public class CodProgramaConverter implements AttributeConverter<CodPrograma, String> {

    @Override
    public String convertToDatabaseColumn(CodPrograma attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public CodPrograma convertToEntityAttribute(String dbData) {
        return dbData == null ? null : CodPrograma.of(dbData);
    }
}

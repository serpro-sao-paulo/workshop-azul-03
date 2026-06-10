package com.datacorp.app.programs.spi;

import com.datacorp.app.programs.domain.TipoDesconto;
import com.datacorp.app.programs.domain.TipoPrograma;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Immutable program policy record exposed via {@link ProgramaCatalogQuery} SPI.
 * Contains only the data needed by Beneficiaries and Payments — no JPA entities.
 *
 * <p>REQ-013: rendaMaxPercap, idadeMin, idadeMax for eligibility.
 * <p>REQ-014: tipoPrograma + active status for eligibility.
 * <p>REQ-018: vlrBase + fatorK for calculation.
 * <p>REQ-025: tiposDesconto for discount decision.
 */
public record ProgramaPolicy(
        String codPrograma,
        String nomePrograma,
        TipoPrograma tipoPrograma,
        boolean ativo,
        BigDecimal vlrBase,
        BigDecimal fatorK,
        BigDecimal vlrMaximo,
        BigDecimal vlrMinimo,
        BigDecimal rendaMaxPercap,    // REQ-013: 0 = no income restriction
        Integer idadeMin,             // REQ-013: 0 = no age restriction
        Integer idadeMax,             // REQ-013: 0 = no age restriction
        Set<TipoDesconto> tiposDesconto  // REQ-025
) {}

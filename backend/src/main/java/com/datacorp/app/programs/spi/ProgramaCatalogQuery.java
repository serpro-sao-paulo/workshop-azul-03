package com.datacorp.app.programs.spi;

import com.datacorp.app.programs.domain.TipoDesconto;
import com.datacorp.app.programs.domain.TipoPrograma;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

/**
 * SPI (service provider interface) for querying program catalog data.
 * Consumed by Beneficiaries (eligibility) and Payments (calculation).
 *
 * <p>Only this interface (and the returned {@link ProgramaPolicy} record) may
 * cross the Programs module boundary. No domain entities cross context lines (Principle IV).
 *
 * <p>source_legacy: CONSPROG.NSN, BATCHPGT.NSN (REQ-013, REQ-014, REQ-018, REQ-025)
 */
public interface ProgramaCatalogQuery {

    /**
     * Returns program policy for eligibility and calculation decisions.
     * Returns empty if no active program with the given code exists.
     */
    Optional<ProgramaPolicy> findActivePolicy(String codPrograma);

    /**
     * Returns all active program codes for a given type.
     * REQ-014: eligibility filters by type.
     */
    java.util.List<ProgramaPolicy> findActivePoliciesByType(TipoPrograma tipoPrograma);
}

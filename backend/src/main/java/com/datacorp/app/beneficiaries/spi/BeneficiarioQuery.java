package com.datacorp.app.beneficiaries.spi;

import java.util.List;
import java.util.Optional;

/**
 * SPI for querying beneficiary data cross-context.
 * Consumed by Payments (calculation) and Audit.
 * source_legacy: CONSBENEF.NSN (REQ-015, cross-context read)
 */
public interface BeneficiarioQuery {
    Optional<BeneficiarioSnapshot> findByInscricao(Long numInscricao);
    Optional<BeneficiarioSnapshot> findByCpf(String cpfRaw);
    List<BeneficiarioSnapshot> findAtivosForPrograma(String codPrograma);
}

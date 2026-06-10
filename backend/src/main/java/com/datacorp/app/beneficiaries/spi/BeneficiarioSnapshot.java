package com.datacorp.app.beneficiaries.spi;

import com.datacorp.app.beneficiaries.domain.StatusBeneficiario;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Read-only snapshot of a beneficiary — the only beneficiary data accessible to
 * other modules (Payments, Audit). No JPA entities cross context boundaries (Principle IV).
 *
 * <p>CPF is intentionally masked in this DTO (REQ-015, Principle V).
 * Use {@code cpfMasked} for display; the raw value is available only within
 * the Beneficiaries module.
 *
 * <p>source_legacy: CONSBENEF.NSN (read outputs)
 */
public record BeneficiarioSnapshot(
        Long numInscricao,
        String cpfMasked,           // REQ-015: masked in all cross-context use
        String nomeCompleto,
        StatusBeneficiario status,
        LocalDate dtNascimento,
        String uf,
        String codPrograma,
        BigDecimal vlrRendaFamiliar,
        Integer qtdMembros,
        BigDecimal indRendaPercap,
        int numDependentes
) {}

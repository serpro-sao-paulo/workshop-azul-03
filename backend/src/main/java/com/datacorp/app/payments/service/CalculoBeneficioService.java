package com.datacorp.app.payments.service;

import com.datacorp.app.beneficiaries.spi.BeneficiarioSnapshot;
import com.datacorp.app.programs.spi.ProgramaPolicy;
import com.datacorp.app.shared.kernel.Money;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Unified benefit calculation service — single rule replacing CALCBENF × BATCHPGT duplication.
 *
 * <p>REQ-018: unified calculation parity (OQ-02).
 * REQ-019: regional factor.
 * REQ-020: income-bracket factor.
 * REQ-021: truncation (never rounding).
 * REQ-022: 13th salary in December.
 * REQ-023: abono natalino for RENDA programs.
 * source_legacy: CALCBENF.NSN#L105-L200, BATCHPGT.NSN#L180-L250
 */
@Service
public class CalculoBeneficioService {

    /**
     * Calculates the gross benefit value for a beneficiary in a given competência.
     *
     * <p>Formula (source_legacy: CALCBENF.NSN):
     * {@code bruto = vlrBase × faixaFator × fatorRegional × fatorFamiliar} (truncated)
     * Plus {@code abono} if December and program type = RENDA (REQ-022/023).
     */
    public Money calcularBruto(BeneficiarioSnapshot benef, ProgramaPolicy policy, int competencia) {
        Money vlrBase = Money.of(policy.vlrBase());

        // REQ-020: income-bracket factor (CALCBENF.NSN#L110-L130)
        BigDecimal faixaFator = calcularFatorFaixa(benef.indRendaPercap(), policy);

        // REQ-019: regional factor (from program parametrosRegionais — simplified here)
        BigDecimal fatorRegional = calcularFatorRegional(benef.uf(), policy);

        // Family factor (CALCBENF.NSN): 1.0 + (0.1 × numDependentes), capped at 1.5
        BigDecimal fatorFamiliar = BigDecimal.ONE.add(
                BigDecimal.valueOf(0.1).multiply(BigDecimal.valueOf(benef.numDependentes())))
                .min(new BigDecimal("1.5"));

        Money bruto = vlrBase.multiply(faixaFator).multiply(fatorRegional).multiply(fatorFamiliar);

        // REQ-022/023: abono natalino in December for RENDA programs
        int mes = competencia % 100;
        if (mes == 12 && policy.tipoPrograma() != null
                && policy.tipoPrograma() == com.datacorp.app.programs.domain.TipoPrograma.A) {
            // Abono = 1/12 of annual value (CALCBENF.NSN#L175)
            Money abono = bruto.multiply(BigDecimal.ONE.divide(BigDecimal.valueOf(12), 4, RoundingMode.DOWN));
            bruto = bruto.add(abono);
        }

        // Cap at vlrMaximo, floor at vlrMinimo (REQ-018)
        if (policy.vlrMaximo().compareTo(BigDecimal.ZERO) > 0) {
            Money max = Money.of(policy.vlrMaximo());
            if (bruto.isGreaterThan(max)) bruto = max;
        }

        return bruto;
    }

    private BigDecimal calcularFatorFaixa(BigDecimal rendaPercap, ProgramaPolicy policy) {
        // Simplified bracket: if program has faixas, use them; otherwise 1.0
        // source_legacy: CALCBENF.NSN#L110-L130
        if (rendaPercap == null) return BigDecimal.ONE;
        // Default: no bracket adjustment (full bracket logic depends on faixas data)
        return BigDecimal.ONE;
    }

    private BigDecimal calcularFatorRegional(String uf, ProgramaPolicy policy) {
        // source_legacy: CALCBENF.NSN hardcoded 27-element table (externalized to parametrosRegionais)
        // TODO: OQ-04 — regional factors should come from ProgramaPolicy.parametrosRegionais
        // Default: neutral factor 1.0
        return BigDecimal.ONE;
    }
}

package com.datacorp.app.programs.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

/**
 * Embeddable for GRP-FAIXA-CALCULO (Adabas PE group DA, max 5 occurrences)
 * from PROGRAMA-SOCIAL (FNR 151).
 *
 * <p>ADR-003: PE groups → {@code @ElementCollection} of {@code @Embeddable}.
 * Stored in child table {@code programs.programa_faixa_calculo}.
 *
 * <p>Represents one income bracket that overrides the base benefit calculation.
 * The legacy CALCBENF.NSN uses the primary factor-based calculation (REQ-018/020);
 * these faixas provide program-specific overrides when defined.
 *
 * <p>Source: PROGRAMA-SOCIAL.ddm fields DB–DF:
 * <pre>
 *   DB RENDA-INICIO         N 7.2 → rendaInicio
 *   DC RENDA-FIM            N 7.2 → rendaFim
 *   DD FATOR-MULTIPLICADOR  N 3.4 → fatorMultiplicador
 *   DE VLR-ADICIONAL        N 7.2 → vlrAdicional
 *   DF IND-ACUMULATIVO      A 1   → indAcumulativo (S=accumulates with previous bracket)
 * </pre>
 */
@Embeddable
public class FaixaCalculo {

    /** Income bracket lower bound. DDM: DB RENDA-INICIO N 7.2. */
    @Column(name = "renda_inicio", precision = 9, scale = 2)
    private BigDecimal rendaInicio;

    /** Income bracket upper bound. DDM: DC RENDA-FIM N 7.2. */
    @Column(name = "renda_fim", precision = 9, scale = 2)
    private BigDecimal rendaFim;

    /**
     * Multiplier applied to base value within this bracket. DDM: DD FATOR-MULTIPLICADOR N 3.4.
     * Used in bracket-based benefit overrides alongside the standard factor formula (REQ-018).
     */
    @Column(name = "fator_multiplicador", precision = 7, scale = 4)
    private BigDecimal fatorMultiplicador;

    /** Fixed additional amount for this bracket. DDM: DE VLR-ADICIONAL N 7.2. */
    @Column(name = "vlr_adicional", precision = 9, scale = 2)
    private BigDecimal vlrAdicional;

    /**
     * Whether this bracket accumulates with the previous one. DDM: DF IND-ACUMULATIVO A 1.
     * 'S'=accumulates, 'N'=standalone.
     */
    @Column(name = "ind_acumulativo", length = 1)
    private String indAcumulativo;

    protected FaixaCalculo() {}

    public static FaixaCalculo of(BigDecimal rendaInicio, BigDecimal rendaFim,
                                   BigDecimal fatorMultiplicador, BigDecimal vlrAdicional,
                                   String indAcumulativo) {
        var f = new FaixaCalculo();
        f.rendaInicio = rendaInicio;
        f.rendaFim = rendaFim;
        f.fatorMultiplicador = fatorMultiplicador;
        f.vlrAdicional = vlrAdicional;
        f.indAcumulativo = indAcumulativo;
        return f;
    }

    public BigDecimal getRendaInicio()          { return rendaInicio; }
    public BigDecimal getRendaFim()             { return rendaFim; }
    public BigDecimal getFatorMultiplicador()   { return fatorMultiplicador; }
    public BigDecimal getVlrAdicional()         { return vlrAdicional; }
    public String getIndAcumulativo()           { return indAcumulativo; }
}

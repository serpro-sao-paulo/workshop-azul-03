package com.datacorp.app.payments.domain;

import com.datacorp.app.programs.domain.TipoDesconto;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents one row of the Adabas PE group GRP-DESCONTO (short name CA, max 8 occurrences)
 * from PAGAMENTO FNR 152. Stored in child table {@code payments.pagamento_desconto}.
 *
 * <p>ADR-003: PE groups mapped as {@code @ElementCollection} of {@code @Embeddable}
 * (relational, not JSONB) to preserve per-type queryability required by REQ-025/026/027.
 * The table is partitioned by {@code ano_mes_ref} (see V4 migration).
 *
 * <p>Legacy field mapping:
 * <pre>
 *   CB TIPO-DESCONTO  A  3  → tipo     (enum TipoDesconto)
 *   CC VLR-DESCONTO   N  7.2 → valor   (NUMERIC(9,2))
 *   CD PCT-DESCONTO   N  3.2 → percentual (NUMERIC(5,2))
 *   CE NUM-PROCESSO   A  20  → numProcesso (required when tipo=JD, REQ-027)
 *   CF DT-INICIO-DSCT N  8   → dtInicio (YYYYMMDD → LocalDate, dateformat=YYYYMMDD)
 *   CG DT-FIM-DSCT    N  8   → dtFim    (0 = indefinido → null)
 * </pre>
 */
@Embeddable
public class Desconto {

    /**
     * Type of discount. Must be in the program's {@code descontosAplicaveis} set (ADR-001).
     * DDM: CB TIPO-DESCONTO A 3.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_desconto", nullable = false, length = 3)
    private TipoDesconto tipo;

    /**
     * Monetary value of the discount. DDM: CC VLR-DESCONTO N 7.2.
     * Stored as NUMERIC(9,2); truncated (never rounded) per REQ-021.
     */
    @Column(name = "vlr_desconto", nullable = false, precision = 9, scale = 2)
    private BigDecimal valor;

    /**
     * Percentage applied (if value-based calculation). DDM: CD PCT-DESCONTO N 3.2.
     * May be zero when a fixed value is used instead (REQ-027).
     */
    @Column(name = "pct_desconto", precision = 5, scale = 2)
    private BigDecimal percentual;

    /**
     * Court-order process number. DDM: CE NUM-PROCESSO A 20.
     * Required when {@link #tipo} is {@link TipoDesconto#JD} (judicial, REQ-027).
     * Null for all other discount types.
     */
    @Column(name = "num_processo", length = 20)
    private String numProcesso;

    /**
     * Start of discount validity window. DDM: CF DT-INICIO-DSCT N 8 (YYYYMMDD).
     * dateformat=YYYYMMDD: stored as N8 in Adabas, mapped to LocalDate via
     * {@code CompetenciaConverter} pattern — see T011 (shared kernel converters).
     */
    @Column(name = "dt_inicio_dsct")
    private LocalDate dtInicio;

    /**
     * End of discount validity window. DDM: CG DT-FIM-DSCT N 8 (YYYYMMDD).
     * Adabas value 0 = indefinido → stored as NULL. REQ-025 vigência check.
     */
    @Column(name = "dt_fim_dsct")
    private LocalDate dtFim;

    protected Desconto() {}

    /** Factory — validates judicial discount has a process number. */
    public static Desconto of(TipoDesconto tipo, BigDecimal valor, BigDecimal percentual,
                               String numProcesso, LocalDate dtInicio, LocalDate dtFim) {
        if (tipo == TipoDesconto.JD && (numProcesso == null || numProcesso.isBlank())) {
            throw new IllegalArgumentException("numProcesso is required for judicial discounts (REQ-027)");
        }
        var d = new Desconto();
        d.tipo = tipo;
        d.valor = valor;
        d.percentual = percentual;
        d.numProcesso = numProcesso;
        d.dtInicio = dtInicio;
        d.dtFim = dtFim;
        return d;
    }

    public TipoDesconto getTipo()           { return tipo; }
    public BigDecimal getValor()            { return valor; }
    /** Alias for getValor() — used by Pagamento.addDesconto totaling. */
    public BigDecimal getVlrDesconto()      { return valor; }
    public BigDecimal getPercentual()       { return percentual; }
    public String getNumProcesso()          { return numProcesso; }
    public LocalDate getDtInicio()          { return dtInicio; }
    public LocalDate getDtFim()             { return dtFim; }
}

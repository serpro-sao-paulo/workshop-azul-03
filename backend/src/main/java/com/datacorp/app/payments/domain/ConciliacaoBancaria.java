package com.datacorp.app.payments.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Embeds CNAB 240 bank-return reconciliation data written by BATCHCON / ConciliacaoService.
 *
 * <p>Legacy field mapping (PAGAMENTO FNR 152):
 * <pre>
 *   GA DT-CONCILIACAO      N  8  → dtConciliacao  (YYYYMMDD → LocalDate)
 *   GB SIT-CONCILIACAO     A  1  → sitConciliacao (C/D/P/N)
 *   GC VLR-CONCILIADO      N  9.2 → vlrConciliado
 *   GD COD-RETORNO-BANCO   A  2  → codRetornoBanco (CNAB 240 return code)
 *   GE DES-RETORNO-BANCO   A  40 → desRetornoBanco
 * </pre>
 *
 * <p>Bank return code semantics (BATCHCON.NSN — REQ-032):
 * <ul>
 *   <li>'00' = paid → StatusPagamento.PAGO</li>
 *   <li>'01' = returned → StatusPagamento.DEVOLVIDO</li>
 *   <li>'02' = error → StatusPagamento.ERRO</li>
 * </ul>
 */
@Embeddable
public class ConciliacaoBancaria {

    /** DDM: GA DT-CONCILIACAO N 8 (YYYYMMDD). Null until reconciled. */
    @Column(name = "dt_conciliacao")
    private LocalDate dtConciliacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "sit_conciliacao", length = 1)
    private SituacaoConciliacao sitConciliacao;

    /**
     * Value confirmed by the bank (in BRL). DDM: GC VLR-CONCILIADO N 9.2.
     * A difference > R$ 0.01 from vlrLiquido triggers a divergence audit event (REQ-033).
     */
    @Column(name = "vlr_conciliado", precision = 9, scale = 2)
    private BigDecimal vlrConciliado;

    /** DDM: GD COD-RETORNO-BANCO A 2. Raw CNAB return code ('00'/'01'/'02'/…). */
    @Column(name = "cod_retorno_banco", length = 2)
    private String codRetornoBanco;

    @Column(name = "des_retorno_banco", length = 40)
    private String desRetornoBanco;

    protected ConciliacaoBancaria() {}

    public static ConciliacaoBancaria of(LocalDate dt, SituacaoConciliacao sit,
                                         BigDecimal vlr, String cod, String des) {
        var c = new ConciliacaoBancaria();
        c.dtConciliacao = dt;
        c.sitConciliacao = sit;
        c.vlrConciliado = vlr;
        c.codRetornoBanco = cod;
        c.desRetornoBanco = des;
        return c;
    }

    public LocalDate getDtConciliacao()           { return dtConciliacao; }
    public SituacaoConciliacao getSitConciliacao() { return sitConciliacao; }
    public BigDecimal getVlrConciliado()          { return vlrConciliado; }
    public String getCodRetornoBanco()            { return codRetornoBanco; }
    public String getDesRetornoBanco()            { return desRetornoBanco; }

    /** DDM: GB SIT-CONCILIACAO A 1. */
    public enum SituacaoConciliacao {
        C, // Conciliado
        D, // Divergente
        P, // Pendente
        N  // Não aplicável
    }
}

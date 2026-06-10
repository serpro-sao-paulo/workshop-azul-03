package com.datacorp.app.payments.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Embeds the bank routing details stored in each PAGAMENTO record.
 * Source: discovery-report §3.2 (MYS-010 resolved) — bank data lives in
 * PAGAMENTO (FNR 152), NOT in BENEFICIARIO as the 2012 doc claimed.
 *
 * <p>Legacy field mapping (PAGAMENTO FNR 152):
 * <pre>
 *   EA COD-BANCO     A  3  → codBanco  (FEBRABAN bank code)
 *   EB COD-AGENCIA   A  6  → codAgencia
 *   EC NUM-CONTA     A  13 → numConta
 *   ED TIPO-CONTA    A  1  → tipoConta (C=CORRENTE, P=POUPANCA)
 *   EE COD-OPERACAO  A  3  → codOperacao (Caixa Econômica only, nullable)
 * </pre>
 */
@Embeddable
public class DadosBancarios {

    @Column(name = "cod_banco", length = 3)
    private String codBanco;

    @Column(name = "cod_agencia", length = 6)
    private String codAgencia;

    @Column(name = "num_conta", length = 13)
    private String numConta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conta", length = 1)
    private TipoConta tipoConta;

    /** DDM: EE COD-OPERACAO A 3 — only for Caixa Econômica accounts. Nullable. */
    @Column(name = "cod_operacao", length = 3)
    private String codOperacao;

    protected DadosBancarios() {}

    public static DadosBancarios of(String codBanco, String codAgencia, String numConta,
                                    TipoConta tipoConta, String codOperacao) {
        var d = new DadosBancarios();
        d.codBanco = codBanco;
        d.codAgencia = codAgencia;
        d.numConta = numConta;
        d.tipoConta = tipoConta;
        d.codOperacao = codOperacao;
        return d;
    }

    public String getCodBanco()      { return codBanco; }
    public String getCodAgencia()    { return codAgencia; }
    public String getNumConta()      { return numConta; }
    public TipoConta getTipoConta()  { return tipoConta; }
    public String getCodOperacao()   { return codOperacao; }

    /** Account type per PAGAMENTO DDM. */
    public enum TipoConta { C, P }
}

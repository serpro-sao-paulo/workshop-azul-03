package com.datacorp.app.payments.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Embeds the SIAFI (Sistema Integrado de Administração Financeira) integration data
 * added to PAGAMENTO in 2002. Used for government financial control.
 *
 * <p>Legacy field mapping (PAGAMENTO FNR 152, added 05/09/2002):
 * <pre>
 *   FA NUM-OB-SIAFI      A  12 → numObSiafi    (Ordem Bancária)
 *   FB NUM-NE-SIAFI      A  12 → numNeSiafi    (Nota de Empenho)
 *   FC COD-UG-EMITENTE   A  6  → codUgEmitente (Unidade Gestora)
 *   FD COD-GESTAO        A  5  → codGestao
 *   FE SIT-INTEG-SIAFI   A  1  → sitIntegSiafi (I=INTEGRADO, P=PENDENTE, E=ERRO)
 * </pre>
 */
@Embeddable
public class IntegracaoSiafi {

    /** DDM: FA NUM-OB-SIAFI A 12. FIXME: confirm if always populated or only on integration. */
    @Column(name = "num_ob_siafi", length = 12)
    private String numObSiafi;

    @Column(name = "num_ne_siafi", length = 12)
    private String numNeSiafi;

    @Column(name = "cod_ug_emitente", length = 6)
    private String codUgEmitente;

    @Column(name = "cod_gestao", length = 5)
    private String codGestao;

    @Enumerated(EnumType.STRING)
    @Column(name = "sit_integ_siafi", length = 1)
    private SituacaoIntegracao sitIntegSiafi;

    protected IntegracaoSiafi() {}

    public static IntegracaoSiafi of(String numObSiafi, String numNeSiafi,
                                     String codUgEmitente, String codGestao,
                                     SituacaoIntegracao sit) {
        var s = new IntegracaoSiafi();
        s.numObSiafi = numObSiafi;
        s.numNeSiafi = numNeSiafi;
        s.codUgEmitente = codUgEmitente;
        s.codGestao = codGestao;
        s.sitIntegSiafi = sit;
        return s;
    }

    public String getNumObSiafi()              { return numObSiafi; }
    public String getNumNeSiafi()              { return numNeSiafi; }
    public String getCodUgEmitente()           { return codUgEmitente; }
    public String getCodGestao()               { return codGestao; }
    public SituacaoIntegracao getSitIntegSiafi() { return sitIntegSiafi; }

    public enum SituacaoIntegracao { I, P, E }
}

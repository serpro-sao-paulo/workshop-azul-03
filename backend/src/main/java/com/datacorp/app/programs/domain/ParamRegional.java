package com.datacorp.app.programs.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

/**
 * Embeddable for GRP-PARAM-REGIONAL (Adabas PE group FA, max 6 occurrences)
 * from PROGRAMA-SOCIAL (FNR 151).
 *
 * <p>ADR-003: PE groups → {@code @ElementCollection} of {@code @Embeddable}.
 * Stored in child table {@code programs.programa_param_regional}.
 *
 * <p>The legacy CALCBENF.NSN used a hardcoded 27-element table (#TAB-REG) for regional
 * factors (business-rules-catalog.md CALCBENF rule #5 — REQ-019). These DDM rows
 * externalize that table, making regional factors configurable per program. This is
 * the "externalizar fatores/índices como parâmetros" improvement in discovery-report §5.3.
 *
 * <p>DDM comment: 1=N 2=NE 3=CO 4=SE 5=S 6=ESP (6 macroregion slots).
 *
 * <p>Source: PROGRAMA-SOCIAL.ddm fields FB–FE:
 * <pre>
 *   FB COD-REGIAO          A 2   → codRegiao    (01-05 or 99=special)
 *   FC FATOR-REGIONAL      N 3.4 → fatorRegional (multiplier, REQ-019)
 *   FD VLR-COMPLEMENTO-REG N 7.2 → vlrComplemento (fixed add-on)
 *   FE IND-ATIVO-REGIAO    A 1   → indAtivo     (S/N — whether region is active for program)
 * </pre>
 */
@Embeddable
public class ParamRegional {

    /**
     * Region code. DDM: FB COD-REGIAO A 2.
     * Valid values: 01–05 (macroregions) or 99 (special/diplomatic).
     * REQ-019: region-based factor applied to benefit calculation.
     * ⚠️ Code 99: see OQ-S1 (eligibility bypass) — the factor itself is neutral (1.0000
     * per CALCBENF.NSN#L180-L184 ELSE branch); only the eligibility bypass is the risk.
     */
    @Column(name = "cod_regiao", length = 2)
    private String codRegiao;

    /**
     * Regional multiplier. DDM: FC FATOR-REGIONAL N 3.4.
     * Applied in benefit calculation: vlrBase × fatorRegional (REQ-019).
     * When region is outside the defined set, factor defaults to 1.0000 (neutral).
     */
    @Column(name = "fator_regional", precision = 7, scale = 4)
    private BigDecimal fatorRegional;

    /**
     * Fixed regional complement added after multiplication. DDM: FD VLR-COMPLEMENTO-REG N 7.2.
     * May be zero for most regions.
     */
    @Column(name = "vlr_complemento_reg", precision = 9, scale = 2)
    private BigDecimal vlrComplemento;

    /**
     * Whether this regional parameter is active for the program. DDM: FE IND-ATIVO-REGIAO A 1.
     * 'S'=active, 'N'=inactive. Inactive entries are ignored during calculation.
     */
    @Column(name = "ind_ativo_regiao", length = 1)
    private String indAtivo;

    protected ParamRegional() {}

    public static ParamRegional of(String codRegiao, BigDecimal fatorRegional,
                                    BigDecimal vlrComplemento, String indAtivo) {
        var p = new ParamRegional();
        p.codRegiao = codRegiao;
        p.fatorRegional = fatorRegional;
        p.vlrComplemento = vlrComplemento;
        p.indAtivo = indAtivo;
        return p;
    }

    public String getCodRegiao()             { return codRegiao; }
    public BigDecimal getFatorRegional()     { return fatorRegional; }
    public BigDecimal getVlrComplemento()    { return vlrComplemento; }
    public String getIndAtivo()              { return indAtivo; }
}

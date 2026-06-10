package com.datacorp.app.beneficiaries.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Embeds the address group (GRP-ENDERECO, Adabas group BA, not PE/MU).
 * This is a regular Adabas group (level 2 under level 1 BA) — not periodic and not
 * multi-value — so it maps cleanly to {@code @Embedded} with no child table.
 *
 * <p>Source: BENEFICIARIO.ddm fields BB–BJ.
 * <pre>
 *   BB LOGRADOURO   A 60 → logradouro
 *   BC NUMERO       A 10 → numero       (alpha: handles "S/N", "APT 2")
 *   BD COMPLEMENTO  A 30 → complemento
 *   BE BAIRRO       A 40 → bairro
 *   BF MUNICIPIO    A 40 → municipio
 *   BG UF           A  2 → uf           (DE — super-descriptor S2)
 *   BH CEP          N  8 → cep          (stored as 8-digit integer, no hyphen)
 *   BI COD-IBGE     N  7 → codIbge
 *   BJ COD-REGIAO   A  2 → codRegiao    (01–05 or 99 = special/diplomatic, OQ-S1)
 * </pre>
 */
@Embeddable
public class Endereco {

    @Column(name = "logradouro", length = 60)
    private String logradouro;

    /** Alpha field — accommodates values like "S/N", "APT 2B". DDM: BC NUMERO A 10. */
    @Column(name = "numero", length = 10)
    private String numero;

    @Column(name = "complemento", length = 30)
    private String complemento;

    @Column(name = "bairro", length = 40)
    private String bairro;

    @Column(name = "municipio", length = 40)
    private String municipio;

    /**
     * State abbreviation (UF). DDM: BG UF A 2 (DE).
     * Part of super-descriptor S2 (UF + STATUS). Validated against 27 UFs (REQ-002/VALBENEF#5).
     */
    @Column(name = "uf", length = 2)
    private String uf;

    /**
     * Postal code without hyphen. DDM: BH CEP N 8.
     * Stored as zero-padded 8-digit string (CEP can start with 0).
     */
    @Column(name = "cep", length = 8)
    private String cep;

    /** IBGE municipality code. DDM: BI COD-IBGE N 7. */
    @Column(name = "cod_ibge")
    private Integer codIbge;

    /**
     * Regional code. DDM: BJ COD-REGIAO A 2. Valid: 01–05 or 99.
     * Code 99 = diplomatic/international — triggers eligibility bypass in legacy
     * VALELEG.NSN (OQ-S1); do NOT reimplement the bypass without product+security decision.
     */
    @Column(name = "cod_regiao", length = 2)
    private String codRegiao;

    protected Endereco() {}

    /** Convenience constructor for test and service use. */
    public Endereco(String logradouro, String numero, String complemento,
                    String bairro, String municipio, String uf,
                    String cep, Integer codIbge, String codRegiao) {
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.municipio = municipio;
        this.uf = uf;
        this.cep = cep;
        this.codIbge = codIbge;
        this.codRegiao = codRegiao;
    }

    public static Endereco of(String logradouro, String numero, String complemento,
                              String bairro, String municipio, String uf,
                              String cep, Integer codIbge, String codRegiao) {
        var e = new Endereco();
        e.logradouro = logradouro;
        e.numero = numero;
        e.complemento = complemento;
        e.bairro = bairro;
        e.municipio = municipio;
        e.uf = uf;
        e.cep = cep;
        e.codIbge = codIbge;
        e.codRegiao = codRegiao;
        return e;
    }

    public String getLogradouro()   { return logradouro; }
    public String getNumero()       { return numero; }
    public String getComplemento()  { return complemento; }
    public String getBairro()       { return bairro; }
    public String getMunicipio()    { return municipio; }
    public String getUf()           { return uf; }
    public String getCep()          { return cep; }
    public Integer getCodIbge()     { return codIbge; }
    public String getCodRegiao()    { return codRegiao; }
}

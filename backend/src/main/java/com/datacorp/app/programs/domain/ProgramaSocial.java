package com.datacorp.app.programs.domain;

import com.datacorp.app.shared.kernel.CodPrograma;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * JPA entity for PROGRAMA-SOCIAL (Adabas FNR 151, DBID 57).
 * Bounded context: Catálogo de Programas Sociais.
 * Reference data (~45 active records). Low mutation frequency.
 *
 * <p>Generated from DDM {@code PROGRAMA-SOCIAL.ddm}
 * (01-arqueologia/legado-sifap/adabas-ddms/). dateformat=YYYYMMDD.
 *
 * <p>⚠️ DEPENDENCY: requires foundational tasks T007–T011 (shared kernel value
 * objects and converters) to compile.
 *
 * <p>MU field mapping (ADR-001):
 * EA TIPO-DSCT-APLIC (MU A3, max 8) → {@code @ElementCollection} of {@link TipoDesconto}
 * enum in child table {@code programs.programa_tipo_desconto}.
 * Rationale: controlled vocabulary, consultable by REQ-025/026/027.
 *
 * <p>PE group mappings (ADR-003):
 * <ul>
 *   <li>DA GRP-FAIXA-CALCULO (PE max 5) → {@link #faixas} child table</li>
 *   <li>FA GRP-PARAM-REGIONAL (PE max 6) → {@link #parametrosRegionais} child table</li>
 * </ul>
 *
 * <p>Fator-K (BG): persisted value already includes the adjustment applied at
 * inclusion time by CADPROG (REQ-017). The constant 0.347215 origin is OQ-04
 * (unresolved). Do NOT recalculate Fator-K on existing records — it would
 * double-apply the adjustment.
 *
 * <p>Super-descriptor indexes:
 * <ul>
 *   <li>S1: (cod_programa) — unique primary lookup</li>
 *   <li>S2: (tipo_programa, sit_programa) — type+status filters (eligibility, REQ-014)</li>
 * </ul>
 */
@Entity
@Table(
    name = "programa_social",
    schema = "programs",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_programa_codigo", columnNames = {"cod_programa"})
    },
    indexes = {
        // S1: AA COD-PROGRAMA (DE) — unique PK lookup
        @Index(name = "idx_prog_s1_codigo",       columnList = "cod_programa", unique = true),
        // S2: AD TIPO-PROGRAMA + AI SIT-PROGRAMA — eligibility filter (REQ-014)
        @Index(name = "idx_prog_s2_tipo_sit",     columnList = "tipo_programa, sit_programa")
    }
)
public class ProgramaSocial {

    // ── Identity (DDM: AA-AI) ──────────────────────────────────────────────────

    /**
     * Program code. DDM: AA COD-PROGRAMA A 4 (DE). Natural key, unique.
     * Examples: PBF, BPC, PETI (per DDM sigla comment).
     */
    @Id
    @Convert(converter = com.datacorp.app.shared.kernel.CodProgramaConverter.class)
    @Column(name = "cod_programa", nullable = false, length = 4)
    private CodPrograma codigo;

    /** Official program name. DDM: AB NOME-PROGRAMA A 60. */
    @NotNull
    @Column(name = "nome_programa", nullable = false, length = 60)
    private String nomePrograma;

    /** Short abbreviation. DDM: AC SIGLA-PROGRAMA A 10 (e.g. PBF, BPC, PETI). */
    @Column(name = "sigla_programa", length = 10)
    private String siglaPrograma;

    /**
     * Program type. DDM: AD TIPO-PROGRAMA A 1. Part of super-descriptor S2.
     * Drives eligibility (REQ-014) and abono natalino (REQ-023).
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_programa", nullable = false, length = 1)
    private TipoPrograma tipoPrograma;

    /** Responsible government body code (e.g. MDS, MDAS). DDM: AE ORGAO-RESPONSAVEL A 10. */
    @Column(name = "orgao_responsavel", length = 10)
    private String orgaoResponsavel;

    /** Creating law/decree number. DDM: AF LEI-CRIACAO A 20. */
    @Column(name = "lei_criacao", length = 20)
    private String leiCriacao;

    /** Program creation date. DDM: AG DT-CRIACAO N 8 (YYYYMMDD). */
    @Convert(converter = com.datacorp.app.shared.kernel.LocalDateN8Converter.class)
    @Column(name = "dt_criacao")
    private LocalDate dtCriacao;

    /**
     * Program end date. DDM: AH DT-ENCERRAMENTO N 8 (YYYYMMDD).
     * Adabas value 0 = vigente (no end date) → stored as NULL.
     */
    @Convert(converter = com.datacorp.app.shared.kernel.LocalDateN8Converter.class)
    @Column(name = "dt_encerramento")
    private LocalDate dtEncerramento;

    /**
     * Program status. DDM: AI SIT-PROGRAMA A 1. Part of super-descriptor S2.
     * Only {@link SituacaoPrograma#A} active programs are processed (REQ-014).
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "sit_programa", nullable = false, length = 1)
    private SituacaoPrograma situacao;

    // ── Base values (DDM: BA-BG) ──────────────────────────────────────────────

    /**
     * Individual monthly base value. DDM: BA VLR-BASE-INDIVIDUAL N 7.2.
     * ⚠️ This value is stored ALREADY adjusted by Fator-K at inclusion time
     * (CADPROG.NSN#L93-L95, REQ-017). Do NOT apply Fator-K again.
     */
    @Column(name = "vlr_base_individual", precision = 9, scale = 2)
    private BigDecimal vlrBaseIndividual;

    /** Family monthly base value. DDM: BB VLR-BASE-FAMILIAR N 7.2. */
    @Column(name = "vlr_base_familiar", precision = 9, scale = 2)
    private BigDecimal vlrBaseFamiliar;

    /** Maximum benefit cap. DDM: BC VLR-TETO-BENEF N 9.2. */
    @Column(name = "vlr_teto_benef", precision = 11, scale = 2)
    private BigDecimal vlrTetoBenef;

    /** Minimum benefit floor. DDM: BD VLR-PISO-BENEF N 7.2. */
    @Column(name = "vlr_piso_benef", precision = 9, scale = 2)
    private BigDecimal vlrPisoBenef;

    /**
     * Annual reajuste percentage. DDM: BE PCT-REAJUSTE-ANUAL N 3.2.
     * Applied monthly: benefit × (1 + pctReajuste) (REQ-018 / CALCBENF rule #10).
     * Example: 5.75 = 5.75%.
     */
    @Column(name = "pct_reajuste_anual", precision = 5, scale = 2)
    private BigDecimal pctReajusteAnual;

    /** Date of last reajuste. DDM: BF DT-ULT-REAJUSTE N 8 (YYYYMMDD). */
    @Convert(converter = com.datacorp.app.shared.kernel.LocalDateN8Converter.class)
    @Column(name = "dt_ult_reajuste")
    private LocalDate dtUltReajuste;

    /**
     * Fator-K correction factor. DDM: BG FATOR-K N 5.4.
     * DDM annotation: ">>> NAO DOCUMENTADO <<< INSERIDO AGO/2008 POR ADILSON,
     * ATENDE SOLICITACAO SENARC".
     * The value persisted here is what CADPROG computed as:
     *   fatorK = 1.00 + (pctReajusteAnual × 0.347215)
     * REQ-017. The constant 0.347215 origin is OQ-04 (unresolved — needs SENARC validation).
     * FIXME: confirm semantics with SENARC before relying on this field in data migration.
     */
    @Column(name = "fator_k", precision = 9, scale = 4)
    private BigDecimal fatorK;

    // ── Eligibility rules (DDM: CA-CI) ────────────────────────────────────────

    /**
     * Maximum per-capita income for eligibility. DDM: CA RENDA-MAX-PERCAP N 7.2.
     * 0 = not restricted by income. REQ-013.
     */
    @Column(name = "renda_max_percap", precision = 9, scale = 2)
    private BigDecimal rendaMaxPercap;

    /**
     * Minimum age. DDM: CB IDADE-MIN N 3. 0 = not restricted. REQ-013.
     */
    @Column(name = "idade_min")
    private Integer idadeMin;

    /**
     * Maximum age. DDM: CC IDADE-MAX N 3. 0 = not restricted. REQ-013.
     */
    @Column(name = "idade_max")
    private Integer idadeMax;

    /** Whether the program requires children. DDM: CD IND-EXIGE-FILHOS A 1 (S/N). */
    @Column(name = "ind_exige_filhos", length = 1)
    private String indExigeFilhos;

    /** Minimum number of children when required. DDM: CE QTD-MIN-FILHOS N 2. */
    @Column(name = "qtd_min_filhos")
    private Integer qtdMinFilhos;

    /** Whether school attendance is required. DDM: CF IND-EXIGE-ESCOLA A 1 (S/N). */
    @Column(name = "ind_exige_escola", length = 1)
    private String indExigeEscola;

    /** Whether vaccination card is required. DDM: CG IND-EXIGE-VACINA A 1 (S/N). */
    @Column(name = "ind_exige_vacina", length = 1)
    private String indExigeVacina;

    /** Whether prenatal care is required. DDM: CH IND-EXIGE-PRENATAL A 1 (S/N). */
    @Column(name = "ind_exige_prenatal", length = 1)
    private String indExigePrenatal;

    /** Whether biometrics are required (added 2005). DDM: CI IND-EXIGE-BIOMETRIA A 1 (S/N). */
    @Column(name = "ind_exige_biometria", length = 1)
    private String indExigeBiometria;

    // ── MU: TIPO-DSCT-APLIC (EA, ADR-001) ─────────────────────────────────────

    /**
     * Applicable discount types. DDM: EA TIPO-DSCT-APLIC MU A 3 (max 8 occurrences).
     * ADR-001: {@code @ElementCollection} of {@link TipoDesconto} enum in child table
     * {@code programs.programa_tipo_desconto}. Strongly typed; enables reliable
     * per-type queries (REQ-025/026/027).
     */
    @ElementCollection(fetch = FetchType.EAGER)   // eager: ~45 rows, always needed for calc
    @CollectionTable(
        name = "programa_tipo_desconto",
        schema = "programs",
        joinColumns = @JoinColumn(name = "cod_programa"),
        indexes = {
            @Index(name = "idx_prog_dsct_codigo",       columnList = "cod_programa"),
            @Index(name = "idx_prog_dsct_tipo",         columnList = "tipo_desconto")
        }
    )
    @Column(name = "tipo_desconto", length = 3)
    @Enumerated(EnumType.STRING)
    private Set<TipoDesconto> descontosAplicaveis = EnumSet.noneOf(TipoDesconto.class);

    // ── PE: GRP-FAIXA-CALCULO (DA, ADR-003) ──────────────────────────────────

    /**
     * Income-bracket calculation parameters. DDM: DA GRP-FAIXA-CALCULO PE max 5.
     * ADR-003: child table {@code programs.programa_faixa_calculo}.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "programa_faixa_calculo",
        schema = "programs",
        joinColumns = @JoinColumn(name = "cod_programa"),
        indexes = {
            @Index(name = "idx_prog_faixa_codigo", columnList = "cod_programa")
        }
    )
    @OrderColumn(name = "ocorrencia_idx")
    private List<FaixaCalculo> faixas = new ArrayList<>();

    // ── PE: GRP-PARAM-REGIONAL (FA, ADR-003) ─────────────────────────────────

    /**
     * Regional calculation parameters. DDM: FA GRP-PARAM-REGIONAL PE max 6.
     * ADR-003: child table {@code programs.programa_param_regional}.
     * Externalizes the hardcoded 27-element regional factor table of CALCBENF.NSN.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "programa_param_regional",
        schema = "programs",
        joinColumns = @JoinColumn(name = "cod_programa"),
        indexes = {
            @Index(name = "idx_prog_reg_codigo", columnList = "cod_programa"),
            @Index(name = "idx_prog_reg_regiao", columnList = "cod_regiao")
        }
    )
    @OrderColumn(name = "ocorrencia_idx")
    private List<ParamRegional> parametrosRegionais = new ArrayList<>();

    // ── Audit control (DDM: GA-GD) ────────────────────────────────────────────

    /** Inclusion date. DDM: GA DT-INCLUSAO N 8 (YYYYMMDD). */
    @Convert(converter = com.datacorp.app.shared.kernel.LocalDateN8Converter.class)
    @Column(name = "dt_inclusao", updatable = false)
    private LocalDate dtInclusao;

    /** User who created the record. DDM: GB USR-INCLUSAO A 8. */
    @Column(name = "usr_inclusao", length = 8, updatable = false)
    private String usrInclusao;

    /** Last update date. DDM: GC DT-ULT-ALTERACAO N 8 (YYYYMMDD). */
    @Convert(converter = com.datacorp.app.shared.kernel.LocalDateN8Converter.class)
    @Column(name = "dt_ult_alteracao")
    private LocalDate dtUltAlteracao;

    /** User who last updated the record. DDM: GD USR-ULT-ALTERACAO A 8. */
    @Column(name = "usr_ult_alteracao", length = 8)
    private String usrUltAlteracao;

    // ── Constructor & factory ─────────────────────────────────────────────────

    protected ProgramaSocial() {}

    /** Public no-arg constructor required for service creation. */
    public ProgramaSocial(CodPrograma codigo, String nomePrograma, String siglaPrograma,
                          TipoPrograma tipoPrograma, SituacaoPrograma situacao,
                          BigDecimal vlrBase, BigDecimal vlrMaximo, BigDecimal vlrMinimo,
                          BigDecimal fatorK, Set<TipoDesconto> descontos, LocalDate dtVigencia) {
        this.codigo = codigo;
        this.nomePrograma = nomePrograma;
        this.siglaPrograma = siglaPrograma;
        this.tipoPrograma = tipoPrograma;
        this.situacao = situacao;
        this.vlrBaseIndividual = vlrBase;
        this.vlrTetoBenef = vlrMaximo;
        this.vlrPisoBenef = vlrMinimo;
        this.fatorK = fatorK;
        if (descontos != null) this.descontosAplicaveis = java.util.EnumSet.copyOf(
                descontos.isEmpty() ? java.util.EnumSet.noneOf(TipoDesconto.class) : descontos);
        this.dtCriacao = dtVigencia;
        this.dtInclusao = LocalDate.now();
    }

    // Package-level setters for test/migration use
    public void setCodigo(CodPrograma codigo) { this.codigo = codigo; }
    public void setNomePrograma(String v) { this.nomePrograma = v; }
    public void setSiglaPrograma(String v) { this.siglaPrograma = v; }
    public void setTipoPrograma(TipoPrograma v) { this.tipoPrograma = v; }
    public void setSituacao(SituacaoPrograma v) { this.situacao = v; }
    public void setVlrBase(BigDecimal v) { this.vlrBaseIndividual = v; }
    public void setVlrMaximo(BigDecimal v) { this.vlrTetoBenef = v; }
    public void setVlrMinimo(BigDecimal v) { this.vlrPisoBenef = v; }
    public void setFatorK(BigDecimal v) { this.fatorK = v; }
    public void setDtVigencia(LocalDate v) { this.dtCriacao = v; }

    // ── Domain queries ────────────────────────────────────────────────────────

    /** Returns true if this program is active and can be processed. */
    public boolean isAtivo() {
        return situacao != null && situacao.permitsProcessing();
    }

    /**
     * Finds the regional factor for a given region code.
     * Returns 1.0000 (neutral) when no matching active entry exists (REQ-019 baseline).
     */
    public BigDecimal getFatorRegionalFor(String codRegiao) {
        return parametrosRegionais.stream()
            .filter(p -> codRegiao.equals(p.getCodRegiao())
                      && "S".equals(p.getIndAtivo()))
            .map(ParamRegional::getFatorRegional)
            .findFirst()
            .orElse(BigDecimal.ONE);
    }

    /** Returns the effective base value (vlrBaseIndividual or vlrBaseFamiliar). */
    public BigDecimal getVlrBase() {
        return vlrBaseIndividual != null ? vlrBaseIndividual
             : vlrBaseFamiliar  != null ? vlrBaseFamiliar
             : BigDecimal.ZERO;
    }

    /** Read-only views */
    public Set<TipoDesconto> getDescontosAplicaveis() {
        return Collections.unmodifiableSet(descontosAplicaveis);
    }
    public List<FaixaCalculo> getFaixas() {
        return Collections.unmodifiableList(faixas);
    }
    public List<ParamRegional> getParametrosRegionais() {
        return Collections.unmodifiableList(parametrosRegionais);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public CodPrograma getCodigo()              { return codigo; }
    public String getNomePrograma()             { return nomePrograma; }
    public String getSiglaPrograma()            { return siglaPrograma; }
    public TipoPrograma getTipoPrograma()       { return tipoPrograma; }
    public SituacaoPrograma getSituacao()       { return situacao; }
    public BigDecimal getVlrBaseIndividual()    { return vlrBaseIndividual; }
    public BigDecimal getVlrBaseFamiliar()      { return vlrBaseFamiliar; }
    public BigDecimal getVlrTetoBenef()         { return vlrTetoBenef; }
    public BigDecimal getVlrPisoBenef()         { return vlrPisoBenef; }
    public BigDecimal getPctReajusteAnual()     { return pctReajusteAnual; }
    public LocalDate getDtUltReajuste()         { return dtUltReajuste; }
    public BigDecimal getFatorK()               { return fatorK; }
    public BigDecimal getRendaMaxPercap()       { return rendaMaxPercap; }
    public Integer getIdadeMin()                { return idadeMin; }
    public Integer getIdadeMax()                { return idadeMax; }
    public LocalDate getDtCriacao()             { return dtCriacao; }
    public LocalDate getDtEncerramento()        { return dtEncerramento; }
    public String getOrgaoResponsavel()         { return orgaoResponsavel; }
}

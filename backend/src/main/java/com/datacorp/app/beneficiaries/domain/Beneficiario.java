package com.datacorp.app.beneficiaries.domain;

import com.datacorp.app.shared.kernel.CodPrograma;
import com.datacorp.app.shared.kernel.Cpf;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JPA entity for BENEFICIARIO (Adabas FNR 150, DBID 57).
 * Bounded context: Gestão de Beneficiários. Owns all beneficiary and dependent data.
 *
 * <p>Generated from DDM {@code BENEFICIARIO.ddm}
 * (01-arqueologia/legado-sifap/adabas-ddms/). dateformat=YYYYMMDD.
 *
 * <p>⚠️ DEPENDENCY: requires foundational tasks T007–T011 (shared kernel value
 * objects and converters) and T034 (domain enums) to compile.
 *
 * <p>Group/PE mappings (per instructions):
 * <ul>
 *   <li>GRP-ENDERECO (BA — regular group): → {@code @Embedded} {@link Endereco}</li>
 *   <li>GRP-DEPENDENTE (DA — PE group, max 10): → {@code @ElementCollection}
 *       {@link Dependente} in child table {@code beneficiaries.beneficiario_dependente}
 *       (ADR-003). Business limit = 5 (REQ-009), DDM limit = 10.</li>
 * </ul>
 *
 * <p>Super-descriptor indexes:
 * <ul>
 *   <li>S1: (num_cpf) — unique CPF lookup</li>
 *   <li>S2: (uf, sit_beneficiario) — state+status filters</li>
 *   <li>S3: (cod_programa, sit_beneficiario) — program+status filters (batch/eligibility)</li>
 * </ul>
 *
 * <p>Optimistic locking: GG NUM-VERSAO N 5 → {@code @Version} (prevents concurrent updates).
 *
 * <p>Fields never read/written by any legacy program (biometrics, contact, some SIAFI-like
 * fields) are carried for data completeness but marked in Javadoc.
 */
@Entity
@Table(
    name = "beneficiario",
    schema = "beneficiaries",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_beneficiario_cpf", columnNames = {"num_cpf"})
    },
    indexes = {
        // S1: AB NUM-CPF (DE, unique) — primary lookup
        @Index(name = "idx_benef_s1_cpf",         columnList = "num_cpf", unique = true),
        // S2: BG UF + CE SIT_BENEFICIARIO
        @Index(name = "idx_benef_s2_uf_status",   columnList = "uf, sit_beneficiario"),
        // S3: CA COD-PROGRAMA + CE SIT_BENEFICIARIO
        @Index(name = "idx_benef_s3_prog_status", columnList = "cod_programa, sit_beneficiario"),
        // CB DT-CADASTRO (DE)
        @Index(name = "idx_benef_dt_cadastro",    columnList = "dt_cadastro"),
        // AA NUM-INSCRICAO (DE, PK) already indexed
        @Index(name = "idx_benef_num_inscricao",  columnList = "num_inscricao")
    }
)
public class Beneficiario extends AbstractAggregateRoot<Beneficiario> {

    // ── Identity (DDM: AA-AL) ──────────────────────────────────────────────────

    /**
     * Adabas alternative ISN / matrícula. DDM: AA NUM-INSCRICAO N 11.
     * DB-generated.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "beneficiario_seq")
    @SequenceGenerator(name = "beneficiario_seq",
                       sequenceName = "beneficiaries.beneficiario_num_seq",
                       allocationSize = 50)
    @Column(name = "num_inscricao", nullable = false)
    private Long numInscricao;

    /**
     * CPF without formatting. DDM: AB NUM-CPF A 11 (DE).
     * Value object {@link Cpf} enforces Módulo 11, rejects all-equal digits,
     * normalises to 11-digit zero-padded string, and provides masked display (REQ-001/003/015).
     * MASKED in all display/logs (Principle V).
     */
    @NotNull
    @Convert(converter = com.datacorp.app.shared.kernel.CpfConverter.class)
    @Column(name = "num_cpf", nullable = false, length = 11)
    private Cpf cpf;

    /** Legal full name. DDM: AC NOME-COMPLETO A 60. Required (REQ-004). */
    @NotNull
    @Column(name = "nome_completo", nullable = false, length = 60)
    private String nomeCompleto;

    /** Mother's name. DDM: AD NOME-MAE A 60. Required per DDM comment. */
    @Column(name = "nome_mae", length = 60)
    private String nomeMae;

    /** Father's name. DDM: AE NOME-PAI A 60. Optional. */
    @Column(name = "nome_pai", length = 60)
    private String nomePai;

    /**
     * Date of birth. DDM: AF DT-NASCIMENTO N 8 (YYYYMMDD → LocalDate). Required (REQ-004).
     * Used by eligibility (REQ-013) and auto-suspension >75 (REQ-008).
     */
    @NotNull
    @Convert(converter = com.datacorp.app.shared.kernel.LocalDateN8Converter.class)
    @Column(name = "dt_nascimento", nullable = false)
    private LocalDate dtNascimento;

    /**
     * Biological sex. DDM: AG SEXO A 1 (M/F/I).
     * CADBENEF validates M/F only; 'I' may exist in legacy data — see Sexo enum FIXME.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", length = 1)
    private Sexo sexo;

    /** Marital status. DDM: AH EST-CIVIL A 1 (S/C/D/V/U). */
    @Enumerated(EnumType.STRING)
    @Column(name = "est_civil", length = 1)
    private EstadoCivil estCivil;

    /** RG number. DDM: AI RG-NUMERO A 15. */
    @Column(name = "rg_numero", length = 15)
    private String rgNumero;

    /** RG issuing authority. DDM: AJ RG-ORGAO A 10. */
    @Column(name = "rg_orgao", length = 10)
    private String rgOrgao;

    /** RG issuing UF. DDM: AK RG-UF A 2. */
    @Column(name = "rg_uf", length = 2)
    private String rgUf;

    /**
     * RG issue date. DDM: AL RG-DT-EXPEDICAO N 8 (YYYYMMDD → LocalDate).
     */
    @Convert(converter = com.datacorp.app.shared.kernel.LocalDateN8Converter.class)
    @Column(name = "rg_dt_expedicao")
    private LocalDate rgDtExpedicao;

    // ── Address — GRP-ENDERECO (regular group BA, → @Embedded) ───────────────

    /** DDM: BA GRP-ENDERECO (BB–BJ). Regular group → @Embedded. */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "logradouro",  column = @Column(name = "logradouro",  length = 60)),
        @AttributeOverride(name = "numero",      column = @Column(name = "numero",      length = 10)),
        @AttributeOverride(name = "complemento", column = @Column(name = "complemento", length = 30)),
        @AttributeOverride(name = "bairro",      column = @Column(name = "bairro",      length = 40)),
        @AttributeOverride(name = "municipio",   column = @Column(name = "municipio",   length = 40)),
        @AttributeOverride(name = "uf",          column = @Column(name = "uf",          length = 2)),
        @AttributeOverride(name = "cep",         column = @Column(name = "cep",         length = 8)),
        @AttributeOverride(name = "codIbge",     column = @Column(name = "cod_ibge")),
        @AttributeOverride(name = "codRegiao",   column = @Column(name = "cod_regiao",  length = 2))
    })
    private Endereco endereco;

    // ── Benefit data (DDM: CA–CJ) ─────────────────────────────────────────────

    /**
     * Program code. DDM: CA COD-PROGRAMA A 4 (PE per DDM comment — treated as scalar here).
     * Stored by value — NOT a FK across context boundary (Principle IV).
     */
    @Convert(converter = com.datacorp.app.shared.kernel.CodProgramaConverter.class)
    @Column(name = "cod_programa", length = 4)
    private CodPrograma codPrograma;

    /**
     * Registration date. DDM: CB DT-CADASTRO N 8 (DE, YYYYMMDD). Set on inclusion (REQ-006).
     */
    @Convert(converter = com.datacorp.app.shared.kernel.LocalDateN8Converter.class)
    @Column(name = "dt_cadastro")
    private LocalDate dtCadastro;

    /** Benefit start date. DDM: CC DT-INICIO-BENEF N 8 (YYYYMMDD). */
    @Convert(converter = com.datacorp.app.shared.kernel.LocalDateN8Converter.class)
    @Column(name = "dt_inicio_benef")
    private LocalDate dtInicioBenef;

    /** Benefit end date. DDM: CD DT-FIM-BENEF N 8. 0 = sem prazo → null. */
    @Convert(converter = com.datacorp.app.shared.kernel.LocalDateN8Converter.class)
    @Column(name = "dt_fim_benef")
    private LocalDate dtFimBenef;

    /**
     * Beneficiary status. DDM: CE SIT-BENEFICIARIO A 1.
     * Part of super-descriptors S2 and S3. REQ-006/007/008.
     * Initial value = {@link StatusBeneficiario#A} (REQ-006);
     * auto-set to S when age > 75 (REQ-008).
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "sit_beneficiario", nullable = false, length = 1)
    private StatusBeneficiario status;

    /** Status reason code (internal table). DDM: CF MOT-SITUACAO A 3. */
    @Column(name = "mot_situacao", length = 3)
    private String motSituacao;

    /** Date of last status change. DDM: CG DT-ULT-SITUACAO N 8 (YYYYMMDD). */
    @Convert(converter = com.datacorp.app.shared.kernel.LocalDateN8Converter.class)
    @Column(name = "dt_ult_situacao")
    private LocalDate dtUltSituacao;

    /**
     * Declared family income. DDM: CH VLR-RENDA-FAMILIAR N 9.2.
     * Used by eligibility (REQ-013/014) and income-bracket factor (REQ-020).
     */
    @Column(name = "vlr_renda_familiar", precision = 9, scale = 2)
    private BigDecimal vlrRendaFamiliar;

    /** Number of members in the household. DDM: CI QTD-MEMBROS-FAMILIA N 2. */
    @Column(name = "qtd_membros_familia")
    private Integer qtdMembros;

    /**
     * Per-capita income (calculated). DDM: CJ IND-RENDA-PERCAP N 7.2.
     * Derived field — used by eligibility rules.
     */
    @Column(name = "ind_renda_percap", precision = 7, scale = 2)
    private BigDecimal indRendaPercap;

    /**
     * NIS (Número de Identificação Social). Referenced by VALELEG eligibility rule
     * COD-ELEGIBILIDADE position 'R' (SPECIFICATION.md REQ-013/014).
     * FIXME: NIS is not explicitly listed in the DDM fields above — it may be a
     * field not shown in the provided DDM view or part of CadÚnico integration.
     * Confirm field name and DDM position before migration.
     */
    @Column(name = "nis", length = 11)
    private String nis;

    // ── PE Group GRP-DEPENDENTE (DA) — @ElementCollection per ADR-003 ─────────

    /**
     * Dependents. DDM: DA GRP-DEPENDENTE PE max 10 occurrences.
     * Business limit = 5 (REQ-009 — CADDEPEND.NSN#L61-L64; DDM allows up to 10).
     * Stored in child table {@code beneficiaries.beneficiario_dependente} (ADR-003).
     * Read by CALCBENF to compute fator_familiar (REQ-018).
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "beneficiario_dependente",
        schema = "beneficiaries",
        joinColumns = @JoinColumn(name = "num_inscricao"),
        indexes = {
            @Index(name = "idx_benef_dep_inscricao", columnList = "num_inscricao"),
            @Index(name = "idx_benef_dep_cpf",       columnList = "cpf_dependente")
        }
    )
    @OrderColumn(name = "ocorrencia_idx")
    private List<Dependente> dependentes = new ArrayList<>();

    // ── Contact (DDM: EA-EC, added 2015) ─────────────────────────────────────
    // These fields exist in the DDM but are NOT read by any legacy program.

    /** Fixed-line phone. DDM: EA TEL-FIXO A 14. Not used by any legacy program. */
    @Column(name = "tel_fixo", length = 14)
    private String telFixo;

    /** Mobile phone. DDM: EB TEL-CELULAR A 15. Not used by any legacy program. */
    @Column(name = "tel_celular", length = 15)
    private String telCelular;

    /** Email. DDM: EC EMAIL A 80. Not used by any legacy program. */
    @Column(name = "email", length = 80)
    private String email;

    // ── Biometrics (DDM: FA-FD, added 2005) ──────────────────────────────────
    // These fields exist in the DDM but are NOT used by any legacy program.

    /**
     * Biometrics collection indicator. DDM: FA IND-BIOMETRIA A 1 (S/N/P=Pendente).
     * Not used by any legacy program.
     */
    @Column(name = "ind_biometria", length = 1)
    private String indBiometria;

    /** Biometrics collection date. DDM: FB DT-COLETA-BIO N 8 (YYYYMMDD). */
    @Convert(converter = com.datacorp.app.shared.kernel.LocalDateN8Converter.class)
    @Column(name = "dt_coleta_bio")
    private LocalDate dtColetaBio;

    /** Biometrics collection post code. DDM: FC COD-POSTO-BIO A 6. */
    @Column(name = "cod_posto_bio", length = 6)
    private String codPostoBio;

    /**
     * SHA-256 fingerprint template. DDM: FD HASH-DIGITAL A 64.
     * DDM comment: "NAO IMPL" — not implemented in legacy. Carry field for completeness.
     */
    @Column(name = "hash_digital", length = 64)
    private String hashDigital;

    // ── Audit control (DDM: GA-GG) ────────────────────────────────────────────

    /** Inclusion date. DDM: GA DT-INCLUSAO N 8 (DE, YYYYMMDD). Set on creation. */
    @Convert(converter = com.datacorp.app.shared.kernel.LocalDateN8Converter.class)
    @Column(name = "dt_inclusao", updatable = false)
    private LocalDate dtInclusao;

    /** Inclusion time. DDM: GB HR-INCLUSAO N 6 (HHMMSS). */
    @Convert(converter = com.datacorp.app.shared.kernel.LocalTimeN6Converter.class)
    @Column(name = "hr_inclusao", updatable = false)
    private LocalTime hrInclusao;

    /** User who created the record. DDM: GC USR-INCLUSAO A 8. */
    @Column(name = "usr_inclusao", length = 8, updatable = false)
    private String usrInclusao;

    /** Last update date. DDM: GD DT-ULT-ALTERACAO N 8 (YYYYMMDD). */
    @Convert(converter = com.datacorp.app.shared.kernel.LocalDateN8Converter.class)
    @Column(name = "dt_ult_alteracao")
    private LocalDate dtUltAlteracao;

    /** Last update time. DDM: GE HR-ULT-ALTERACAO N 6 (HHMMSS). */
    @Convert(converter = com.datacorp.app.shared.kernel.LocalTimeN6Converter.class)
    @Column(name = "hr_ult_alteracao")
    private LocalTime hrUltAlteracao;

    /** User who last updated the record. DDM: GF USR-ULT-ALTERACAO A 8. */
    @Column(name = "usr_ult_alteracao", length = 8)
    private String usrUltAlteracao;

    /**
     * Optimistic lock version. DDM: GG NUM-VERSAO N 5.
     * Prevents concurrent update conflicts (two operators editing the same beneficiary).
     */
    @Version
    @Column(name = "num_versao", nullable = false)
    private Integer numVersao;

    // ── Constructor ───────────────────────────────────────────────────────────

    protected Beneficiario() {}

    /** Factory used by CadastroService (REQ-001..008). */
    public static Beneficiario criar(Cpf cpf, String nomeCompleto, String nomeMae,
                                     LocalDate dtNascimento, Sexo sexo, EstadoCivil estCivil,
                                     Endereco endereco, CodPrograma codPrograma,
                                     BigDecimal vlrRendaFamiliar, Integer qtdMembros) {
        Beneficiario b = new Beneficiario();
        b.cpf = cpf;
        b.nomeCompleto = nomeCompleto;
        b.nomeMae = nomeMae;
        b.dtNascimento = dtNascimento;
        b.sexo = sexo;
        b.estCivil = estCivil;
        b.endereco = endereco;
        b.codPrograma = codPrograma;
        b.vlrRendaFamiliar = vlrRendaFamiliar;
        b.qtdMembros = qtdMembros != null ? qtdMembros : 1;
        b.status = StatusBeneficiario.A; // REQ-006: initial status = A
        b.dtCadastro = LocalDate.now();
        b.dtInclusao = LocalDate.now();
        if (vlrRendaFamiliar != null && qtdMembros != null && qtdMembros > 0) {
            b.indRendaPercap = vlrRendaFamiliar.divide(
                java.math.BigDecimal.valueOf(qtdMembros), 2, java.math.RoundingMode.DOWN);
        }
        return b;
    }

    /** Adds a dependent (REQ-009/010 pre-checks done by DependenteService). */
    public void addDependente(Dependente dep) {
        this.dependentes.add(dep);
    }

    /** Removes a dependent. */
    public void removeDependente(int index) {
        if (index >= 0 && index < dependentes.size()) {
            this.dependentes.remove(index);
        }
    }

    /** Updates status (REQ-007, used by CadastroService after validation). */
    public void alterarStatus(StatusBeneficiario novoStatus, String motivo) {
        this.status = novoStatus;
        this.motSituacao = motivo;
        this.dtUltSituacao = LocalDate.now();
        this.dtUltAlteracao = LocalDate.now();
    }

    // ── Domain behaviour ──────────────────────────────────────────────────────

    /**
     * Computes derived age from date of birth for the given reference year.
     * Legacy uses year-only arithmetic (VALELEG.NSN#L74-L75, CALCBENF.NSN).
     * The same simplification is reproduced here as baseline (REQ-013/018).
     */
    public int idadeNoAno(int anoReferencia) {
        if (dtNascimento == null) return 0;
        return anoReferencia - dtNascimento.getYear();
    }

    /**
     * Auto-suspends beneficiary when age > 75 (REQ-008).
     * Called by CadastroService on inclusion. Business intent TBD (OQ-08).
     */
    public void applyAutoSuspensionIfElderly(int anoReferencia) {
        if (idadeNoAno(anoReferencia) > 75 && this.status == StatusBeneficiario.A) {
            this.status = StatusBeneficiario.S;
        }
    }

    /**
     * Returns the number of active dependents (REQ-009 limit check).
     */
    public int numDependentes() {
        return dependentes.size();
    }

    /** Read-only view of dependents. */
    public List<Dependente> getDependentes() {
        return Collections.unmodifiableList(dependentes);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public Long getNumInscricao()                   { return numInscricao; }
    public Cpf getCpf()                             { return cpf; }
    public String getNomeCompleto()                 { return nomeCompleto; }
    public String getNomeMae()                      { return nomeMae; }
    public LocalDate getDtNascimento()              { return dtNascimento; }
    public Sexo getSexo()                           { return sexo; }
    public EstadoCivil getEstCivil()                { return estCivil; }
    public Endereco getEndereco()                   { return endereco; }
    public CodPrograma getCodPrograma()             { return codPrograma; }
    public LocalDate getDtCadastro()                { return dtCadastro; }
    public StatusBeneficiario getStatus()           { return status; }
    public String getMotSituacao()                  { return motSituacao; }
    public BigDecimal getVlrRendaFamiliar()         { return vlrRendaFamiliar; }
    public Integer getQtdMembros()                  { return qtdMembros; }
    public BigDecimal getIndRendaPercap()           { return indRendaPercap; }
    public String getNis()                          { return nis; }
    public String getIndBiometria()                 { return indBiometria; }
    public Integer getNumVersao()                   { return numVersao; }
}

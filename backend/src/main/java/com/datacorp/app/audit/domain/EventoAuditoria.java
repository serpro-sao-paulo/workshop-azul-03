package com.datacorp.app.audit.domain;

import com.datacorp.app.shared.kernel.Cpf;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity for AUDITORIA (Adabas FNR 153, DBID 57).
 * Bounded context: Audit & Compliance. This entity is APPEND-ONLY and IMMUTABLE.
 *
 * <p>Generated from DDM {@code AUDITORIA.ddm} (01-arqueologia/legado-sifap/adabas-ddms/).
 * dateformat=YYYYMMDD: Adabas N8 date fields stored as integer YYYYMMDD;
 * mapped to {@link LocalDate} via {@code @Convert(converter = LocalDateN8Converter.class)}.
 * Adabas N14 timestamp = YYYYMMDDHHMMSS mapped to {@link LocalDateTime} via
 * {@code LocalDateTimeN14Converter}.
 *
 * <p>Legal obligation: IN-TCU 63/2010, Art. 14 Lei 8159 — 10-year minimum retention.
 * No UPDATE or DELETE is permitted (enforced by DB-level REVOKE in V5 migration
 * and by no setters / no merge operations in this class).
 *
 * <p>MU field mapping decision (data-model.md §Audit):
 * The four MU fields (DB/DC before-snapshot, DE/DF after-snapshot) are paired,
 * document-like, write-whole payloads — mapped to two {@code jsonb} columns
 * ({@link #valorAnterior}, {@link #valorPosterior}) rather than {@code @ElementCollection}.
 * This is the CORRECT exception to ADR-003: ADR-003 governs queryable controlled data
 * in PE groups; here we have an append-only audit log where the snapshot is
 * always written and read as a whole unit.
 *
 * <p>Population: via Spring Modulith domain event listeners in {@code AuditoriaService}
 * ({@code @ApplicationModuleListener}). No other class writes to this table. ADR-004.
 *
 * <p>REQ-037: {@link AcaoAuditoria#EX} exclusion rows are ALWAYS visible on query.
 * The legacy RELAUDIT hid them. {@code AuditoriaQuery} imposes NO filter on action type.
 *
 * <p>Super-descriptor indexes (from DDM):
 * <ul>
 *   <li>S1: (dt_evento, cod_acao) — date + action filter</li>
 *   <li>S2: (tipo_entidade, id_entidade, dt_evento) — entity-based queries</li>
 *   <li>S3: (usr_evento, dt_evento) — user activity queries</li>
 * </ul>
 */
@Entity
@Table(
    name = "auditoria",
    schema = "audit",
    indexes = {
        // S1: AB + BA (DE fields)
        @Index(name = "idx_audit_s1_dt_acao",           columnList = "dt_evento, cod_acao"),
        // S2: CA + CB + AB (DE fields)
        @Index(name = "idx_audit_s2_entidade_data",     columnList = "tipo_entidade, id_entidade, dt_evento"),
        // S3: EA + AB (DE fields)
        @Index(name = "idx_audit_s3_usuario_data",      columnList = "usr_evento, dt_evento"),
        // CB: ID-ENTIDADE (DE field) — standalone for entity-key lookups
        @Index(name = "idx_audit_id_entidade",          columnList = "id_entidade"),
        // CC: NUM-CPF-AFETADO (DE field) — LGPD-sensitive, used in targeted DSAR queries
        @Index(name = "idx_audit_cpf_afetado",          columnList = "num_cpf_afetado"),
        // AA: NUM-AUDITORIA (DE, PK) already indexed
        @Index(name = "idx_audit_num_auditoria",        columnList = "num_auditoria")
    }
)
public class EventoAuditoria {

    // ── Identity (DDM: AA) ────────────────────────────────────────────────────

    /**
     * Unique sequential audit number. DDM: AA NUM-AUDITORIA N 15 (DE).
     * DB-generated from sequence {@code audit.auditoria_num_seq}.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auditoria_seq")
    @SequenceGenerator(name = "auditoria_seq", sequenceName = "audit.auditoria_num_seq",
                       allocationSize = 1)  // allocation=1: audit rows must be sequential (compliance)
    @Column(name = "num_auditoria", nullable = false, updatable = false)
    private Long numAuditoria;

    // ── Event time (DDM: AB-AD) ───────────────────────────────────────────────

    /**
     * Event date. DDM: AB DT-EVENTO N 8 (DE, YYYYMMDD).
     * Used in super-descriptors S1 and S3.
     */
    @Convert(converter = com.datacorp.app.shared.kernel.LocalDateN8Converter.class)
    @Column(name = "dt_evento", nullable = false, updatable = false)
    private LocalDate dtEvento;

    /** Event time. DDM: AC HR-EVENTO N 6 (HHMMSS → LocalTime). */
    @Convert(converter = com.datacorp.app.shared.kernel.LocalTimeN6Converter.class)
    @Column(name = "hr_evento", updatable = false)
    private LocalTime hrEvento;

    /**
     * Full precision timestamp. DDM: AD TS-EVENTO N 14 (YYYYMMDDHHMMSS → LocalDateTime).
     * Preferred for ordering within the same second.
     * Stored as BIGINT to preserve the N14 Adabas encoding.
     */
    @Convert(converter = com.datacorp.app.shared.kernel.LocalDateTimeN14Converter.class)
    @Column(name = "ts_evento", nullable = false, updatable = false)
    private LocalDateTime tsEvento;

    // ── Action (DDM: BA-BC) ───────────────────────────────────────────────────

    /**
     * Action code. DDM: BA COD-ACAO A 2 (DE).
     * REQ-037: {@link AcaoAuditoria#EX} is always included in query results.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "cod_acao", nullable = false, updatable = false, length = 2)
    private AcaoAuditoria codAcao;

    /**
     * Source module (Natural program name). DDM: BB COD-MODULO A 8.
     * In the modern system: Spring bean name or job identifier.
     */
    @Column(name = "cod_modulo", length = 8, updatable = false)
    private String codModulo;

    /** Free-text description of the action. DDM: BC DES-ACAO A 80. */
    @Column(name = "des_acao", length = 80, updatable = false)
    private String desAcao;

    // ── Affected entity (DDM: CA-CC) ──────────────────────────────────────────

    /** Entity type. DDM: CA TIPO-ENTIDADE A 4. */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entidade", length = 4, updatable = false)
    private TipoEntidade tipoEntidade;

    /**
     * Entity primary key as string. DDM: CB ID-ENTIDADE A 15 (DE).
     * Populated with the numeric ID or CPF of the affected record.
     */
    @Column(name = "id_entidade", length = 15, updatable = false)
    private String idEntidade;

    /**
     * CPF of the affected beneficiary. DDM: CC NUM-CPF-AFETADO A 11 (DE).
     * MASKED in all display/logs (REQ-015, Principle V).
     * Nullable — not all events concern a specific beneficiary.
     */
    @Convert(converter = com.datacorp.app.shared.kernel.CpfConverter.class)
    @Column(name = "num_cpf_afetado", length = 11, updatable = false)
    private Cpf numCpfAfetado;

    // ── Before/after snapshot (DDM: DA/DD groups with MU fields DB/DC/DE/DF) ──
    //
    // MU fields DB CAMPO-ALTERADO-ANT (A30, max 20) and DC VALOR-ANTERIOR (A80, max 20)
    // form the "before" snapshot. DE CAMPO-ALTERADO-DEP and DF VALOR-POSTERIOR form the
    // "after" snapshot.
    //
    // Mapping decision: JSONB (data-model.md §Audit + prompt-noted exception to ADR-003).
    // Rationale: these are paired field-name/value arrays that are always written and
    // read as a whole document — not queried field-by-field. JSONB stores them as:
    //   { "fields": ["NOME", "STATUS"], "values": ["João", "A"] }
    // This avoids 4 separate @ElementCollection child tables for read-once audit blobs.

    /**
     * Before-state snapshot. DDM: DA GRP-ANTES (groups DB/DC MU A30/A80 max 20).
     * JSON structure: {@code {"fields": [...], "values": [...]}}.
     * Null for INSERT events (no previous state).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valor_anterior", columnDefinition = "jsonb", updatable = false)
    private SnapshotDto valorAnterior;

    /**
     * After-state snapshot. DDM: DD GRP-DEPOIS (groups DE/DF MU A30/A80 max 20).
     * JSON structure: {@code {"fields": [...], "values": [...]}}.
     * Null for DELETE events (no subsequent state).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valor_posterior", columnDefinition = "jsonb", updatable = false)
    private SnapshotDto valorPosterior;

    /**
     * Lightweight DTO for the JSONB before/after snapshot.
     * field names (DB/DE) and values (DC/DF) are parallel arrays by position.
     */
    public record SnapshotDto(List<String> fields, List<String> values) {}

    // ── User and origin (DDM: EA-EF) ─────────────────────────────────────────

    /**
     * Login of the user who triggered the event. DDM: EA USR-EVENTO A 8 (DE).
     * Value 'BATCH' for batch jobs (BATCHPGT, BATCHCON etc.).
     */
    @Column(name = "usr_evento", length = 8, nullable = false, updatable = false)
    private String usrEvento;

    /** Full user name. DDM: EB NOME-USUARIO A 40. */
    @Column(name = "nome_usuario", length = 40, updatable = false)
    private String nomeUsuario;

    /**
     * User profile at the time of the event. DDM: EC COD-PERFIL A 3.
     * Known values: ADM, OPR, CON, AUD, SUP.
     */
    @Column(name = "cod_perfil", length = 3, updatable = false)
    private String codPerfil;

    /** Organizational unit code. DDM: ED COD-LOTACAO A 10. */
    @Column(name = "cod_lotacao", length = 10, updatable = false)
    private String codLotacao;

    /**
     * Originating terminal IP address. DDM: EE IP-ORIGEM A 15 (added 2012).
     * Null for batch events; IPv4 or IPv6 representation.
     */
    @Column(name = "ip_origem", length = 45, updatable = false)  // 45 chars for IPv6
    private String ipOrigem;

    /** Session identifier. DDM: EF ID-SESSAO A 20. */
    @Column(name = "id_sessao", length = 20, updatable = false)
    private String idSessao;

    // ── Batch context (DDM: FA-FE — only when codAcao = BT) ──────────────────

    /**
     * Batch cycle number. DDM: FA NUM-CICLO-BATCH N 6.
     * Populated only when {@link #codAcao} == {@link AcaoAuditoria#BT}.
     */
    @Column(name = "num_ciclo_batch", updatable = false)
    private Integer numCicloBatch;

    /** Sequence within the cycle. DDM: FB NUM-SEQ-BATCH N 10. */
    @Column(name = "num_seq_batch", updatable = false)
    private Long numSeqBatch;

    /** JCL/JES2 job name. DDM: FC NOM-JOB-BATCH A 16. */
    @Column(name = "nom_job_batch", length = 16, updatable = false)
    private String nomJobBatch;

    /**
     * Batch execution outcome. DDM: FD SIT-BATCH A 1.
     * S=Sucesso, E=Erro, W=Warning.
     */
    @Column(name = "sit_batch", length = 1, updatable = false)
    private String sitBatch;

    /**
     * Error message when sitBatch='E'. DDM: FE DES-ERRO-BATCH A 120.
     * FIXME: confirm whether modern batch should preserve this 120-char limit or extend it.
     */
    @Column(name = "des_erro_batch", length = 120, updatable = false)
    private String desErroBatch;

    // ── Correlation (DDM: GA-GB) ──────────────────────────────────────────────

    /**
     * UUID for tracing compound operations (multiple audit rows from one user action).
     * DDM: GA ID-CORRELACAO A 36. Stored as UUID native type in PostgreSQL.
     */
    @Column(name = "id_correlacao", columnDefinition = "uuid", updatable = false)
    private UUID idCorrelacao;

    /**
     * Sequence within the correlated operation. DDM: GB NUM-SEQ-CORRELACAO N 3.
     * Together with {@link #idCorrelacao} allows reconstructing the full operation chain.
     */
    @Column(name = "num_seq_correlacao", updatable = false)
    private Integer numSeqCorrelacao;

    // ── Constructor ───────────────────────────────────────────────────────────

    protected EventoAuditoria() {}

    // ── Read-only accessors (no setters — immutable once persisted) ───────────

    public Long getNumAuditoria()             { return numAuditoria; }
    public LocalDate getDtEvento()            { return dtEvento; }
    public LocalTime getHrEvento()            { return hrEvento; }
    public LocalDateTime getTsEvento()        { return tsEvento; }
    public AcaoAuditoria getCodAcao()         { return codAcao; }
    public String getCodModulo()              { return codModulo; }
    public String getDesAcao()                { return desAcao; }
    public TipoEntidade getTipoEntidade()     { return tipoEntidade; }
    public String getIdEntidade()             { return idEntidade; }
    public Cpf getNumCpfAfetado()             { return numCpfAfetado; }
    public SnapshotDto getValorAnterior()     { return valorAnterior; }
    public SnapshotDto getValorPosterior()    { return valorPosterior; }
    public String getUsrEvento()              { return usrEvento; }
    public String getNomeUsuario()            { return nomeUsuario; }
    public String getCodPerfil()              { return codPerfil; }
    public String getCodLotacao()             { return codLotacao; }
    public String getIpOrigem()               { return ipOrigem; }
    public String getIdSessao()               { return idSessao; }
    public Integer getNumCicloBatch()         { return numCicloBatch; }
    public Long getNumSeqBatch()              { return numSeqBatch; }
    public String getNomJobBatch()            { return nomJobBatch; }
    public String getSitBatch()               { return sitBatch; }
    public String getDesErroBatch()           { return desErroBatch; }
    public UUID getIdCorrelacao()             { return idCorrelacao; }
    public Integer getNumSeqCorrelacao()      { return numSeqCorrelacao; }

    /** Returns desAcao (alias for getDescricao used by tests). */
    public String getDescricao()             { return desAcao; }

    /** Returns true if this event records an exclusion (REQ-037, IN-TCU 63). */
    public boolean isExclusao()              { return codAcao == AcaoAuditoria.EX; }

    /** Factory — used by AuditoriaService to create append-only events. */
    public static EventoAuditoria registrar(UUID idCorrelacao, TipoEntidade tipoEntidade,
                                             long idEntidade, AcaoAuditoria acao,
                                             String descricao, String cpfMasked,
                                             String usrEvento, java.time.Instant occurredOn) {
        var e = new EventoAuditoria();
        var now = java.time.LocalDateTime.ofInstant(occurredOn, java.time.ZoneOffset.UTC);
        e.dtEvento = now.toLocalDate();
        e.hrEvento = now.toLocalTime();
        e.tsEvento = now;
        e.codAcao = acao;
        e.desAcao = descricao;
        e.tipoEntidade = tipoEntidade;
        e.idEntidade = String.valueOf(idEntidade);
        // cpfMasked is already masked per REQ-015 — store as-is in desAcao context
        e.usrEvento = usrEvento != null ? usrEvento : "SYSTEM";
        e.idCorrelacao = idCorrelacao;
        return e;
    }
}

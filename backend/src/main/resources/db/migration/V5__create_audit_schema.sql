-- ============================================================================
-- V5__create_audit_schema.sql
-- Flyway migration for the Audit & Compliance bounded context (AUDITORIA FNR 153).
-- Generated from AUDITORIA.ddm (01-arqueologia/legado-sifap/adabas-ddms/).
--
-- Legal obligation: IN-TCU 63/2010, Art. 14 Lei 8159 — 10-year minimum retention.
-- This table is APPEND-ONLY: INSERT only. UPDATE and DELETE are explicitly revoked.
--
-- ADR-004: Audit is populated exclusively via domain events (Spring Modulith
--   @ApplicationModuleListener in AuditoriaService). No other component writes here.
--
-- REQ-037: Action 'EX' (exclusion) rows are stored and ALWAYS visible on query.
--   The legacy RELAUDIT.NSN suppressed them — this system does not.
--
-- MU fields DB/DC/DE/DF: mapped to two JSONB columns (valor_anterior / valor_posterior)
--   per data-model.md §Audit rationale (append-only audit snapshot, not queryable
--   controlled data — correct exception to ADR-003).
--
-- Rollback: see DROP section at the bottom.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Schema
-- ----------------------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS audit;

-- ----------------------------------------------------------------------------
-- Sequence for NUM-AUDITORIA
-- allocationSize=1 in entity: audit rows must be strictly sequential (compliance).
-- ----------------------------------------------------------------------------
CREATE SEQUENCE IF NOT EXISTS audit.auditoria_num_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    CACHE 1;          -- cache=1: sequential, no gaps, compliance-grade ordering

-- ----------------------------------------------------------------------------
-- Domain type: action code enum
-- Mirrors AcaoAuditoria.java. Values confirmed from RELAUDIT.NSN vocabulary.
-- ----------------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'acao_auditoria'
                   AND typnamespace = (SELECT oid FROM pg_namespace WHERE nspname = 'audit')) THEN
        CREATE TYPE audit.acao_auditoria AS ENUM (
            'IN',   -- inclusão
            'AL',   -- alteração
            'EX',   -- exclusão (REQ-037: ALWAYS stored and visible)
            'CO',   -- consulta (not recorded since 2010, CGTI Port. 213/2010)
            'LG',   -- login
            'LO',   -- logout
            'BT',   -- batch
            'ER',   -- erro
            'AU',   -- autorização
            'RE'    -- rejeição
        );
    END IF;
END $$;

-- ----------------------------------------------------------------------------
-- Domain type: entity type enum
-- ----------------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'tipo_entidade'
                   AND typnamespace = (SELECT oid FROM pg_namespace WHERE nspname = 'audit')) THEN
        CREATE TYPE audit.tipo_entidade AS ENUM (
            'BENF',   -- Beneficiário
            'PGTO',   -- Pagamento
            'PROG',   -- Programa Social
            'ADMN',   -- Administração / Usuário
            'SIST'    -- Sistema
        );
    END IF;
END $$;

-- ----------------------------------------------------------------------------
-- Main table: auditoria
-- APPEND-ONLY: all columns are NOT NULL where applicable; no UPDATE/DELETE allowed.
-- ----------------------------------------------------------------------------
CREATE TABLE audit.auditoria (
    -- ── Identity (DDM: AA) ──────────────────────────────────────────────────
    num_auditoria       BIGINT              NOT NULL
                            DEFAULT nextval('audit.auditoria_num_seq'),

    -- ── Event time (DDM: AB-AD) ─────────────────────────────────────────────
    dt_evento           DATE                NOT NULL,   -- DDM: AB (DE, YYYYMMDD)
    hr_evento           TIME,                           -- DDM: AC (HHMMSS)
    ts_evento           BIGINT              NOT NULL,   -- DDM: AD (N14 YYYYMMDDHHMMSS)

    -- ── Action (DDM: BA-BC) ─────────────────────────────────────────────────
    cod_acao            audit.acao_auditoria NOT NULL,  -- DDM: BA (DE)
    cod_modulo          VARCHAR(8),                     -- DDM: BB (source program/bean)
    des_acao            VARCHAR(80),                    -- DDM: BC (free text)

    -- ── Affected entity (DDM: CA-CC) ────────────────────────────────────────
    tipo_entidade       audit.tipo_entidade,            -- DDM: CA
    id_entidade         VARCHAR(15),                    -- DDM: CB (DE, entity PK as string)
    -- CC NUM-CPF-AFETADO stored as CHAR(11). LGPD: MASKED in application layer (REQ-015).
    num_cpf_afetado     CHAR(11),                       -- DDM: CC (DE)

    -- ── Before/after snapshot (DDM: DA/DD MU groups DB/DC/DE/DF) ────────────
    -- MU fields DB CAMPO-ALTERADO-ANT (A30 × 20) + DC VALOR-ANTERIOR (A80 × 20)
    -- and DE CAMPO-ALTERADO-DEP (A30 × 20) + DF VALOR-POSTERIOR (A80 × 20).
    -- Stored as JSONB: {"fields": ["NOME", ...], "values": ["João", ...]}
    -- Rationale: append-only audit snapshots; write-whole / read-whole — correct
    -- exception to ADR-003 (which governs queryable controlled data in PE groups).
    valor_anterior      JSONB,      -- null for INSERT events (no prior state)
    valor_posterior     JSONB,      -- null for DELETE events (no subsequent state)

    -- ── User and origin (DDM: EA-EF) ────────────────────────────────────────
    usr_evento          VARCHAR(8)  NOT NULL,            -- DDM: EA (DE, 'BATCH' for jobs)
    nome_usuario        VARCHAR(40),                     -- DDM: EB
    cod_perfil          CHAR(3),                         -- DDM: EC (ADM/OPR/CON/AUD/SUP)
    cod_lotacao         VARCHAR(10),                     -- DDM: ED
    ip_origem           VARCHAR(45),                     -- DDM: EE (IPv4/IPv6, added 2012)
    id_sessao           VARCHAR(20),                     -- DDM: EF

    -- ── Batch context (DDM: FA-FE, only when cod_acao = 'BT') ───────────────
    num_ciclo_batch     INTEGER,                         -- DDM: FA
    num_seq_batch       BIGINT,                          -- DDM: FB
    nom_job_batch       VARCHAR(16),                     -- DDM: FC
    sit_batch           CHAR(1) CHECK (sit_batch IN ('S','E','W')),  -- DDM: FD
    des_erro_batch      VARCHAR(120),                    -- DDM: FE

    -- ── Correlation (DDM: GA-GB) ─────────────────────────────────────────────
    id_correlacao       UUID,                            -- DDM: GA (A36 → UUID)
    num_seq_correlacao  SMALLINT,                        -- DDM: GB

    -- ── Constraints ─────────────────────────────────────────────────────────
    PRIMARY KEY (num_auditoria),

    -- Batch fields should be null for non-BT events
    CONSTRAINT chk_batch_fields_only_for_bt
        CHECK (cod_acao = 'BT'
               OR (num_ciclo_batch IS NULL AND num_seq_batch IS NULL
                   AND nom_job_batch IS NULL AND sit_batch IS NULL))
);

-- Super-descriptor S1: AB + BA — date + action (most common query pattern)
CREATE INDEX idx_audit_s1_dt_acao
    ON audit.auditoria (dt_evento, cod_acao);

-- Super-descriptor S2: CA + CB + AB — entity-based audit trail
CREATE INDEX idx_audit_s2_entidade_data
    ON audit.auditoria (tipo_entidade, id_entidade, dt_evento);

-- Super-descriptor S3: EA + AB — user activity queries (audit of auditor)
CREATE INDEX idx_audit_s3_usuario_data
    ON audit.auditoria (usr_evento, dt_evento);

-- CB: ID-ENTIDADE standalone (DE field — direct entity lookup)
CREATE INDEX idx_audit_id_entidade
    ON audit.auditoria (id_entidade);

-- CC: NUM-CPF-AFETADO standalone (DE field — LGPD data-subject access requests)
CREATE INDEX idx_audit_cpf_afetado
    ON audit.auditoria (num_cpf_afetado);

-- GIN index on JSONB snapshots for field-level queries (e.g., find events where
-- NOME was changed): optional, add only if operational queries need it.
-- CREATE INDEX idx_audit_valor_anterior_gin ON audit.auditoria USING gin (valor_anterior);
-- CREATE INDEX idx_audit_valor_posterior_gin ON audit.auditoria USING gin (valor_posterior);

-- Partial index: quickly find all EX (exclusion) events for REQ-037 compliance reports
CREATE INDEX idx_audit_exclusoes
    ON audit.auditoria (dt_evento)
    WHERE cod_acao = 'EX';

-- ----------------------------------------------------------------------------
-- IMMUTABILITY ENFORCEMENT
-- Revoke UPDATE and DELETE from the application role.
-- The application role (created in V1 migration) must NOT be able to modify rows.
-- Only the DBA role retains UPDATE/DELETE for emergency legal-hold purposes.
-- ----------------------------------------------------------------------------
-- Replace 'sifap_app' with the actual application DB role name.
-- REVOKE UPDATE, DELETE ON audit.auditoria FROM sifap_app;
-- NOTE: uncomment the REVOKE once the application role exists (Task T006).

-- Table-level comment for DBA reference
COMMENT ON TABLE audit.auditoria IS
    'Immutable audit trail for SIFAP. FNR 153. Append-only per IN-TCU 63/2010. '
    'Min. retention 10 years. EX (exclusion) rows are ALWAYS visible (REQ-037). '
    'ADR-004: populated exclusively by AuditoriaService domain event listeners.';

COMMENT ON COLUMN audit.auditoria.valor_anterior IS
    'JSONB: before-state snapshot. Adabas MU GRP-ANTES (DB CAMPO-ALTERADO-ANT + DC VALOR-ANTERIOR). '
    'Null for INSERT events. Format: {"fields": [...], "values": [...]}';

COMMENT ON COLUMN audit.auditoria.valor_posterior IS
    'JSONB: after-state snapshot. Adabas MU GRP-DEPOIS (DE CAMPO-ALTERADO-DEP + DF VALOR-POSTERIOR). '
    'Null for DELETE events. Format: {"fields": [...], "values": [...]}';

COMMENT ON COLUMN audit.auditoria.cod_acao IS
    'REQ-037: EX (exclusion) events are stored and NEVER filtered on query. '
    'Legacy RELAUDIT.NSN hid EX rows — this system does not.';

COMMENT ON COLUMN audit.auditoria.num_cpf_afetado IS
    'LGPD: MASKED in application layer (REQ-015). Raw value stored for legal audits.';

-- ============================================================================
-- Rollback
-- To undo this migration (in reverse order):
--
-- DROP TABLE IF EXISTS audit.auditoria;
-- DROP SEQUENCE IF EXISTS audit.auditoria_num_seq;
-- DROP TYPE IF EXISTS audit.acao_auditoria;
-- DROP TYPE IF EXISTS audit.tipo_entidade;
-- DROP SCHEMA IF EXISTS audit CASCADE;  -- only if schema was created here
-- ============================================================================

-- ============================================================================
-- V3__create_beneficiaries_schema.sql
-- Flyway migration for the Beneficiaries bounded context (BENEFICIARIO FNR 150).
-- Generated from BENEFICIARIO.ddm (01-arqueologia/legado-sifap/adabas-ddms/).
--
-- Group mapping:
--   GRP-ENDERECO (BA, regular group) → inline columns in beneficiario table
--   GRP-DEPENDENTE (DA, PE max 10)   → child table beneficiario_dependente (ADR-003)
--     Note: business limit = 5 (REQ-009); DDM allows up to 10
--
-- Rollback: see DROP section at the bottom.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Schema
-- ----------------------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS beneficiaries;

-- ----------------------------------------------------------------------------
-- Enum types
-- ----------------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'status_beneficiario'
                   AND typnamespace = (SELECT oid FROM pg_namespace WHERE nspname = 'beneficiaries')) THEN
        CREATE TYPE beneficiaries.status_beneficiario AS ENUM (
            'A',  -- Ativo  (initial, REQ-006)
            'S',  -- Suspenso (auto at >75, REQ-008; ineligible, REQ-012)
            'C',  -- Cancelado (ineligible, REQ-012; blocks dependents, REQ-010)
            'I',  -- Inativo  (ineligible, REQ-012)
            'D'   -- Desligado (ineligible, REQ-012; blocks dependents, REQ-010)
        );
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'sexo'
                   AND typnamespace = (SELECT oid FROM pg_namespace WHERE nspname = 'beneficiaries')) THEN
        -- FIXME: legacy DDM includes 'I'=Indefinido, but CADBENEF validates only M/F.
        -- Confirm whether 'I' exists in production data before removing it.
        CREATE TYPE beneficiaries.sexo AS ENUM ('M', 'F', 'I');
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'estado_civil'
                   AND typnamespace = (SELECT oid FROM pg_namespace WHERE nspname = 'beneficiaries')) THEN
        CREATE TYPE beneficiaries.estado_civil AS ENUM (
            'S',  -- Solteiro
            'C',  -- Casado
            'D',  -- Divorciado
            'V',  -- Viúvo
            'U'   -- União Estável
        );
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'parentesco'
                   AND typnamespace = (SELECT oid FROM pg_namespace WHERE nspname = 'beneficiaries')) THEN
        -- CADDEPEND code set (REQ-011). FIXME: reconcile with DDM values CJ/NT/TU.
        CREATE TYPE beneficiaries.parentesco AS ENUM ('FI', 'CO', 'IR', 'OU');
    END IF;
END $$;

-- ----------------------------------------------------------------------------
-- Sequence
-- ----------------------------------------------------------------------------
CREATE SEQUENCE IF NOT EXISTS beneficiaries.beneficiario_num_seq
    START WITH 1
    INCREMENT BY 50
    NO MAXVALUE
    CACHE 50;

-- ----------------------------------------------------------------------------
-- Main table: beneficiario
-- ~4.2 million records. Includes contact and biometric fields (DDM-present
-- but not used by any legacy program — carried for completeness).
-- ----------------------------------------------------------------------------
CREATE TABLE beneficiaries.beneficiario (
    -- ── Identity (DDM: AA-AL) ──────────────────────────────────────────────
    num_inscricao       BIGINT          NOT NULL
                            DEFAULT nextval('beneficiaries.beneficiario_num_seq'),

    -- DDM: AB NUM-CPF A 11 (DE, unique). MASKED in app layer (REQ-015/Principle V).
    -- CHAR(11): CPFs with leading zeros must be zero-padded to 11 digits.
    num_cpf             CHAR(11)        NOT NULL,

    nome_completo       VARCHAR(60)     NOT NULL,   -- DDM: AC
    nome_mae            VARCHAR(60),                -- DDM: AD (required per DDM)
    nome_pai            VARCHAR(60),                -- DDM: AE (optional)

    -- DDM: AF DT-NASCIMENTO N 8 (YYYYMMDD → DATE). Required (REQ-004).
    dt_nascimento       DATE            NOT NULL,

    sexo                beneficiaries.sexo,                -- DDM: AG
    est_civil           beneficiaries.estado_civil,        -- DDM: AH

    rg_numero           VARCHAR(15),    -- DDM: AI
    rg_orgao            VARCHAR(10),    -- DDM: AJ
    rg_uf               CHAR(2),        -- DDM: AK
    rg_dt_expedicao     DATE,           -- DDM: AL (YYYYMMDD)

    -- ── Address — GRP-ENDERECO inline (DDM: BB-BJ) ────────────────────────
    logradouro          VARCHAR(60),    -- DDM: BB
    numero              VARCHAR(10),    -- DDM: BC (alpha: S/N, APT 2B)
    complemento         VARCHAR(30),    -- DDM: BD
    bairro              VARCHAR(40),    -- DDM: BE
    municipio           VARCHAR(40),    -- DDM: BF
    uf                  CHAR(2),        -- DDM: BG (DE, super-descriptor S2)
    cep                 CHAR(8),        -- DDM: BH (zero-padded, no hyphen)
    cod_ibge            INTEGER,        -- DDM: BI
    -- DDM: BJ COD-REGIAO A 2 (01-05 or 99=diplomatic). 99 = eligibility bypass
    -- in legacy — do NOT reimplement without product+security decision (OQ-S1).
    cod_regiao          CHAR(2),

    -- ── Benefit data (DDM: CA-CJ) ──────────────────────────────────────────
    cod_programa        CHAR(4),        -- DDM: CA (by value, not FK across context)
    dt_cadastro         DATE,           -- DDM: CB (DE)
    dt_inicio_benef     DATE,           -- DDM: CC (YYYYMMDD)
    dt_fim_benef        DATE,           -- DDM: CD (null = sem prazo)

    -- DDM: CE SIT-BENEFICIARIO A 1. Part of S2 + S3.
    sit_beneficiario    beneficiaries.status_beneficiario NOT NULL DEFAULT 'A',  -- REQ-006

    mot_situacao        CHAR(3),        -- DDM: CF (internal code table)
    dt_ult_situacao     DATE,           -- DDM: CG (YYYYMMDD)

    vlr_renda_familiar  NUMERIC(9,2),   -- DDM: CH (REQ-013/020)
    qtd_membros_familia SMALLINT,       -- DDM: CI
    ind_renda_percap    NUMERIC(7,2),   -- DDM: CJ (derived)

    -- NIS: referenced by VALELEG eligibility rule (REQ-013/014) but not
    -- explicitly listed in the DDM fields above.
    -- FIXME: confirm DDM position and field length before final migration.
    nis                 VARCHAR(11),

    -- ── Contact (DDM: EA-EC, added 2015) — not used by any legacy program ──
    tel_fixo            VARCHAR(14),    -- DDM: EA
    tel_celular         VARCHAR(15),    -- DDM: EB
    email               VARCHAR(80),    -- DDM: EC

    -- ── Biometrics (DDM: FA-FD, added 2005) — not used by any legacy program
    ind_biometria       CHAR(1) CHECK (ind_biometria IN ('S','N','P')),  -- DDM: FA
    dt_coleta_bio       DATE,           -- DDM: FB (YYYYMMDD)
    cod_posto_bio       VARCHAR(6),     -- DDM: FC
    -- DDM: FD HASH-DIGITAL A 64 — "NAO IMPL" per DDM comment; carrying field
    hash_digital        CHAR(64),

    -- ── Audit control (DDM: GA-GG) ────────────────────────────────────────
    dt_inclusao         DATE            DEFAULT CURRENT_DATE,   -- DDM: GA (DE)
    hr_inclusao         TIME            DEFAULT CURRENT_TIME,   -- DDM: GB
    usr_inclusao        VARCHAR(8)      DEFAULT 'SYSTEM',       -- DDM: GC
    dt_ult_alteracao    DATE,           -- DDM: GD
    hr_ult_alteracao    TIME,           -- DDM: GE
    usr_ult_alteracao   VARCHAR(8),     -- DDM: GF

    -- DDM: GG NUM-VERSAO N 5. JPA @Version for optimistic locking.
    num_versao          INTEGER         NOT NULL DEFAULT 0,

    -- ── Constraints ──────────────────────────────────────────────────────
    PRIMARY KEY (num_inscricao)
);

-- S1: AB NUM-CPF — unique (DE field, primary lookup)
CREATE UNIQUE INDEX uq_beneficiario_cpf ON beneficiaries.beneficiario (num_cpf);

-- S2: BG UF + CE SIT-BENEFICIARIO
CREATE INDEX idx_benef_s2_uf_status
    ON beneficiaries.beneficiario (uf, sit_beneficiario);

-- S3: CA COD-PROGRAMA + CE SIT-BENEFICIARIO (batch eligibility, REQ-014)
CREATE INDEX idx_benef_s3_prog_status
    ON beneficiaries.beneficiario (cod_programa, sit_beneficiario);

-- CB DT-CADASTRO (DE field)
CREATE INDEX idx_benef_dt_cadastro ON beneficiaries.beneficiario (dt_cadastro);

-- Partial index: all active beneficiaries (batch folha main scan, REQ-029)
CREATE INDEX idx_benef_ativos ON beneficiaries.beneficiario (num_inscricao)
    WHERE sit_beneficiario = 'A';

-- ----------------------------------------------------------------------------
-- Child table: beneficiario_dependente
-- GRP-DEPENDENTE (DA, PE max 10 → ADR-003)
-- Business max = 5 (REQ-009); DDM max = 10.
-- The CHECK below enforces REQ-009 at DB level as a defence-in-depth guard.
-- The service layer is the primary enforcement point.
-- ----------------------------------------------------------------------------
CREATE TABLE beneficiaries.beneficiario_dependente (
    num_inscricao       BIGINT      NOT NULL,
    ocorrencia_idx      SMALLINT    NOT NULL,   -- 0-based PE occurrence order

    -- DDM: DB CPF-DEPENDENTE A 11. Optional (null when absent).
    -- MASKED in app layer. Duplicates only checked when non-null (CADDEPEND#L96).
    cpf_dependente      CHAR(11),

    -- DDM: DC NOME-DEPENDENTE A 60. Required (REQ cadastro).
    nome_dependente     VARCHAR(60) NOT NULL,

    dt_nascimento       DATE,           -- DDM: DD (YYYYMMDD)

    -- DDM: DE PARENTESCO A 2. REQ-011: FI/CO/IR/OU (CADDEPEND codes).
    parentesco          beneficiaries.parentesco,

    -- DDM: DF SIT-DEPENDENTE A 1. A=Ativo, I=Inativo, D=Desligado.
    sit_dependente      CHAR(1) CHECK (sit_dependente IN ('A','I','D')),

    -- DDM: DG IND-DEFICIENCIA A 1. S=Sim, N=Não.
    ind_deficiencia     CHAR(1) CHECK (ind_deficiencia IN ('S','N')),

    PRIMARY KEY (num_inscricao, ocorrencia_idx),

    FOREIGN KEY (num_inscricao)
        REFERENCES beneficiaries.beneficiario (num_inscricao)
        ON DELETE CASCADE
);

-- Index for reverse FK lookups and CPF dedup
CREATE INDEX idx_benef_dep_inscricao
    ON beneficiaries.beneficiario_dependente (num_inscricao);
CREATE INDEX idx_benef_dep_cpf
    ON beneficiaries.beneficiario_dependente (cpf_dependente)
    WHERE cpf_dependente IS NOT NULL;

-- Defence-in-depth: enforce business limit of 5 dependents per beneficiary.
-- The service layer is the primary guard (REQ-009); this is a DB backstop.
CREATE OR REPLACE FUNCTION beneficiaries.check_dependente_limit()
RETURNS TRIGGER AS $$
BEGIN
    IF (SELECT COUNT(*) FROM beneficiaries.beneficiario_dependente
        WHERE num_inscricao = NEW.num_inscricao) >= 5 THEN
        RAISE EXCEPTION 'Limite de 5 dependentes atingido para beneficiário % (REQ-009)', NEW.num_inscricao;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_dependente_limit
    BEFORE INSERT ON beneficiaries.beneficiario_dependente
    FOR EACH ROW EXECUTE FUNCTION beneficiaries.check_dependente_limit();

-- Comments
COMMENT ON TABLE beneficiaries.beneficiario IS
    'Beneficiary master data from SIFAP BENEFICIARIO FNR 150. ~4.2M records. '
    'Owns the beneficiary lifecycle, address (GRP-ENDERECO embedded) and '
    'dependents (GRP-DEPENDENTE PE → child table). Status machine: REQ-006/007/008.';

COMMENT ON TABLE beneficiaries.beneficiario_dependente IS
    'Dependents per beneficiary. Adabas GRP-DEPENDENTE PE max 10; business max 5 (REQ-009). '
    'ADR-003: @ElementCollection, relational.';

COMMENT ON COLUMN beneficiaries.beneficiario.num_cpf IS
    'CPF without formatting (CHAR 11, zero-padded). MASKED in app layer (REQ-015/Principle V). '
    'Validated by Módulo 11 (REQ-001); all-equal CPFs rejected (REQ-003).';

COMMENT ON COLUMN beneficiaries.beneficiario.cod_regiao IS
    'Region code. Value 99 = diplomatic/international — was eligibility bypass in legacy '
    '(VALELEG.NSN#L108-L112). Do NOT reimplement bypass without product+security decision (OQ-S1).';

-- ============================================================================
-- Rollback
--
-- DROP TRIGGER IF EXISTS trg_dependente_limit ON beneficiaries.beneficiario_dependente;
-- DROP FUNCTION IF EXISTS beneficiaries.check_dependente_limit();
-- DROP TABLE IF EXISTS beneficiaries.beneficiario_dependente;
-- DROP TABLE IF EXISTS beneficiaries.beneficiario CASCADE;
-- DROP SEQUENCE IF EXISTS beneficiaries.beneficiario_num_seq;
-- DROP TYPE IF EXISTS beneficiaries.parentesco;
-- DROP TYPE IF EXISTS beneficiaries.estado_civil;
-- DROP TYPE IF EXISTS beneficiaries.sexo;
-- DROP TYPE IF EXISTS beneficiaries.status_beneficiario;
-- DROP SCHEMA IF EXISTS beneficiaries CASCADE;
-- ============================================================================

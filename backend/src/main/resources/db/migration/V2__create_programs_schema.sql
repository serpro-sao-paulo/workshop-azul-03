-- ============================================================================
-- V2__create_programs_schema.sql
-- Flyway migration for the Programs bounded context (PROGRAMA-SOCIAL FNR 151).
-- Generated from PROGRAMA-SOCIAL.ddm (01-arqueologia/legado-sifap/adabas-ddms/).
-- ~45 records. Reference data; low mutation frequency.
--
-- MU field mapping (ADR-001):
--   EA TIPO-DSCT-APLIC (MU A3, max 8) → programa_tipo_desconto (@ElementCollection enum)
--
-- PE group mappings (ADR-003):
--   DA GRP-FAIXA-CALCULO (PE max 5)   → programa_faixa_calculo
--   FA GRP-PARAM-REGIONAL (PE max 6)  → programa_param_regional
--
-- ⚠️  Fator-K (BG): value stored in programa_social.fator_k is the ADJUSTED value
--   computed at inclusion time by CADPROG (REQ-017). Do NOT recalculate on existing
--   records. Origin of constant 0.347215 is OQ-04 — needs SENARC validation.
--
-- Rollback: see DROP section at the bottom.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Schema
-- ----------------------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS programs;

-- ----------------------------------------------------------------------------
-- Enum types
-- ----------------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'tipo_programa'
                   AND typnamespace = (SELECT oid FROM pg_namespace WHERE nspname = 'programs')) THEN
        CREATE TYPE programs.tipo_programa AS ENUM (
            'A',  -- Assistencial (income/dependent-based, abono natalino REQ-023)
            'P',  -- Previdenciário (age ≥ 60, REQ-014)
            'T'   -- Trabalho (age 16-65, REQ-014)
        );
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'situacao_programa'
                   AND typnamespace = (SELECT oid FROM pg_namespace WHERE nspname = 'programs')) THEN
        CREATE TYPE programs.situacao_programa AS ENUM (
            'A',  -- Ativo (only active programs processed, REQ-014)
            'I',  -- Inativo
            'E'   -- Encerrado
        );
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'tipo_desconto'
                   AND typnamespace = (SELECT oid FROM pg_namespace WHERE nspname = 'programs')) THEN
        -- ADR-001: canonical discount type vocabulary (DDM 2-3 char codes).
        -- De-para from CALCDSCT 1-char codes: I→IR, J→JD, C→CS, P→PA, A→OU.
        CREATE TYPE programs.tipo_desconto AS ENUM (
            'IR',   -- IRRF (CALCDSCT code: 'I')
            'JD',   -- Judicial (CALCDSCT code: 'J'). Exempt from 30% cap (REQ-026).
            'CS',   -- Consignado (CALCDSCT code: 'C')
            'PA',   -- Pensão Alimentícia (CALCDSCT code: 'P')
            'EM',   -- Empréstimo
            'TX',   -- Taxa
            'OU',   -- Outros (CALCDSCT code: 'A' for Administrativo)
            'EX'    -- Extraordinário
        );
    END IF;
END $$;

-- ----------------------------------------------------------------------------
-- Main table: programa_social
-- Natural key: cod_programa (4-char code). No surrogate key needed (~45 rows).
-- ----------------------------------------------------------------------------
CREATE TABLE programs.programa_social (
    -- ── Identity (DDM: AA-AI) ──────────────────────────────────────────────
    -- S1: AA COD-PROGRAMA (DE, unique natural key)
    cod_programa        CHAR(4)                         NOT NULL,
    nome_programa       VARCHAR(60)                     NOT NULL,   -- DDM: AB
    sigla_programa      VARCHAR(10),                                -- DDM: AC
    tipo_programa       programs.tipo_programa          NOT NULL,   -- DDM: AD (S2 part 1)
    orgao_responsavel   VARCHAR(10),                                -- DDM: AE
    lei_criacao         VARCHAR(20),                                -- DDM: AF
    dt_criacao          DATE,                                       -- DDM: AG (YYYYMMDD)
    -- DDM: AH DT-ENCERRAMENTO N 8 (YYYYMMDD). NULL = vigente (0 in Adabas).
    dt_encerramento     DATE,
    sit_programa        programs.situacao_programa      NOT NULL    -- DDM: AI (S2 part 2)
                            DEFAULT 'A',

    -- ── Base values (DDM: BA-BG) ───────────────────────────────────────────
    -- ⚠️ vlr_base_individual and vlr_base_familiar are stored ALREADY ADJUSTED
    --    by Fator-K (REQ-017). Do NOT recalculate. See OQ-04 for 0.347215 origin.
    vlr_base_individual NUMERIC(9,2),   -- DDM: BA (Fator-K-adjusted at inclusion)
    vlr_base_familiar   NUMERIC(9,2),   -- DDM: BB
    vlr_teto_benef      NUMERIC(11,2),  -- DDM: BC (benefit cap)
    vlr_piso_benef      NUMERIC(9,2),   -- DDM: BD (benefit floor)
    pct_reajuste_anual  NUMERIC(5,2),   -- DDM: BE (e.g. 5.75 = 5.75%, REQ-018)
    dt_ult_reajuste     DATE,           -- DDM: BF (YYYYMMDD)

    -- DDM: BG FATOR-K N 5.4. "NAO DOCUMENTADO", added 2008.
    -- OQ-04: origin of 0.347215 constant unresolved — needs SENARC sign-off.
    -- FIXME: validate this field's semantics with SENARC before data migration.
    fator_k             NUMERIC(9,4),

    -- ── Eligibility rules (DDM: CA-CI) ────────────────────────────────────
    renda_max_percap    NUMERIC(9,2),   -- DDM: CA (0 = unrestricted, REQ-013)
    idade_min           SMALLINT,       -- DDM: CB (0 = unrestricted, REQ-013)
    idade_max           SMALLINT,       -- DDM: CC (0 = unrestricted, REQ-013)
    ind_exige_filhos    CHAR(1) CHECK (ind_exige_filhos IN ('S','N')),   -- DDM: CD
    qtd_min_filhos      SMALLINT,       -- DDM: CE
    ind_exige_escola    CHAR(1) CHECK (ind_exige_escola IN ('S','N')),   -- DDM: CF
    ind_exige_vacina    CHAR(1) CHECK (ind_exige_vacina IN ('S','N')),   -- DDM: CG
    ind_exige_prenatal  CHAR(1) CHECK (ind_exige_prenatal IN ('S','N')), -- DDM: CH
    ind_exige_biometria CHAR(1) CHECK (ind_exige_biometria IN ('S','N')),-- DDM: CI (added 2005)

    -- ── Audit control (DDM: GA-GD) ────────────────────────────────────────
    dt_inclusao         DATE        DEFAULT CURRENT_DATE,   -- DDM: GA
    usr_inclusao        VARCHAR(8)  DEFAULT 'SYSTEM',       -- DDM: GB
    dt_ult_alteracao    DATE,                               -- DDM: GC
    usr_ult_alteracao   VARCHAR(8),                         -- DDM: GD

    PRIMARY KEY (cod_programa)
);

-- S1: AA COD-PROGRAMA (DE) — primary key (already indexed as PK)
CREATE UNIQUE INDEX uq_programa_codigo ON programs.programa_social (cod_programa);

-- S2: AD TIPO-PROGRAMA + AI SIT-PROGRAMA — eligibility lookups (REQ-014)
CREATE INDEX idx_prog_s2_tipo_sit
    ON programs.programa_social (tipo_programa, sit_programa);

-- Partial index: active programs only (most frequent access pattern)
CREATE INDEX idx_prog_ativos
    ON programs.programa_social (cod_programa)
    WHERE sit_programa = 'A';

-- Table comment
COMMENT ON TABLE programs.programa_social IS
    'Social program catalog from SIFAP PROGRAMA-SOCIAL FNR 151. ~45 rows. '
    'Reference data; low mutation. Fator-K (fator_k) is pre-adjusted at inclusion '
    '(REQ-017, CADPROG). Discount types in programa_tipo_desconto (ADR-001). '
    'Regional factors in programa_param_regional (ADR-003). OQ-04: Fator-K origin TBD.';

COMMENT ON COLUMN programs.programa_social.fator_k IS
    'Stored pre-adjusted at inclusion (CADPROG.NSN#L93-L95, REQ-017). '
    'Origin of constant 0.347215 is OQ-04 (SENARC sign-off required). '
    'NEVER recalculate on existing rows — would double-apply the adjustment.';

-- ----------------------------------------------------------------------------
-- Child table: programa_tipo_desconto (MU EA — ADR-001)
-- @ElementCollection of TipoDesconto enum. Max 8 entries per program.
-- ----------------------------------------------------------------------------
CREATE TABLE programs.programa_tipo_desconto (
    cod_programa    CHAR(4)                 NOT NULL,
    tipo_desconto   programs.tipo_desconto  NOT NULL,

    PRIMARY KEY (cod_programa, tipo_desconto),

    FOREIGN KEY (cod_programa)
        REFERENCES programs.programa_social (cod_programa)
        ON DELETE CASCADE
);

CREATE INDEX idx_prog_dsct_codigo ON programs.programa_tipo_desconto (cod_programa);
CREATE INDEX idx_prog_dsct_tipo   ON programs.programa_tipo_desconto (tipo_desconto);

COMMENT ON TABLE programs.programa_tipo_desconto IS
    'Applicable discount types per program. Adabas MU TIPO-DSCT-APLIC (max 8). '
    'ADR-001: @ElementCollection enum. Enables REQ-025/026/027 per-type queries.';

-- ----------------------------------------------------------------------------
-- Child table: programa_faixa_calculo (PE DA — ADR-003)
-- @ElementCollection of FaixaCalculo. Max 5 entries per program.
-- ----------------------------------------------------------------------------
CREATE TABLE programs.programa_faixa_calculo (
    cod_programa            CHAR(4)         NOT NULL,
    ocorrencia_idx          SMALLINT        NOT NULL,   -- 0-based PE order

    renda_inicio            NUMERIC(9,2),   -- DDM: DB
    renda_fim               NUMERIC(9,2),   -- DDM: DC
    fator_multiplicador     NUMERIC(7,4),   -- DDM: DD (N 3.4)
    vlr_adicional           NUMERIC(9,2),   -- DDM: DE
    ind_acumulativo         CHAR(1) CHECK (ind_acumulativo IN ('S','N')),  -- DDM: DF

    PRIMARY KEY (cod_programa, ocorrencia_idx),

    FOREIGN KEY (cod_programa)
        REFERENCES programs.programa_social (cod_programa)
        ON DELETE CASCADE
);

CREATE INDEX idx_prog_faixa_codigo ON programs.programa_faixa_calculo (cod_programa);

-- ----------------------------------------------------------------------------
-- Child table: programa_param_regional (PE FA — ADR-003)
-- @ElementCollection of ParamRegional. Max 6 entries per program.
-- Externalizes the hardcoded 27-element #TAB-REG of CALCBENF.NSN (REQ-019).
-- ----------------------------------------------------------------------------
CREATE TABLE programs.programa_param_regional (
    cod_programa        CHAR(4)         NOT NULL,
    ocorrencia_idx      SMALLINT        NOT NULL,   -- 0-based PE order

    cod_regiao          CHAR(2),        -- DDM: FB (01-05 or 99)
    fator_regional      NUMERIC(7,4),   -- DDM: FC (multiplier, REQ-019)
    vlr_complemento_reg NUMERIC(9,2),   -- DDM: FD
    ind_ativo_regiao    CHAR(1) CHECK (ind_ativo_regiao IN ('S','N')),  -- DDM: FE

    PRIMARY KEY (cod_programa, ocorrencia_idx),

    FOREIGN KEY (cod_programa)
        REFERENCES programs.programa_social (cod_programa)
        ON DELETE CASCADE
);

CREATE INDEX idx_prog_reg_codigo ON programs.programa_param_regional (cod_programa);
CREATE INDEX idx_prog_reg_regiao ON programs.programa_param_regional (cod_regiao);

COMMENT ON TABLE programs.programa_param_regional IS
    'Regional calculation parameters per program. Adabas GRP-PARAM-REGIONAL PE (max 6). '
    'ADR-003: @ElementCollection, relational. Externalizes CALCBENF #TAB-REG hardcoded table '
    '(REQ-019). cod_regiao 99 factor is neutral (1.0000) — OQ-S1 eligibility bypass NOT here.';

-- ============================================================================
-- Rollback
--
-- DROP TABLE IF EXISTS programs.programa_param_regional;
-- DROP TABLE IF EXISTS programs.programa_faixa_calculo;
-- DROP TABLE IF EXISTS programs.programa_tipo_desconto;
-- DROP TABLE IF EXISTS programs.programa_social;
-- DROP TYPE IF EXISTS programs.tipo_desconto;
-- DROP TYPE IF EXISTS programs.situacao_programa;
-- DROP TYPE IF EXISTS programs.tipo_programa;
-- DROP SCHEMA IF EXISTS programs CASCADE;
-- ============================================================================

-- V1: Create the four bounded-context schemas and shared sequences.
-- source_legacy: N/A (greenfield schema setup for Modular Monolith)
-- Schemas correspond to bounded contexts (one schema per module, schema-per-context pattern).

CREATE SCHEMA IF NOT EXISTS beneficiaries;
CREATE SCHEMA IF NOT EXISTS programs;
CREATE SCHEMA IF NOT EXISTS payments;
CREATE SCHEMA IF NOT EXISTS audit;

-- Sequences for each aggregate root
CREATE SEQUENCE IF NOT EXISTS beneficiaries.beneficiario_num_seq
    START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS programs.programa_seq
    START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE IF NOT EXISTS payments.pagamento_num_seq
    START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS audit.auditoria_num_seq
    START WITH 1 INCREMENT BY 1;

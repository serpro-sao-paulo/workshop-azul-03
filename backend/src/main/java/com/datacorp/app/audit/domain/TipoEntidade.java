package com.datacorp.app.audit.domain;

/**
 * Type of entity that was acted upon.
 * Source: AUDITORIA.ddm field CA TIPO-ENTIDADE A 4.
 * Values: BENF=Beneficiário, PGTO=Pagamento, PROG=Programa Social,
 *         ADMN=Administração/Usuário, SIST=Sistema.
 */
public enum TipoEntidade {
    BENF, PGTO, PROG, ADMN, SIST
}

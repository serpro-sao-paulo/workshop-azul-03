package com.datacorp.app.audit.spi;

import com.datacorp.app.audit.domain.AcaoAuditoria;
import com.datacorp.app.audit.domain.TipoEntidade;

import java.time.Instant;

/** Immutable audit event DTO for cross-context use. */
public record AuditoriaEventoDto(
        Long numAuditoria,
        Instant tsEvento,
        AcaoAuditoria codAcao,
        TipoEntidade tipoEntidade,
        String idEntidade,
        String descricao,
        String usrEvento
) {}

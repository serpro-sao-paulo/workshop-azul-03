package com.datacorp.app.audit.spi;

import com.datacorp.app.audit.domain.TipoEntidade;

import java.util.List;

/**
 * SPI for querying audit data cross-context (if needed).
 * REQ-037: exclusions always visible.
 */
public interface AuditoriaQuery {
    List<AuditoriaEventoDto> findByEntidade(TipoEntidade tipo, String idEntidade);
    List<AuditoriaEventoDto> findAll();
}

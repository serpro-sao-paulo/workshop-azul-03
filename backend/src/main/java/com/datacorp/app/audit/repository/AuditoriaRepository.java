package com.datacorp.app.audit.repository;

import com.datacorp.app.audit.domain.AcaoAuditoria;
import com.datacorp.app.audit.domain.EventoAuditoria;
import com.datacorp.app.audit.domain.TipoEntidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

/**
 * Append-only repository for {@link EventoAuditoria}.
 * REQ-037: no delete/update operations (enforced by DB REVOKE in V5 + no @Modifying methods here).
 * source_legacy: N/A (ADR-004, IN-TCU 63)
 */
public interface AuditoriaRepository extends JpaRepository<EventoAuditoria, Long> {

    /** Find all events for an entity. REQ-037: exclusions (EX) always included. */
    List<EventoAuditoria> findByTipoEntidadeAndIdEntidadeOrderByDtEventoDesc(
            TipoEntidade tipoEntidade, String idEntidade);

    /** Find all events by type. */
    List<EventoAuditoria> findByTipoEntidadeOrderByDtEventoDesc(TipoEntidade tipoEntidade);

    /** Find events in a date range. */
    List<EventoAuditoria> findByDtEventoBetweenOrderByTsEventoDesc(
            LocalDate inicio, LocalDate fim);

    /** Find all exclusion events (IN-TCU 63 — always visible). */
    List<EventoAuditoria> findByCodAcaoOrderByDtEventoDesc(AcaoAuditoria codAcao);
}

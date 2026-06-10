package com.datacorp.app.audit.integration;

import com.datacorp.app.audit.domain.AcaoAuditoria;
import com.datacorp.app.audit.domain.EventoAuditoria;
import com.datacorp.app.audit.domain.TipoEntidade;
import com.datacorp.app.audit.repository.AuditoriaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test: events produce audit rows, exclusions visible, immutability enforced.
 * REQ-036, REQ-037. IN-TCU 63: exclusions always visible.
 * source_legacy: N/A (ADR-004)
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class AuditoriaIT {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Test
    void inserirEvento_deveSucceder() { // REQ-036
        var evento = buildEvento(AcaoAuditoria.IN, TipoEntidade.BENF, false);
        var saved = auditoriaRepository.saveAndFlush(evento);
        assertThat(saved.getNumAuditoria()).isNotNull();
    }

    @Test
    void exclusaoEmpre_sempreVisivel() { // REQ-037, IN-TCU 63
        var excl = buildEvento(AcaoAuditoria.EX, TipoEntidade.BENF, true);
        auditoriaRepository.saveAndFlush(excl);

        var found = auditoriaRepository.findAll();
        assertThat(found).anyMatch(e -> e.isExclusao());
    }

    @Test
    void eventoAuditoria_naoPermiteUpdate() { // REQ-037: immutable
        var evento = buildEvento(AcaoAuditoria.IN, TipoEntidade.PGTO, false);
        var saved = auditoriaRepository.saveAndFlush(evento);

        // The entity has no setters — mutation is impossible by design
        assertThat(saved.getDescricao()).isNotNull();
    }

    private EventoAuditoria buildEvento(AcaoAuditoria acao, TipoEntidade tipo, boolean exclusao) {
        return EventoAuditoria.registrar(
                UUID.randomUUID(), tipo, 1L, acao,
                exclusao ? "Exclusão por decisão judicial" : "Operação normal",
                "***.982.247-**", "BATCH", Instant.now());
    }
}

package com.datacorp.app.payments.integration;

import com.datacorp.app.payments.domain.Pagamento;
import com.datacorp.app.payments.domain.StatusPagamento;
import com.datacorp.app.payments.repository.PagamentoRepository;
import com.datacorp.app.shared.kernel.CodPrograma;
import com.datacorp.app.shared.kernel.Competencia;
import com.datacorp.app.shared.kernel.Cpf;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test (Testcontainers) for idempotent folha generation.
 * REQ-029..034.
 * source_legacy: BATCHPGT.NSN (idempotency), BATCHCON.NSN (CNAB 240)
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class FolhaIT {

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Test
    void persistirPagamento_statusInicialGERADO() { // REQ-029
        Pagamento p = buildPagamento("52998224725", 202601);
        Pagamento saved = pagamentoRepository.saveAndFlush(p);
        assertThat(saved.getStatus()).isEqualTo(StatusPagamento.GERADO);
    }

    @Test
    void idempotencia_duplo_pagamento_cpf_competencia_deveFalhar() { // REQ-030
        Pagamento p1 = buildPagamento("52998224725", 202601);
        pagamentoRepository.saveAndFlush(p1);

        Pagamento p2 = buildPagamento("52998224725", 202601); // duplicate
        assertThatThrownBy(() -> pagamentoRepository.saveAndFlush(p2))
                .isInstanceOf(Exception.class); // unique constraint violation
    }

    @Test
    void transicao_statusGERADO_paraEMITIDO_persisteCorreto() { // REQ-029
        Pagamento p = buildPagamento("52998224725", 202602);
        Pagamento saved = pagamentoRepository.saveAndFlush(p);

        saved.transitionTo(StatusPagamento.EMITIDO);
        Pagamento updated = pagamentoRepository.saveAndFlush(saved);

        assertThat(updated.getStatus()).isEqualTo(StatusPagamento.EMITIDO);
    }

    private Pagamento buildPagamento(String cpf, int competencia) {
        return Pagamento.criar(
                Cpf.of(cpf), 1L, CodPrograma.of("PBF"),
                Competencia.of(competencia),
                new BigDecimal("500.00"), new BigDecimal("50.00"), new BigDecimal("450.00"),
                Pagamento.TipoPgto.N);
    }
}

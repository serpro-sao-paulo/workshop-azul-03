package com.datacorp.app.payments;

import com.datacorp.app.payments.domain.StatusPagamento;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for StatusPagamento state machine.
 * ADR-002: OQ-01 resolution — ERRO ≠ ESTORNADO.
 * source_legacy: BATCHCON.NSN (cod-retorno '02' → ERRO), BATCHREL.NSN ('E' = ESTORNADO)
 */
class StatusPagamentoTest {

    @Test
    void transicao_GERADO_para_EMITIDO_deveSucceder() { // ADR-002
        assertThatNoException().isThrownBy(() ->
                StatusPagamento.GERADO.validateTransitionTo(StatusPagamento.EMITIDO));
    }

    @Test
    void transicao_EMITIDO_para_PAGO_deveSucceder() { // ADR-002
        assertThatNoException().isThrownBy(() ->
                StatusPagamento.EMITIDO.validateTransitionTo(StatusPagamento.PAGO));
    }

    @Test
    void transicao_EMITIDO_para_ERRO_deveSucceder() { // ADR-002, OQ-01
        assertThatNoException().isThrownBy(() ->
                StatusPagamento.EMITIDO.validateTransitionTo(StatusPagamento.ERRO));
    }

    @Test
    void transicao_EMITIDO_para_DEVOLVIDO_deveSucceder() { // ADR-002
        assertThatNoException().isThrownBy(() ->
                StatusPagamento.EMITIDO.validateTransitionTo(StatusPagamento.DEVOLVIDO));
    }

    @Test
    void transicao_PAGO_para_ESTORNADO_deveSucceder() { // ADR-002
        assertThatNoException().isThrownBy(() ->
                StatusPagamento.PAGO.validateTransitionTo(StatusPagamento.ESTORNADO));
    }

    @Test
    void transicao_ERRO_para_qualquerStatus_deveFalhar() { // ADR-002: ERRO é terminal
        assertThatThrownBy(() ->
                StatusPagamento.ERRO.validateTransitionTo(StatusPagamento.ESTORNADO))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void transicao_ESTORNADO_para_qualquerStatus_deveFalhar() { // ADR-002: ESTORNADO é terminal
        assertThatThrownBy(() ->
                StatusPagamento.ESTORNADO.validateTransitionTo(StatusPagamento.PAGO))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void erroNaoEquivalenteAEstornado() { // OQ-01: legacy 'E' collision resolved
        assertThat(StatusPagamento.ERRO).isNotEqualTo(StatusPagamento.ESTORNADO);
    }
}

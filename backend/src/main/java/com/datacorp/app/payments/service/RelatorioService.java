package com.datacorp.app.payments.service;

import com.datacorp.app.payments.domain.Pagamento;
import com.datacorp.app.payments.domain.StatusPagamento;
import com.datacorp.app.payments.repository.PagamentoRepository;
import com.datacorp.app.shared.kernel.Competencia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for consolidated payment report (REQ-035).
 * source_legacy: BATCHREL.NSN
 */
@Service
@Transactional(readOnly = true)
public class RelatorioService {

    private final PagamentoRepository pagamentoRepository;

    public RelatorioService(PagamentoRepository pagamentoRepository) {
        this.pagamentoRepository = pagamentoRepository;
    }

    /**
     * Generates a consolidated summary by status for a given competência.
     * REQ-035: macroregion + status breakdown.
     * source_legacy: BATCHREL.NSN consolidation output
     */
    public Map<StatusPagamento, Long> consolidadoPorStatus(int competenciaInt) {
        Competencia competencia = Competencia.of(competenciaInt);
        return java.util.Arrays.stream(StatusPagamento.values())
                .collect(Collectors.toMap(
                        status -> status,
                        status -> pagamentoRepository.countByStatusAndCompetencia(status, competencia)));
    }
}

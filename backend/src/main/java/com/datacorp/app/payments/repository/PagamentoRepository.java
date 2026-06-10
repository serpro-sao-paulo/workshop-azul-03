package com.datacorp.app.payments.repository;

import com.datacorp.app.payments.domain.Pagamento;
import com.datacorp.app.payments.domain.StatusPagamento;
import com.datacorp.app.shared.kernel.Competencia;
import com.datacorp.app.shared.kernel.Cpf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Pagamento}.
 * source_legacy: BATCHPGT.NSN (write), BATCHCON.NSN (update), BATCHREL.NSN (read)
 */
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    /** REQ-030: unique per CPF+competência (idempotency guard). */
    Optional<Pagamento> findByCpfValueAndCompetencia(String cpfRaw, Competencia competencia);

    /** REQ-035: report — all payments for a competência by program. */
    List<Pagamento> findByCodProgramaValueAndCompetencia(String codPrograma, Competencia competencia);

    /** REQ-031: folha — all GERADO payments for a program+competência. */
    List<Pagamento> findByStatusAndCodProgramaValueAndCompetencia(
            StatusPagamento status, String codPrograma, Competencia competencia);

    /** Count payments by status and competência. */
    long countByStatusAndCompetencia(StatusPagamento status, Competencia competencia);

    /** Find all payments for a beneficiary. */
    List<Pagamento> findByNumInscricaoOrderByCompetenciaDesc(Long numInscricao);
}

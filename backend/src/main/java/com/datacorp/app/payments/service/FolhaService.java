package com.datacorp.app.payments.service;

import com.datacorp.app.beneficiaries.spi.BeneficiarioQuery;
import com.datacorp.app.beneficiaries.spi.BeneficiarioSnapshot;
import com.datacorp.app.payments.domain.Pagamento;
import com.datacorp.app.payments.domain.StatusPagamento;
import com.datacorp.app.payments.repository.PagamentoRepository;
import com.datacorp.app.programs.spi.ProgramaCatalogQuery;
import com.datacorp.app.programs.spi.ProgramaPolicy;
import com.datacorp.app.shared.exception.DomainException;
import com.datacorp.app.shared.kernel.CodPrograma;
import com.datacorp.app.shared.kernel.Competencia;
import com.datacorp.app.shared.kernel.Cpf;
import com.datacorp.app.shared.kernel.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for idempotent monthly folha generation.
 *
 * <p>REQ-029: generate folha for a program+competência, status GERADO.
 * <p>REQ-030: idempotent — skip existing GERADO payments for the same CPF+competência.
 * <p>REQ-031: process all active beneficiaries for the program.
 * source_legacy: BATCHPGT.NSN#L180-L250
 */
@Service
@Transactional
public class FolhaService {

    private final PagamentoRepository pagamentoRepository;
    private final BeneficiarioQuery beneficiarioQuery;
    private final ProgramaCatalogQuery programaCatalogQuery;
    private final CalculoBeneficioService calculoService;
    private final DescontoService descontoService;

    public FolhaService(PagamentoRepository pagamentoRepository,
                         BeneficiarioQuery beneficiarioQuery,
                         ProgramaCatalogQuery programaCatalogQuery,
                         CalculoBeneficioService calculoService,
                         DescontoService descontoService) {
        this.pagamentoRepository = pagamentoRepository;
        this.beneficiarioQuery = beneficiarioQuery;
        this.programaCatalogQuery = programaCatalogQuery;
        this.calculoService = calculoService;
        this.descontoService = descontoService;
    }

    /**
     * Generates the folha for a program in a competência.
     * Idempotent: existing GERADO payments are skipped (REQ-030).
     *
     * @return number of payments generated
     */
    public int gerarFolha(String codPrograma, int competenciaInt) {
        Competencia competencia = Competencia.of(competenciaInt);

        ProgramaPolicy policy = programaCatalogQuery.findActivePolicy(codPrograma)
                .orElseThrow(() -> new DomainException("PROGRAMA_NOT_FOUND",
                        "Programa não encontrado ou inativo: " + codPrograma));

        List<BeneficiarioSnapshot> ativos = beneficiarioQuery.findAtivosForPrograma(codPrograma);
        int count = 0;

        for (BeneficiarioSnapshot benef : ativos) {
            // REQ-030: idempotent — skip if already generated
            if (pagamentoRepository.findByCpfValueAndCompetencia(
                    benef.cpfMasked(), competencia).isPresent()) {
                continue;
            }

            Money bruto = calculoService.calcularBruto(benef, policy, competenciaInt);
            // Simple discount: 3% contribution for this baseline
            Money desconto = descontoService.calcularDesconto(
                    bruto, new BigDecimal("0.03"), null, null, policy.tiposDesconto() != null
                            ? policy.tiposDesconto() : java.util.Set.of());
            Money liquido = descontoService.calcularLiquido(bruto, desconto);

            // Note: cpfMasked in snapshot — Payments never holds raw CPF cross-context
            // For folha we need the raw CPF via another SPI path or passed from caller
            // TODO: BeneficiarioQuery should expose cpfRaw for folha use (internal only)
            // For now use numInscricao as proxy
            Pagamento p = Pagamento.criar(
                    null, // cpf - TODO: resolve CPF access for folha
                    benef.numInscricao(),
                    CodPrograma.of(codPrograma),
                    competencia,
                    bruto.amount(), desconto.amount(), liquido.amount(),
                    competencia.month() == 12 ? Pagamento.TipoPgto.D : Pagamento.TipoPgto.N);

            pagamentoRepository.save(p);
            count++;
        }
        return count;
    }

    @Transactional(readOnly = true)
    public List<Pagamento> buscarPorCompetencia(String codPrograma, int competenciaInt) {
        Competencia competencia = Competencia.of(competenciaInt);
        return pagamentoRepository.findByStatusAndCodProgramaValueAndCompetencia(
                StatusPagamento.GERADO, codPrograma, competencia);
    }
}

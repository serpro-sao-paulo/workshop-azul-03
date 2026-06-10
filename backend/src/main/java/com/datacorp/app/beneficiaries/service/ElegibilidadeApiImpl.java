package com.datacorp.app.beneficiaries.service;

import com.datacorp.app.beneficiaries.domain.Beneficiario;
import com.datacorp.app.beneficiaries.repository.BeneficiarioRepository;
import com.datacorp.app.beneficiaries.spi.ElegibilidadeApi;
import com.datacorp.app.beneficiaries.spi.ResultadoElegibilidade;
import com.datacorp.app.shared.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements {@link ElegibilidadeApi} SPI.
 */
@Service
@Transactional(readOnly = true)
class ElegibilidadeApiImpl implements ElegibilidadeApi {

    private final BeneficiarioRepository repo;
    private final ElegibilidadeService elegibilidadeService;

    ElegibilidadeApiImpl(BeneficiarioRepository repo, ElegibilidadeService elegibilidadeService) {
        this.repo = repo;
        this.elegibilidadeService = elegibilidadeService;
    }

    @Override
    public ResultadoElegibilidade avaliarElegibilidade(Long numInscricao, String codPrograma) {
        Beneficiario b = repo.findById(numInscricao)
                .orElseThrow(() -> new DomainException("BENEFICIARIO_NOT_FOUND",
                        "Beneficiário não encontrado: " + numInscricao));
        return elegibilidadeService.avaliar(b, codPrograma);
    }
}

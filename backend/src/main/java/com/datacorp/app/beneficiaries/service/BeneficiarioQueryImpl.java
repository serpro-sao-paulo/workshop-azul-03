package com.datacorp.app.beneficiaries.service;

import com.datacorp.app.beneficiaries.domain.Beneficiario;
import com.datacorp.app.beneficiaries.domain.StatusBeneficiario;
import com.datacorp.app.beneficiaries.repository.BeneficiarioRepository;
import com.datacorp.app.beneficiaries.spi.BeneficiarioQuery;
import com.datacorp.app.beneficiaries.spi.BeneficiarioSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implements {@link BeneficiarioQuery} SPI. Only this class and the returned
 * snapshot record may cross the Beneficiaries module boundary (Principle IV).
 * CPF is always masked in the snapshot (REQ-015).
 */
@Service
@Transactional(readOnly = true)
class BeneficiarioQueryImpl implements BeneficiarioQuery {

    private final BeneficiarioRepository repo;

    BeneficiarioQueryImpl(BeneficiarioRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<BeneficiarioSnapshot> findByInscricao(Long numInscricao) {
        return repo.findById(numInscricao).map(this::toSnapshot);
    }

    @Override
    public Optional<BeneficiarioSnapshot> findByCpf(String cpfRaw) {
        return repo.findByCpfValue(cpfRaw).map(this::toSnapshot);
    }

    @Override
    public List<BeneficiarioSnapshot> findAtivosForPrograma(String codPrograma) {
        return repo.findByCodProgramaValueAndStatus(codPrograma, StatusBeneficiario.A)
                .stream()
                .map(this::toSnapshot)
                .toList();
    }

    private BeneficiarioSnapshot toSnapshot(Beneficiario b) {
        String uf = b.getEndereco() != null ? b.getEndereco().getUf() : null;
        String cod = b.getCodPrograma() != null ? b.getCodPrograma().value() : null;
        return new BeneficiarioSnapshot(
                b.getNumInscricao(),
                b.getCpf().masked(), // REQ-015: always masked
                b.getNomeCompleto(),
                b.getStatus(),
                b.getDtNascimento(),
                uf,
                cod,
                b.getVlrRendaFamiliar(),
                b.getQtdMembros(),
                b.getIndRendaPercap(),
                b.numDependentes()
        );
    }
}

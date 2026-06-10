package com.datacorp.app.beneficiaries.repository;

import com.datacorp.app.beneficiaries.domain.Beneficiario;
import com.datacorp.app.beneficiaries.domain.StatusBeneficiario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Beneficiario}.
 * source_legacy: CONSBENEF.NSN (read), CADBENEF.NSN (write)
 */
public interface BeneficiarioRepository extends JpaRepository<Beneficiario, Long> {

    /** Find by CPF raw value. REQ-005 duplicate check + REQ-015 mask in service layer. */
    Optional<Beneficiario> findByCpfValue(String cpfRaw);

    /** Check CPF existence (duplicate guard, REQ-005). */
    boolean existsByCpfValue(String cpfRaw);

    /** Find all beneficiaries in a program with a given status (batch/eligibility, REQ-014). */
    List<Beneficiario> findByCodProgramaValueAndStatus(String codPrograma, StatusBeneficiario status);

    /** Find active beneficiaries in a program (REQ-031 monthly folha). */
    @Query("SELECT b FROM Beneficiario b WHERE b.codPrograma.value = :cod AND b.status = 'A'")
    List<Beneficiario> findAtivosParaFolha(@Param("cod") String codPrograma);
}

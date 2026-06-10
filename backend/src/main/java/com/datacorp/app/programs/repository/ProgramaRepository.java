package com.datacorp.app.programs.repository;

import com.datacorp.app.programs.domain.ProgramaSocial;
import com.datacorp.app.programs.domain.SituacaoPrograma;
import com.datacorp.app.programs.domain.TipoPrograma;
import com.datacorp.app.shared.kernel.CodPrograma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for {@link ProgramaSocial}.
 * source_legacy: CONSPROG.NSN (read), CADPROG.NSN (write)
 */
public interface ProgramaRepository extends JpaRepository<ProgramaSocial, CodPrograma> {

    /** Find all programs by type and status (eligibility filter, REQ-014). */
    List<ProgramaSocial> findByTipoProgramaAndSituacao(TipoPrograma tipo, SituacaoPrograma situacao);

    /** Find all active programs. */
    List<ProgramaSocial> findBySituacao(SituacaoPrograma situacao);

    /** Check existence by code (duplicate guard, REQ-016). */
    boolean existsByCodigoValue(String codPrograma);
}

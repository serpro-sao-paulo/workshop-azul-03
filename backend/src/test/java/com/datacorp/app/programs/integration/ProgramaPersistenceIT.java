package com.datacorp.app.programs.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.datacorp.app.programs.repository.ProgramaRepository;
import com.datacorp.app.programs.domain.ProgramaSocial;
import com.datacorp.app.programs.domain.TipoPrograma;
import com.datacorp.app.programs.domain.SituacaoPrograma;
import com.datacorp.app.shared.kernel.CodPrograma;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Testcontainers integration test for program uniqueness.
 * REQ-016: único código de programa (409 on duplicate).
 * source_legacy: CADPROG.NSN#uniqueness-check
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ProgramaPersistenceIT {

    @Autowired
    private ProgramaRepository programaRepository;

    @Test
    void persistirPrograma_comCodigoDuplicado_deveLancarExcecao() { // REQ-016
        ProgramaSocial p1 = buildPrograma("UNQ1");
        programaRepository.saveAndFlush(p1);

        ProgramaSocial p2 = buildPrograma("UNQ1"); // same code
        assertThatThrownBy(() -> programaRepository.saveAndFlush(p2))
                .isInstanceOf(Exception.class); // DB unique constraint violation
    }

    @Test
    void persistirPrograma_comCodigoDiferente_deveSucceder() { // REQ-016
        ProgramaSocial p1 = buildPrograma("UN1A");
        ProgramaSocial p2 = buildPrograma("UN1B");

        programaRepository.saveAndFlush(p1);
        programaRepository.saveAndFlush(p2);

        assertThat(programaRepository.count()).isGreaterThanOrEqualTo(2);
    }

    private ProgramaSocial buildPrograma(String cod) {
        // Use package-private or builder approach per entity design
        ProgramaSocial p = new ProgramaSocial();
        p.setCodigo(CodPrograma.of(cod));
        p.setNomePrograma("Prog " + cod);
        p.setSiglaPrograma(cod);
        p.setTipoPrograma(TipoPrograma.A);
        p.setSituacao(SituacaoPrograma.A);
        p.setVlrBase(new BigDecimal("500.00"));
        p.setVlrMaximo(new BigDecimal("1000.00"));
        p.setVlrMinimo(new BigDecimal("100.00"));
        p.setFatorK(new BigDecimal("0.347215"));
        p.setDtVigencia(LocalDate.now());
        return p;
    }
}

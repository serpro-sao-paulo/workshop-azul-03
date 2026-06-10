package com.datacorp.app.beneficiaries;

import com.datacorp.app.beneficiaries.domain.*;
import com.datacorp.app.beneficiaries.service.ElegibilidadeService;
import com.datacorp.app.programs.domain.TipoPrograma;
import com.datacorp.app.programs.spi.ProgramaCatalogQuery;
import com.datacorp.app.programs.spi.ProgramaPolicy;
import com.datacorp.app.shared.kernel.Cpf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for eligibility decisions.
 * REQ-012: beneficiary status must be A.
 * REQ-013: income/age/type filters.
 * REQ-014: only active programs.
 * source_legacy: VALELEG.NSN#L74-L90
 */
@ExtendWith(MockitoExtension.class)
class ElegibilidadeServiceTest {

    @Mock
    private ProgramaCatalogQuery programaCatalogQuery;

    @InjectMocks
    private ElegibilidadeService elegibilidadeService;

    private Beneficiario beneficiarioAtivo;
    private Beneficiario beneficiarioSuspenso;

    @BeforeEach
    void setup() {
        beneficiarioAtivo = Beneficiario.criar(
                Cpf.of("529.982.247-25"), "João da Silva", "Maria",
                LocalDate.of(1985, 1, 1), Sexo.M, EstadoCivil.S,
                new Endereco("Rua", "1", null, "Centro", "SP", "SP", "01001000", null, "SP"),
                null, BigDecimal.valueOf(1200), 3);

        beneficiarioSuspenso = Beneficiario.criar(
                Cpf.of("529.982.247-25"), "Maria da Silva", "Ana",
                LocalDate.of(1985, 1, 1), Sexo.F, EstadoCivil.S,
                new Endereco("Rua", "2", null, "Centro", "SP", "SP", "01001000", null, "SP"),
                null, BigDecimal.valueOf(800), 2);
        beneficiarioSuspenso.alterarStatus(StatusBeneficiario.S, "TEST");
    }

    @Test
    void elegibilidade_comStatusSuspenso_deveSerInelegivel() { // REQ-012
        var result = elegibilidadeService.avaliar(beneficiarioSuspenso, "PBF");
        assertThat(result.elegivel()).isFalse();
        assertThat(result.motivo()).containsIgnoringCase("status");
    }

    @Test
    void elegibilidade_comProgramaInexistente_deveSerInelegivel() { // REQ-014
        when(programaCatalogQuery.findActivePolicy("XXXX")).thenReturn(Optional.empty());

        var result = elegibilidadeService.avaliar(beneficiarioAtivo, "XXXX");
        assertThat(result.elegivel()).isFalse();
    }

    @Test
    void elegibilidade_comRendaAcimaLimite_deveSerInelegivel() { // REQ-013
        var policy = new ProgramaPolicy("PBF", "PBF", TipoPrograma.A, true,
                BigDecimal.valueOf(500), BigDecimal.valueOf(0.347215),
                BigDecimal.valueOf(1000), BigDecimal.valueOf(100),
                BigDecimal.valueOf(300), // max renda percap = 300
                0, 0, Set.of());
        when(programaCatalogQuery.findActivePolicy("PBF")).thenReturn(Optional.of(policy));

        // Beneficiario has rendaPercap = 1200/3 = 400 > 300 limit
        var result = elegibilidadeService.avaliar(beneficiarioAtivo, "PBF");
        assertThat(result.elegivel()).isFalse();
        assertThat(result.motivo()).containsIgnoringCase("renda");
    }

    @Test
    void elegibilidade_comCriteriosAtendidos_deveSerElegivel() { // REQ-012..014
        var policy = new ProgramaPolicy("PBF", "PBF", TipoPrograma.A, true,
                BigDecimal.valueOf(500), BigDecimal.valueOf(0.347215),
                BigDecimal.valueOf(1000), BigDecimal.valueOf(100),
                BigDecimal.valueOf(500), // max renda percap = 500
                0, 0, Set.of());
        when(programaCatalogQuery.findActivePolicy("PBF")).thenReturn(Optional.of(policy));

        // rendaPercap = 1200/3 = 400 ≤ 500 → eligible
        var result = elegibilidadeService.avaliar(beneficiarioAtivo, "PBF");
        assertThat(result.elegivel()).isTrue();
    }
}

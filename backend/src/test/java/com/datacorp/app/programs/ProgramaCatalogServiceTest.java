package com.datacorp.app.programs;

import com.datacorp.app.programs.domain.ProgramaSocial;
import com.datacorp.app.programs.domain.TipoPrograma;
import com.datacorp.app.programs.domain.SituacaoPrograma;
import com.datacorp.app.programs.repository.ProgramaRepository;
import com.datacorp.app.shared.kernel.CodPrograma;
import com.datacorp.app.shared.kernel.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Fator-K adjustment on program inclusion.
 * REQ-017: vlrBase persisted = vlrBase × (1 + fatorK).
 * source_legacy: CADPROG.NSN (Fator-K adjustment logic, OQ-04 origin unresolved)
 */
@ExtendWith(MockitoExtension.class)
class ProgramaCatalogServiceTest {

    @Mock
    private ProgramaRepository programaRepository;

    @InjectMocks
    private com.datacorp.app.programs.service.ProgramaCatalogService catalogService;

    @Test
    void incluirPrograma_deveAjustarVlrBaseComFatorK() { // REQ-017
        when(programaRepository.findById(any())).thenReturn(Optional.empty());
        when(programaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // vlrBase = 1000.00, fatorK = 0.347215
        // adjustedVlrBase = 1000.00 * (1 + 0.347215) = 1347.21 (truncated)
        var cmd = new com.datacorp.app.programs.service.IncluirProgramaCommand(
                CodPrograma.of("TST2"),
                "Programa Teste",
                "TST2",
                TipoPrograma.A,
                SituacaoPrograma.ATIVO,
                Money.of(new BigDecimal("1000.00")),
                Money.of(new BigDecimal("2000.00")),
                Money.of(new BigDecimal("500.00")),
                new BigDecimal("0.347215"),
                java.util.Set.of()
        );

        ProgramaSocial saved = catalogService.incluir(cmd);

        // vlrBase should be adjusted: 1000 * 1.347215 = 1347.215 → truncated 1347.21
        assertThat(saved.getVlrBase()).isEqualByComparingTo(new BigDecimal("1347.21"));
    }

    @Test
    void incluirPrograma_comCodDuplicado_deveLancarExcecao() { // REQ-016
        when(programaRepository.findById(CodPrograma.of("PBF")))
                .thenReturn(Optional.of(mock(ProgramaSocial.class)));

        var cmd = new com.datacorp.app.programs.service.IncluirProgramaCommand(
                CodPrograma.of("PBF"),
                "PBF Duplicado", "PBF", TipoPrograma.A, SituacaoPrograma.A,
                Money.of(new BigDecimal("1000.00")),
                Money.of(new BigDecimal("2000.00")),
                Money.of(new BigDecimal("500.00")),
                new BigDecimal("0.347215"),
                java.util.Set.of()
        );

        assertThatThrownBy(() -> catalogService.incluir(cmd))
                .isInstanceOf(com.datacorp.app.shared.exception.DomainException.class)
                .hasMessageContaining("PROGRAMA_CONFLICT");
    }
}

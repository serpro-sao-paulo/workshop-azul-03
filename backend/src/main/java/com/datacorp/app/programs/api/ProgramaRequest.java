package com.datacorp.app.programs.api;

import com.datacorp.app.programs.domain.SituacaoPrograma;
import com.datacorp.app.programs.domain.TipoDesconto;
import com.datacorp.app.programs.domain.TipoPrograma;
import com.datacorp.app.shared.kernel.Money;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Set;

/** Request DTO for program creation. REQ-016, REQ-017. */
public record ProgramaRequest(
        @NotBlank @Size(max = 4) String codPrograma,
        @NotBlank @Size(max = 60) String nomePrograma,
        @Size(max = 10) String siglaPrograma,
        @NotNull TipoPrograma tipoPrograma,
        @NotNull SituacaoPrograma sitPrograma,
        @NotNull BigDecimal vlrBase,
        BigDecimal vlrMaximo,
        BigDecimal vlrMinimo,
        BigDecimal fatorK,
        Set<TipoDesconto> tiposDesconto
) {}

package com.datacorp.app.programs.service;

import com.datacorp.app.programs.domain.SituacaoPrograma;
import com.datacorp.app.programs.domain.TipoDesconto;
import com.datacorp.app.programs.domain.TipoPrograma;
import com.datacorp.app.shared.kernel.CodPrograma;
import com.datacorp.app.shared.kernel.Money;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Command record for including a new programa social.
 * REQ-016, REQ-017.
 */
public record IncluirProgramaCommand(
        CodPrograma codPrograma,
        String nomePrograma,
        String siglaPrograma,
        TipoPrograma tipoPrograma,
        SituacaoPrograma sitPrograma,
        Money vlrBase,
        Money vlrMaximo,
        Money vlrMinimo,
        BigDecimal fatorK,
        Set<TipoDesconto> tiposDesconto
) {}

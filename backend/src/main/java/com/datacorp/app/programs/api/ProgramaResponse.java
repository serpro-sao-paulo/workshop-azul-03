package com.datacorp.app.programs.api;

import com.datacorp.app.programs.domain.ProgramaSocial;
import com.datacorp.app.programs.domain.SituacaoPrograma;
import com.datacorp.app.programs.domain.TipoDesconto;
import com.datacorp.app.programs.domain.TipoPrograma;

import java.math.BigDecimal;
import java.util.Set;

/** Response DTO for programa social. REQ-016, REQ-017. */
public record ProgramaResponse(
        String codPrograma,
        String nomePrograma,
        String siglaPrograma,
        TipoPrograma tipoPrograma,
        SituacaoPrograma sitPrograma,
        BigDecimal vlrBase,      // already Fator-K adjusted (REQ-017)
        BigDecimal vlrMaximo,
        BigDecimal vlrMinimo,
        BigDecimal fatorK,
        Set<TipoDesconto> tiposDesconto
) {
    public static ProgramaResponse from(ProgramaSocial p) {
        return new ProgramaResponse(
                p.getCodigo().value(),
                p.getNomePrograma(),
                p.getSiglaPrograma(),
                p.getTipoPrograma(),
                p.getSituacao(),
                p.getVlrBase(),
                p.getVlrTetoBenef(),
                p.getVlrPisoBenef(),
                p.getFatorK(),
                p.getDescontosAplicaveis()
        );
    }
}

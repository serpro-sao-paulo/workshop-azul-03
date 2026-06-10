package com.datacorp.app.audit.api;

import com.datacorp.app.audit.domain.EventoAuditoria;
import com.datacorp.app.audit.domain.TipoEntidade;
import com.datacorp.app.audit.repository.AuditoriaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for Audit trail.
 * REQ-036, REQ-037: exclusions always visible (IN-TCU 63).
 * source_legacy: N/A (ADR-004)
 */
@RestController
@RequestMapping("/api/v1/auditoria")
@Tag(name = "Auditoria", description = "Trilha de Auditoria & Conformidade")
public class AuditoriaController {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaController(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @GetMapping
    @Operation(summary = "Lista eventos de auditoria (exclusões sempre visíveis)")
    public ResponseEntity<List<Map<String, Object>>> listar(
            @RequestParam(required = false) String tipoEntidade) {

        List<EventoAuditoria> eventos;
        if (tipoEntidade != null) {
            try {
                TipoEntidade tipo = TipoEntidade.valueOf(tipoEntidade.toUpperCase());
                eventos = auditoriaRepository.findByTipoEntidadeOrderByDtEventoDesc(tipo);
            } catch (IllegalArgumentException e) {
                eventos = List.of();
            }
        } else {
            // REQ-037: no filter on AcaoAuditoria.EX — exclusions always visible
            eventos = auditoriaRepository.findAll();
        }

        return ResponseEntity.ok(eventos.stream()
                .map(e -> Map.of(
                        "numAuditoria", e.getNumAuditoria(),
                        "dtEvento", e.getDtEvento() != null ? e.getDtEvento().toString() : "",
                        "codAcao", e.getCodAcao(),
                        "tipoEntidade", e.getTipoEntidade() != null ? e.getTipoEntidade() : "",
                        "idEntidade", e.getIdEntidade() != null ? e.getIdEntidade() : "",
                        "descricao", e.getDescricao() != null ? e.getDescricao() : "",
                        "usrEvento", e.getUsrEvento() != null ? e.getUsrEvento() : ""
                ))
                .collect(Collectors.toList()));
    }
}

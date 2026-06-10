package com.datacorp.app.programs.api;

import com.datacorp.app.programs.domain.ProgramaSocial;
import com.datacorp.app.programs.service.IncluirProgramaCommand;
import com.datacorp.app.programs.service.ProgramaCatalogService;
import com.datacorp.app.shared.exception.DomainException;
import com.datacorp.app.shared.kernel.CodPrograma;
import com.datacorp.app.shared.kernel.Money;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Catálogo de Programas Sociais.
 * REQ-016: POST /api/v1/programas — unicidade do código.
 * REQ-017: GET /api/v1/programas/{cod} — vlrBase já ajustado pelo Fator-K.
 * source_legacy: CADPROG.NSN, CONSPROG.NSN
 */
@RestController
@RequestMapping("/api/v1/programas")
@Tag(name = "Programas", description = "Catálogo de Programas Sociais")
public class ProgramaController {

    private final ProgramaCatalogService catalogService;

    public ProgramaController(ProgramaCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @PostMapping
    @Operation(summary = "Inclui programa social (vlrBase ajustado por Fator-K)")
    @ApiResponse(responseCode = "201", description = "Programa criado")
    @ApiResponse(responseCode = "409", description = "Código duplicado")
    @ApiResponse(responseCode = "422", description = "Validação")
    public ResponseEntity<ProgramaResponse> incluir(@Valid @RequestBody ProgramaRequest req) {
        Money vlrBase = Money.ofStrict(req.vlrBase());
        Money vlrMax = req.vlrMaximo() != null ? Money.ofStrict(req.vlrMaximo()) : Money.zero();
        Money vlrMin = req.vlrMinimo() != null ? Money.ofStrict(req.vlrMinimo()) : Money.zero();

        var cmd = new IncluirProgramaCommand(
                CodPrograma.of(req.codPrograma()),
                req.nomePrograma(),
                req.siglaPrograma(),
                req.tipoPrograma(),
                req.sitPrograma(),
                vlrBase, vlrMax, vlrMin,
                req.fatorK(),
                req.tiposDesconto() != null ? req.tiposDesconto() : java.util.Set.of()
        );
        ProgramaSocial saved = catalogService.incluir(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProgramaResponse.from(saved));
    }

    @GetMapping("/{cod}")
    @Operation(summary = "Consulta programa por código")
    @ApiResponse(responseCode = "200", description = "Programa encontrado")
    @ApiResponse(responseCode = "404", description = "Programa não encontrado")
    public ResponseEntity<ProgramaResponse> buscar(@PathVariable String cod) {
        return catalogService.buscarPorCodigo(CodPrograma.of(cod))
                .map(p -> ResponseEntity.ok(ProgramaResponse.from(p)))
                .orElseThrow(() -> new DomainException("PROGRAMA_NOT_FOUND",
                        "Programa não encontrado: " + cod));
    }

    @GetMapping
    @Operation(summary = "Lista programas ativos")
    public ResponseEntity<List<ProgramaResponse>> listar() {
        List<ProgramaResponse> list = catalogService.listarAtivos().stream()
                .map(ProgramaResponse::from)
                .toList();
        return ResponseEntity.ok(list);
    }
}

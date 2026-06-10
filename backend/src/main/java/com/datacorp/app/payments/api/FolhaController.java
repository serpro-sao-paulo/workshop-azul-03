package com.datacorp.app.payments.api;

import com.datacorp.app.payments.service.ConciliacaoService;
import com.datacorp.app.payments.service.CorrecaoService;
import com.datacorp.app.payments.service.FolhaService;
import com.datacorp.app.payments.service.RelatorioService;
import com.datacorp.app.shared.exception.DomainException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Controllers for Payments & Folha.
 * REQ-029..035. @Valid at every boundary (Principle V).
 * source_legacy: BATCHPGT.NSN, BATCHCON.NSN, BATCHREL.NSN
 */
@RestController
@Tag(name = "Pagamentos", description = "Folha, Conciliação, Correção e Relatórios")
public class FolhaController {

    private final FolhaService folhaService;
    private final ConciliacaoService conciliacaoService;
    private final CorrecaoService correcaoService;
    private final RelatorioService relatorioService;

    public FolhaController(FolhaService folhaService,
                            ConciliacaoService conciliacaoService,
                            CorrecaoService correcaoService,
                            RelatorioService relatorioService) {
        this.folhaService = folhaService;
        this.conciliacaoService = conciliacaoService;
        this.correcaoService = correcaoService;
        this.relatorioService = relatorioService;
    }

    // ── Folha ─────────────────────────────────────────────────────────────────

    @PostMapping("/api/v1/folhas")
    @Operation(summary = "Gera folha de pagamento (idempotente)")
    public ResponseEntity<Map<String, Object>> gerarFolha(
            @Valid @RequestBody FolhaRequest req) {
        int count = folhaService.gerarFolha(req.codPrograma(), req.competencia());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("pagamentosGerados", count, "competencia", req.competencia()));
    }

    @GetMapping("/api/v1/folhas/{competencia}")
    @Operation(summary = "Consulta folha por competência")
    public ResponseEntity<Map<String, Object>> consultarFolha(@PathVariable int competencia) {
        var pagamentos = folhaService.buscarPorCompetencia("PBF", competencia);
        if (pagamentos.isEmpty()) {
            throw new DomainException("FOLHA_NOT_FOUND", "Folha não encontrada: " + competencia);
        }
        return ResponseEntity.ok(Map.of("competencia", competencia, "count", pagamentos.size()));
    }

    // ── Conciliação ───────────────────────────────────────────────────────────

    @PostMapping("/api/v1/conciliacoes/{pagamentoId}")
    @Operation(summary = "Processa retorno bancário CNAB 240")
    public ResponseEntity<Void> conciliar(@PathVariable Long pagamentoId,
                                           @RequestParam String codRetorno) {
        conciliacaoService.processarRetorno(pagamentoId, codRetorno);
        return ResponseEntity.ok().build();
    }

    // ── Correção ──────────────────────────────────────────────────────────────

    @PostMapping("/api/v1/correcoes/{pagamentoId}")
    @Operation(summary = "Aplica correção monetária IPCA (idempotente)")
    public ResponseEntity<Void> aplicarCorrecao(@PathVariable Long pagamentoId,
                                                 @RequestParam BigDecimal indiceIPCA) {
        correcaoService.aplicarCorrecao(pagamentoId, indiceIPCA);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/v1/estornos/{pagamentoId}")
    @Operation(summary = "Estorna pagamento")
    public ResponseEntity<Void> estornar(@PathVariable Long pagamentoId,
                                          @RequestParam String motivo) {
        correcaoService.estornar(pagamentoId, motivo);
        return ResponseEntity.ok().build();
    }

    // ── Relatório ─────────────────────────────────────────────────────────────

    @GetMapping("/api/v1/relatorios/consolidado")
    @Operation(summary = "Relatório consolidado por status e competência")
    public ResponseEntity<Map<String, Object>> relatorioConsolidado(
            @RequestParam int competencia) {
        var resumo = relatorioService.consolidadoPorStatus(competencia);
        return ResponseEntity.ok(Map.of("competencia", competencia, "resumo", resumo));
    }

    /** Request record for folha generation. */
    public record FolhaRequest(
            @NotNull String codPrograma,
            @NotNull Integer competencia
    ) {}
}

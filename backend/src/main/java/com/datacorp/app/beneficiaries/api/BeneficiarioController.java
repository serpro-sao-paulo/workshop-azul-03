package com.datacorp.app.beneficiaries.api;

import com.datacorp.app.beneficiaries.domain.*;
import com.datacorp.app.beneficiaries.service.CadastroService;
import com.datacorp.app.beneficiaries.service.DependenteService;
import com.datacorp.app.beneficiaries.service.ElegibilidadeService;
import com.datacorp.app.shared.exception.DomainException;
import com.datacorp.app.shared.kernel.Cpf;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * REST controller for Gestão de Beneficiários.
 * REQ-001..015. @Valid at every boundary (Principle V).
 * CPF always masked in responses (REQ-015).
 * source_legacy: CADBENEF.NSN, CONSBENEF.NSN, CADDEPEND.NSN, VALELEG.NSN
 */
@RestController
@RequestMapping("/api/v1/beneficiarios")
@Tag(name = "Beneficiários", description = "Gestão de Beneficiários")
public class BeneficiarioController {

    private final CadastroService cadastroService;
    private final DependenteService dependenteService;
    private final ElegibilidadeService elegibilidadeService;

    public BeneficiarioController(CadastroService cadastroService,
                                   DependenteService dependenteService,
                                   ElegibilidadeService elegibilidadeService) {
        this.cadastroService = cadastroService;
        this.dependenteService = dependenteService;
        this.elegibilidadeService = elegibilidadeService;
    }

    @PostMapping
    @Operation(summary = "Cadastra beneficiário com validação acumulativa")
    @ApiResponse(responseCode = "201", description = "Beneficiário cadastrado")
    @ApiResponse(responseCode = "409", description = "CPF duplicado")
    @ApiResponse(responseCode = "422", description = "Erros de validação")
    public ResponseEntity<Map<String, Object>> incluir(@Valid @RequestBody BeneficiarioRequest req) {
        var endDto = req.endereco();
        var endereco = new Endereco(endDto.logradouro(), endDto.numero(), endDto.complemento(),
                endDto.bairro(), endDto.municipio(), endDto.uf(), endDto.cep(),
                endDto.codIbge(), endDto.codRegiao());

        var b = cadastroService.incluir(req.cpf(), req.nomeCompleto(), req.nomeMae(),
                req.dtNascimento(), req.sexo(), req.estCivil(), endereco,
                req.codPrograma(), req.vlrRendaFamiliar(), req.qtdMembros());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "numInscricao", b.getNumInscricao(),
                "cpf", b.getCpf().masked(), // REQ-015
                "status", b.getStatus()
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta beneficiário (CPF mascarado)")
    @ApiResponse(responseCode = "200", description = "Beneficiário encontrado")
    @ApiResponse(responseCode = "404", description = "Não encontrado")
    public ResponseEntity<Map<String, Object>> buscar(@PathVariable Long id) {
        return cadastroService.buscar(id)
                .map(b -> ResponseEntity.ok(Map.of(
                        "numInscricao", b.getNumInscricao(),
                        "cpf", b.getCpf().masked(), // REQ-015
                        "nomeCompleto", b.getNomeCompleto(),
                        "status", b.getStatus(),
                        "numDependentes", b.numDependentes()
                )))
                .orElseThrow(() -> new DomainException("BENEFICIARIO_NOT_FOUND",
                        "Beneficiário não encontrado: " + id));
    }

    @GetMapping("/{id}/elegibilidade/{codPrograma}")
    @Operation(summary = "Avalia elegibilidade para programa")
    @ApiResponse(responseCode = "200", description = "Resultado da elegibilidade")
    public ResponseEntity<Map<String, Object>> elegibilidade(@PathVariable Long id,
                                                              @PathVariable String codPrograma) {
        var b = cadastroService.buscar(id)
                .orElseThrow(() -> new DomainException("BENEFICIARIO_NOT_FOUND",
                        "Beneficiário não encontrado: " + id));
        var result = elegibilidadeService.avaliar(b, codPrograma);
        return ResponseEntity.ok(Map.of(
                "elegivel", result.elegivel(),
                "programaNome", result.programaNome() != null ? result.programaNome() : "",
                "motivo", result.motivo() != null ? result.motivo() : ""
        ));
    }
}

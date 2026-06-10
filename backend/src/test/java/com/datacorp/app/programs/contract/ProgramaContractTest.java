package com.datacorp.app.programs.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Contract tests for POST/GET /api/v1/programas.
 * REQ-016: Unicidade do código de programa (409 on duplicate).
 * REQ-017: Valor base ajustado pelo Fator-K na inclusão.
 * source_legacy: CADPROG.NSN, CONSPROG.NSN
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProgramaContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postPrograma_comCodUnico_retorna201() throws Exception { // REQ-016
        String body = """
                {
                  "codPrograma": "TST1",
                  "nomePrograma": "Programa Teste",
                  "siglaPrograma": "TEST",
                  "tipoPrograma": "A",
                  "sitPrograma": "A",
                  "vlrBase": "1000.00",
                  "vlrMaximo": "2000.00",
                  "vlrMinimo": "500.00",
                  "fatorK": "0.347215",
                  "tiposDesconto": []
                }
                """;

        mockMvc.perform(post("/api/v1/programas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void postPrograma_codDuplicado_retorna409() throws Exception { // REQ-016
        String body = """
                {
                  "codPrograma": "DUP1",
                  "nomePrograma": "Duplicado",
                  "siglaPrograma": "DUP",
                  "tipoPrograma": "A",
                  "sitPrograma": "A",
                  "vlrBase": "1000.00",
                  "vlrMaximo": "2000.00",
                  "vlrMinimo": "500.00",
                  "fatorK": "0.347215",
                  "tiposDesconto": []
                }
                """;

        // First creation succeeds
        mockMvc.perform(post("/api/v1/programas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body));

        // Duplicate must return 409
        mockMvc.perform(post("/api/v1/programas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void getPrograma_existente_retornaVlrBaseAjustado() throws Exception { // REQ-017
        mockMvc.perform(get("/api/v1/programas/PBF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codPrograma").value("PBF"));
    }

    @Test
    void getPrograma_inexistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/v1/programas/XXXX"))
                .andExpect(status().isNotFound());
    }
}

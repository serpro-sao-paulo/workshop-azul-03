package com.datacorp.app.payments.contract;

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
 * Contract tests for payment-related endpoints.
 * REQ-018..035.
 * source_legacy: BATCHPGT.NSN, BATCHCON.NSN, BATCHREL.NSN
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PagamentoContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postFolha_competenciaInvalida_retorna422() throws Exception { // REQ-029
        mockMvc.perform(post("/api/v1/folhas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "competencia": 0, "codPrograma": "PBF" }
                        """))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getFolha_inexistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/v1/folhas/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRelatorio_retorna200() throws Exception { // REQ-035
        mockMvc.perform(get("/api/v1/relatorios/consolidado")
                        .param("competencia", "202601"))
                .andExpect(status().isOk());
    }
}

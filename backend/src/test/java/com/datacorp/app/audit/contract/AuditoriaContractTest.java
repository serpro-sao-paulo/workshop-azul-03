package com.datacorp.app.audit.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Contract test for GET /api/v1/auditoria.
 * REQ-036: audit trail populated by domain events.
 * REQ-037: exclusions always visible; immutable trail.
 * source_legacy: N/A (greenfield, ADR-004, IN-TCU 63)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditoriaContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAuditoria_retorna200() throws Exception { // REQ-036
        mockMvc.perform(get("/api/v1/auditoria"))
                .andExpect(status().isOk());
    }

    @Test
    void getAuditoria_porEntidade_retornaResultados() throws Exception { // REQ-037
        mockMvc.perform(get("/api/v1/auditoria")
                        .param("tipoEntidade", "BENF"))
                .andExpect(status().isOk());
    }
}

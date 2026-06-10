package com.datacorp.app.beneficiaries.contract;

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
 * Contract tests for /api/v1/beneficiarios (POST, PUT, GET, dependentes, elegibilidade).
 * REQ-001..015.
 * source_legacy: CADBENEF.NSN, CONSBENEF.NSN, CADDEPEND.NSN, VALELEG.NSN
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BeneficiarioContractTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String VALID_CPF = "529.982.247-25";
    private static final String VALID_BODY = """
            {
              "cpf": "529.982.247-25",
              "nomeCompleto": "João da Silva",
              "nomeMae": "Maria da Silva",
              "dtNascimento": "1985-05-15",
              "sexo": "M",
              "endereco": {
                "uf": "SP",
                "municipio": "São Paulo",
                "logradouro": "Rua das Flores",
                "numero": "100",
                "cep": "01001000"
              },
              "vlrRendaFamiliar": "1200.00",
              "qtdMembros": 3
            }
            """;

    @Test
    void postBeneficiario_cpfInvalido_retorna422() throws Exception { // REQ-001
        String body = VALID_BODY.replace(VALID_CPF, "111.111.111-11");
        mockMvc.perform(post("/api/v1/beneficiarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void postBeneficiario_semNome_retorna422() throws Exception { // REQ-004
        String body = VALID_BODY.replace("João da Silva", "");
        mockMvc.perform(post("/api/v1/beneficiarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void postBeneficiario_valido_retorna201() throws Exception { // REQ-006
        mockMvc.perform(post("/api/v1/beneficiarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated());
    }

    @Test
    void getBeneficiario_inexistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/v1/beneficiarios/999999"))
                .andExpect(status().isNotFound());
    }
}

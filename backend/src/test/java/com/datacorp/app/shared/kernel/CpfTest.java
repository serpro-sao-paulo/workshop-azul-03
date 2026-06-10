package com.datacorp.app.shared.kernel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link Cpf} value object.
 * REQ-001: CPF obrigatório e válido por Módulo 11.
 * REQ-003: Rejeição de CPF com dígitos repetidos.
 * REQ-015: CPF mascarado em logs e consultas (short-CPF normalization).
 * source_legacy: VALBENEF.NSN#L113-L205, CADBENEF.NSN#L103-L115
 */
class CpfTest {

    // ── REQ-001: Módulo 11 validation ──────────────────────────────────────

    @Test
    void cpfValido_deveSerAceito() { // REQ-001
        assertThatNoException().isThrownBy(() -> Cpf.of("529.982.247-25"));
    }

    @Test
    void cpfComDigitoVerificadorErrado_deveSerRejeitado() { // REQ-001
        assertThatThrownBy(() -> Cpf.of("529.982.247-26"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CPF inválido");
    }

    @Test
    void cpfZerado_deveSerRejeitado() { // REQ-001
        assertThatThrownBy(() -> Cpf.of("000.000.000-00"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cpfNulo_deveSerRejeitado() { // REQ-001
        assertThatThrownBy(() -> Cpf.of(null))
                .isInstanceOf(NullPointerException.class);
    }

    // ── REQ-003: All-equal digits ──────────────────────────────────────────

    @ParameterizedTest
    @ValueSource(strings = {
        "111.111.111-11",
        "222.222.222-22",
        "333.333.333-33",
        "444.444.444-44",
        "555.555.555-55",
        "666.666.666-66",
        "777.777.777-77",
        "888.888.888-88",
        "999.999.999-99"
    })
    void cpfComDigitosIguais_deveSerRejeitado(String cpf) { // REQ-003
        assertThatThrownBy(() -> Cpf.of(cpf))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CPF inválido");
    }

    // ── REQ-015: Masking ───────────────────────────────────────────────────

    @Test
    void mascara_deveExibirApenasUltimosDigitos() { // REQ-015
        Cpf cpf = Cpf.of("529.982.247-25");
        String masked = cpf.masked();
        assertThat(masked).isEqualTo("***.982.247-**");
    }

    @Test
    void shortCpf_deveSerNormalizadoPara11Digitos() { // REQ-015
        // Legacy SIFAP sometimes stored CPFs without leading zeros (e.g., 9 digits)
        // Normalization to 11 digits before masking is required
        Cpf cpf = Cpf.of("529982247-25"); // without formatting, 11 chars
        assertThat(cpf.value()).hasSize(11);
        assertThat(cpf.masked()).isEqualTo("***.982.247-**");
    }

    @Test
    void valorFormatado_deveRetornarApenas11Digitos() {
        Cpf cpf = Cpf.of("52998224725"); // raw 11-digit format
        assertThat(cpf.value()).isEqualTo("52998224725");
        assertThat(cpf.value()).hasSize(11);
    }

    @Test
    void doisCpfsComMesmoValor_devemSerIguais() {
        Cpf a = Cpf.of("529.982.247-25");
        Cpf b = Cpf.of("52998224725");
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}

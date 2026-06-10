package com.datacorp.app.beneficiaries.spi;

/**
 * Eligibility result DTO exposed via SPI.
 * REQ-012..014.
 */
public record ResultadoElegibilidade(
        boolean elegivel,
        String programaNome,
        String motivo
) {
    public static ResultadoElegibilidade elegivel(String programaNome) {
        return new ResultadoElegibilidade(true, programaNome, null);
    }

    public static ResultadoElegibilidade inelegivel(String motivo) {
        return new ResultadoElegibilidade(false, null, motivo);
    }
}

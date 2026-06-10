package com.datacorp.app.beneficiaries.spi;

/**
 * SPI for eligibility API — consumed by Payments when determining payment eligibility.
 * source_legacy: VALELEG.NSN (REQ-012..014)
 */
public interface ElegibilidadeApi {
    ResultadoElegibilidade avaliarElegibilidade(Long numInscricao, String codPrograma);
}

package com.datacorp.app.beneficiaries.domain;

/**
 * Biological sex of the beneficiary.
 * Source: BENEFICIARIO.ddm field AG SEXO A 1. Values: M/F/I (I=Indefinido).
 * The 'I' value was present in the DDM but not listed in CADBENEF validation
 * (which accepts only M/F — CADBENEF rule #6 in business-rules-catalog.md).
 * FIXME: confirm with stakeholders whether 'I' is a valid active value in production data.
 */
public enum Sexo { M, F, I }

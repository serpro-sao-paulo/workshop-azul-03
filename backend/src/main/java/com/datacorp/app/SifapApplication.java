package com.datacorp.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

/**
 * SIFAP Core Modernization — Spring Boot + Spring Modulith application entry point.
 *
 * <p>Spring Modulith enforces module boundaries (Principle IV) and provides
 * the durable event publication registry (ADR-004) for the audit trail.
 *
 * <p>Bounded contexts (Spring Modulith modules):
 * <ul>
 *   <li>{@code beneficiaries} — Gestão de Beneficiários (FNR 150)</li>
 *   <li>{@code programs} — Catálogo de Programas Sociais (FNR 151)</li>
 *   <li>{@code payments} — Pagamentos &amp; Folha (FNR 152)</li>
 *   <li>{@code audit} — Auditoria &amp; Conformidade (FNR 153)</li>
 * </ul>
 */
@SpringBootApplication
@Modulithic(
    systemName = "SIFAP",
    sharedModules = "shared"
)
public class SifapApplication {

    public static void main(String[] args) {
        SpringApplication.run(SifapApplication.class, args);
    }
}

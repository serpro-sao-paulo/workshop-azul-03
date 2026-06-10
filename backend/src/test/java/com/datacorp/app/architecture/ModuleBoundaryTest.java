package com.datacorp.app.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * ArchUnit module boundary tests (Principle IV).
 * No module may import another context's domain/, service/, or repository/.
 * Only api/ and spi/ packages are public cross-context.
 *
 * source_legacy: N/A (architectural governance)
 */
@AnalyzeClasses(packages = "com.datacorp.app")
class ModuleBoundaryTest {

    // ── Beneficiaries boundaries ────────────────────────────────────────────

    @ArchTest
    static final ArchRule programs_must_not_access_beneficiaries_domain =
            noClasses().that().resideInAPackage("..programs..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "..beneficiaries.domain..",
                            "..beneficiaries.service..",
                            "..beneficiaries.repository..");

    @ArchTest
    static final ArchRule payments_must_not_access_beneficiaries_internals =
            noClasses().that().resideInAPackage("..payments..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "..beneficiaries.domain..",
                            "..beneficiaries.service..",
                            "..beneficiaries.repository..");

    @ArchTest
    static final ArchRule audit_must_not_access_beneficiaries_internals =
            noClasses().that().resideInAPackage("..audit..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "..beneficiaries.domain..",
                            "..beneficiaries.service..",
                            "..beneficiaries.repository..");

    // ── Programs boundaries ─────────────────────────────────────────────────

    @ArchTest
    static final ArchRule beneficiaries_must_not_access_programs_internals =
            noClasses().that().resideInAPackage("..beneficiaries..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "..programs.domain..",
                            "..programs.service..",
                            "..programs.repository..");

    @ArchTest
    static final ArchRule payments_must_not_access_programs_internals =
            noClasses().that().resideInAPackage("..payments..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "..programs.domain..",
                            "..programs.service..",
                            "..programs.repository..");

    // ── Payments boundaries ─────────────────────────────────────────────────

    @ArchTest
    static final ArchRule beneficiaries_must_not_access_payments_internals =
            noClasses().that().resideInAPackage("..beneficiaries..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "..payments.domain..",
                            "..payments.service..",
                            "..payments.repository..");

    @ArchTest
    static final ArchRule audit_must_not_access_payments_internals =
            noClasses().that().resideInAPackage("..audit..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "..payments.domain..",
                            "..payments.service..",
                            "..payments.repository..");

    // ── Repository must not be accessed outside its own context ────────────

    @ArchTest
    static final ArchRule repositories_not_accessed_cross_context =
            classes().that().resideInAPackage("..repository..")
                    .should().onlyBeAccessed().byClassesThat()
                    .resideInAnyPackage(
                            "..repository..",
                            "..service..",
                            "..config..",
                            "..integration..",  // tests
                            "..architecture.."); // this test
}

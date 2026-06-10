package com.datacorp.app.beneficiaries.domain;

import com.datacorp.app.shared.kernel.Cpf;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Embeddable representing one occurrence of GRP-DEPENDENTE (Adabas PE group DA,
 * max 10 occurrences) from BENEFICIARIO (FNR 150).
 *
 * <p>ADR-003: PE groups mapped as {@code @ElementCollection} of {@code @Embeddable}.
 * Stored in child table {@code beneficiaries.beneficiario_dependente}.
 *
 * <p>Business constraints (CadastroService / DependenteService):
 * <ul>
 *   <li>Max 5 dependents per beneficiary (REQ-009) — enforced by service, not DB CHECK</li>
 *   <li>Parentesco must be one of FI/CO/IR/OU (REQ-011)</li>
 *   <li>Cannot add to titular with status C or D (REQ-010)</li>
 *   <li>CPF duplicate check only when cpfDependente is not null (CADDEPEND#L91-L101)</li>
 * </ul>
 *
 * <p>Source: BENEFICIARIO.ddm fields DB–DG:
 * <pre>
 *   DB CPF-DEPENDENTE  A 11 → cpfDependente  (optional — 0 = absent)
 *   DC NOME-DEPENDENTE A 60 → nomeDependente  (required, REQ cadastro)
 *   DD DT-NASC-DEPEND  N  8 → dtNascimento    (YYYYMMDD)
 *   DE PARENTESCO      A  2 → parentesco       (FI/CO/IR/OU per CADDEPEND code, REQ-011)
 *   DF SIT-DEPENDENTE  A  1 → situacao         (A/I/D)
 *   DG IND-DEFICIENCIA A  1 → indDeficiencia   (S/N)
 * </pre>
 */
@Embeddable
public class Dependente {

    /**
     * Dependent's CPF. DDM: DB CPF-DEPENDENTE A 11.
     * Optional (children may not have CPF yet). Null when not provided.
     * Duplicate check: only when present (CADDEPEND.NSN#L96 "AND #CPF-DEP NE 0").
     * MASKED in display (REQ-015, Principle V).
     */
    @Convert(converter = com.datacorp.app.shared.kernel.CpfConverter.class)
    @Column(name = "cpf_dependente", length = 11)
    private Cpf cpfDependente;

    /** Dependent's name. DDM: DC NOME-DEPENDENTE A 60. Required per cadastro rules. */
    @Column(name = "nome_dependente", nullable = false, length = 60)
    private String nomeDependente;

    /**
     * Date of birth. DDM: DD DT-NASC-DEPEND N 8 (YYYYMMDD → LocalDate).
     * dateformat=YYYYMMDD per command argument.
     */
    @Convert(converter = com.datacorp.app.shared.kernel.LocalDateN8Converter.class)
    @Column(name = "dt_nascimento")
    private LocalDate dtNascimento;

    /**
     * Kinship type. DDM: DE PARENTESCO A 2. REQ-011: must be FI/CO/IR/OU.
     * FIXME: DDM comment uses different codes (CJ/NT/TU) — see Parentesco enum Javadoc.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "parentesco", length = 2)
    private Parentesco parentesco;

    /**
     * Dependent's status. DDM: DF SIT-DEPENDENTE A 1. Values: A=Ativo, I=Inativo, D=Desligado.
     */
    @Column(name = "sit_dependente", length = 1)
    private String situacao;

    /**
     * Disability indicator. DDM: DG IND-DEFICIENCIA A 1. S=Sim, N=Não.
     */
    @Column(name = "ind_deficiencia", length = 1)
    private String indDeficiencia;

    protected Dependente() {}

    /** Convenience constructor for test use. */
    public Dependente(String nome, LocalDate dtNasc, Parentesco parentesco,
                      Cpf cpf, String situacao) {
        this.nomeDependente = nome;
        this.dtNascimento = dtNasc;
        this.parentesco = parentesco;
        this.cpfDependente = cpf;
        this.situacao = situacao != null ? situacao : "A";
    }

    public static Dependente of(Cpf cpf, String nome, LocalDate dtNasc,
                                 Parentesco parentesco, String situacao, String indDeficiencia) {
        var d = new Dependente();
        d.cpfDependente = cpf;
        d.nomeDependente = nome;
        d.dtNascimento = dtNasc;
        d.parentesco = parentesco;
        d.situacao = situacao;
        d.indDeficiencia = indDeficiencia;
        return d;
    }

    public Cpf getCpfDependente()       { return cpfDependente; }
    public String getNomeDependente()   { return nomeDependente; }
    public LocalDate getDtNascimento()  { return dtNascimento; }
    public Parentesco getParentesco()   { return parentesco; }
    public String getSituacao()         { return situacao; }
    public String getIndDeficiencia()   { return indDeficiencia; }
}

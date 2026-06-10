package com.datacorp.app.beneficiaries.api;

import com.datacorp.app.beneficiaries.domain.EstadoCivil;
import com.datacorp.app.beneficiaries.domain.Sexo;
import com.datacorp.app.beneficiaries.domain.Endereco;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Request DTO for beneficiary registration. REQ-001..008. */
public record BeneficiarioRequest(
        @NotBlank String cpf,
        @NotBlank String nomeCompleto,
        String nomeMae,
        @NotNull LocalDate dtNascimento,
        Sexo sexo,
        EstadoCivil estCivil,
        @NotNull EnderecoDto endereco,
        String codPrograma,
        BigDecimal vlrRendaFamiliar,
        Integer qtdMembros
) {
    public record EnderecoDto(
            String logradouro,
            String numero,
            String complemento,
            String bairro,
            String municipio,
            @NotBlank String uf,
            String cep,
            Integer codIbge,
            String codRegiao
    ) {}
}

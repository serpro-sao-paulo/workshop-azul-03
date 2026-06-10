<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Glossário do SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **glossary**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Preencha esta tabela com todos os termos, abreviações e siglas encontrados no código Natural/Adabas.
> **Meta: no mínimo 30 termos.**

## Por que isso importa

Sistemas legados têm vocabulário próprio que ninguém documenta em lugar nenhum — só está no nome das variáveis. Se o time do Estágio 2 não souber o que `DSCT`, `BENF`, `PE` ou `CTC` significam, vai escrever uma spec sobre o que ele _acha_ que isso significa. Glossário é o que evita esse desencontro.

## Como preencher

- **Termo**: a abreviação ou sigla exatamente como aparece no código
- **Expansão**: o significado completo do termo
- **Programa**: em qual arquivo `.NSN` ou `.ddm` o termo foi encontrado
- **Contexto**: breve explicação de como/onde o termo é usado

## Dica de extração

Prompt útil no Copilot Chat (cole o conteúdo de 2–3 arquivos `.NSN` no chat antes):

> _"Liste todas as abreviações e siglas usadas neste código Natural. Para cada uma, sugira a expansão e marque com 'CONFIRMADO' ou 'HIPÓTESE'."_

## Termos encontrados

> Preenchido em **2026-06-10** a partir da leitura dos 15 programas `.NSN` e dos 4 DDMs. `CONFIRMADO` = significado comprovado por código/DDM; `HIPÓTESE` = inferido pelo uso.

| #   | Termo | Expansão | Programa / DDM | Contexto |
| --- | ----- | -------- | -------------- | -------- |
| 1 | `SIFAP` | Sistema de Fiscalização e Administração de Pagamentos | todos | Sistema legado de benefícios sociais. CONFIRMADO. |
| 2 | `BENF` / `BENEF` | Beneficiário | `CADBENEF`, `CALCBENF`, `VALBENEF`, `CONSBENF` | Pessoa que recebe o benefício social. CONFIRMADO. |
| 3 | `DSCT` | Desconto | `CALCDSCT`, `PAGAMENTO.ddm` | Dedução sobre o valor bruto. Tipos código: C/I/J/S/P/A. CONFIRMADO. |
| 4 | `PGT` / `PGTO` | Pagamento | `BATCHPGT`, `RELPGT`, `PAGAMENTO.ddm` | Registro de pagamento mensal. CONFIRMADO. |
| 5 | `CON` | Conciliação | `BATCHCON` | Conciliação bancária (CNAB × SIFAP). CONFIRMADO. |
| 6 | `REL` | Relatório | `RELPGT`, `RELAUDIT`, `BATCHREL` | Programas de geração de relatórios. CONFIRMADO. |
| 7 | `CALC` | Cálculo | `CALCBENF`, `CALCDSCT`, `CALCCORR` | Rotinas de cálculo financeiro. CONFIRMADO. |
| 8 | `CAD` | Cadastro | `CADBENEF`, `CADDEPEND`, `CADPROG` | Programas de manutenção (CRUD). CONFIRMADO. |
| 9 | `VAL` | Validação | `VALBENEF`, `VALDOCS`, `VALELEG` | Rotinas de validação. CONFIRMADO. |
| 10 | `ELEG` | Elegibilidade | `VALELEG`, `PROGRAMA-SOCIAL.ddm` | Regras que decidem se o beneficiário pode receber. CONFIRMADO. |
| 11 | `CORR` | Correção (monetária) | `CALCCORR` | Correção retroativa por IPCA. CONFIRMADO. |
| 12 | `CPF` | Cadastro de Pessoa Física | vários | Identificador único do beneficiário (N11), validado por Módulo 11. CONFIRMADO. |
| 13 | `NIS` | Número de Identificação Social | `BENEFICIARIO.ddm`, `VALELEG`, `CONSBENF` | Identificador social alternativo (busca em CONSBENF). CONFIRMADO. |
| 14 | `COMPETENCIA` | Competência (AAAAMM) | `CALCBENF`, `BATCHPGT`, `PAGAMENTO.ddm` | Mês/ano de referência do pagamento. CONFIRMADO. |
| 15 | `VLR-BASE` | Valor Base | `PROGRAMA-SOCIAL.ddm`, `CALCBENF` | Valor mensal base do programa, ajustado por Fator-K. CONFIRMADO. |
| 16 | `FATOR-K` | Fator de Correção Especial | `CADPROG`, `PROGRAMA-SOCIAL.ddm` (BG) | Multiplicador `1+(reajuste×0.347215)` aplicado ao VLR-BASE. Origem não documentada. HIPÓTESE (semântica). |
| 17 | `FATOR-REG` | Fator Regional | `CALCBENF`, `BATCHPGT` | Multiplicador por UF/região (tabela 27 posições). CONFIRMADO. |
| 18 | `FATOR-FAM` | Fator Familiar | `CALCBENF` | Acréscimo por número de dependentes. CONFIRMADO. |
| 19 | `FATOR-RND` | Fator de Renda | `CALCBENF` | Multiplicador por faixa de renda familiar. CONFIRMADO. |
| 20 | `FATOR-REAJUSTE` | Fator de Reajuste | `PROGRAMA-SOCIAL.ddm`, `CALCBENF` | Reajuste anual do programa (RN-019). CONFIRMADO. |
| 21 | `ABONO` (natalino) | 13º / Abono Natalino | `CALCBENF`, `PAGAMENTO.ddm` (VLR-ABONO) | 15% adicional em dezembro para programa tipo 'A'. CONFIRMADO. |
| 22 | `13O` / `DECIMO` | Décimo terceiro benefício | `CALCBENF`, `BATCHPGT` | Pagamento extra de dezembro (tipo 'D'). CONFIRMADO. |
| 23 | `IPCA` | Índice de Preços ao Consumidor Amplo | `CALCCORR` | Índice de correção retroativa (tabela 2010-2012). CONFIRMADO. |
| 24 | `CNAB 240` | Centro Nacional de Automação Bancária (layout 240) | `BATCHCON` | Layout do arquivo de retorno do Banco do Brasil. CONFIRMADO. |
| 25 | `SIAFI` | Sistema Integrado de Administração Financeira | `BATCHCON`, `PAGAMENTO.ddm` (FA-FE) | Integração financeira do governo (OB/NE/UG/gestão). CONFIRMADO. |
| 26 | `OB` | Ordem Bancária (SIAFI) | `PAGAMENTO.ddm` (FA) | Documento de pagamento SIAFI. CONFIRMADO. |
| 27 | `NE` | Nota de Empenho (SIAFI) | `PAGAMENTO.ddm` (FB) | Empenho orçamentário SIAFI. CONFIRMADO. |
| 28 | `UG` | Unidade Gestora | `PAGAMENTO.ddm` (FC) | Unidade gestora emitente no SIAFI. CONFIRMADO. |
| 29 | `PE` | Periodic Group (Adabas) | `BENEFICIARIO.ddm`, `PAGAMENTO.ddm` | Grupo periódico (array de campos): dependentes, descontos. CONFIRMADO. |
| 30 | `MU` | Multiple-value field (Adabas) | `PROGRAMA-SOCIAL.ddm`, `AUDITORIA.ddm` | Campo multivalorado (ex.: tipos de desconto, campos auditados). CONFIRMADO. |
| 31 | `DE` | Descriptor (Adabas) | todos os `.ddm` | Campo indexado para busca (FIND/READ LOGICAL). CONFIRMADO. |
| 32 | `FNR` | File Number (Adabas) | todos os `.ddm` | Identificador do arquivo: 150 BEN, 151 PRG, 152 PAG, 153 AUD. CONFIRMADO. |
| 33 | `DBID` | Database ID (Adabas) | todos os `.ddm` | Banco de dados (DBID 57). CONFIRMADO. |
| 34 | `ISN` | Internal Sequence Number (Adabas) | `BENEFICIARIO.ddm` | Identificador interno de registro. CONFIRMADO. |
| 35 | `SUPERDESCRIPTOR` (S1/S2/S3) | Super-descritor (chave composta) | todos os `.ddm` | Índice composto (ex.: CPF+competência). CONFIRMADO. |
| 36 | `MOD 11` | Módulo 11 | `CADBENEF`, `VALBENEF`, `VALDOCS` | Algoritmo de dígito verificador de CPF. CONFIRMADO. |
| 37 | `CTPS` | Carteira de Trabalho e Previdência Social | `VALDOCS` | Documento coletado (não validado). CONFIRMADO. |
| 38 | `RG` | Registro Geral (identidade) | `BENEFICIARIO.ddm`, `VALDOCS` | Documento de identidade. CONFIRMADO. |
| 39 | `UF` | Unidade Federativa | `BENEFICIARIO.ddm`, `VALBENEF` | Estado (tabela 27 UFs). CONFIRMADO. |
| 40 | `COD-REGIAO` | Código de Região | `BENEFICIARIO.ddm`, `CALCBENF`, `VALELEG` | 01-25 macrorregiões; **99 = especial/internacional** (bypass). CONFIRMADO. |
| 41 | `STATUS` benef. (A/S/C/I/D) | Situação do Beneficiário | `CONSBENF`, `VALELEG`, `BENEFICIARIO.ddm` | A=ativo, S=suspenso, C=cancelado, I=inativo, D=desligado. CONFIRMADO. |
| 42 | `STATUS-PGTO` (G/P/D/E/C) | Situação do Pagamento | `PAGAMENTO.ddm`, `BATCHCON`, `BATCHREL` | G=gerado, P=pago, D=devolvido, E=erro/estornado(⚠️ conflito), C=cancelado. HIPÓTESE (conflito DDM×código). |
| 43 | `TIPO-PGTO` (N/D/T) | Tipo de Pagamento | `PAGAMENTO.ddm`, `RELPGT` | N=normal, D=décimo, T=terceiro(órfão). HIPÓTESE (T não gravado). |
| 44 | `TIPO-PROG` (A/P/T) | Tipo de Programa Social | `PROGRAMA-SOCIAL.ddm`, `VALELEG`, `CADPROG` | A=assistencial, P=previdenciário, T=trabalho. CONFIRMADO. |
| 45 | `PARENTESCO` (FI/CO/IR/OU) | Grau de Parentesco do Dependente | `CADDEPEND` | FI=filho, CO=cônjuge, IR=irmão, OU=outro (DDM usa CJ/NT/TU). CONFIRMADO (divergência código×DDM). |
| 46 | `COD-ELEGIBILIDADE` | Código de Elegibilidade Específica | `PROGRAMA-SOCIAL.ddm`, `VALELEG` | Código posicional A5 (pos.1='R' NIS, pos.2='D' dependentes). HIPÓTESE (pos. 3-5 desconhecidas). |
| 47 | `CICLO` | Ciclo de Processamento | `PAGAMENTO.ddm` (AF) | Identificador do ciclo batch. CONFIRMADO. |
| 48 | `ABEND U4038` | Abnormal End (código U4038) | doc (não no código) | Terminação anormal do batch citada na doc, não encontrada no código. HIPÓTESE. |
| 49 | `CadÚnico` | Cadastro Único para Programas Sociais | doc (RN-016) | Base externa de cruzamento; programa não localizado no legado. HIPÓTESE. |
| 50 | `SENARC` / `CGPB` / `MDS` | Órgãos gestores (Secretaria/Coordenação/Ministério) | docs, `PROGRAMA-SOCIAL.ddm` | Áreas de negócio responsáveis pelas regras. CONFIRMADO (contexto). |

> Mínimo do DoD: 15 termos — **50 catalogados**.

## Exemplo de linha bem preenchida

| #   | Termo  | Expansão | Programa                        | Contexto                                                                                                         |
| --- | ------ | -------- | ------------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| 1   | `DSCT` | Desconto | `CALCDSCT.NSN`, `PAGAMENTO.ddm` | Tipo de dedução aplicada sobre valor bruto do pagamento. Tipos: 'J' (judicial), 'I' (imposto), 'T' (trabalhista) |

## Observações

- **Convenções de prefixo de programa:** `CAD*` cadastro, `CALC*` cálculo, `VAL*` validação, `CONS*` consulta, `BATCH*` batch, `REL*` relatório.
- **Convenções de campo Adabas:** sufixos `-BENF` (beneficiário), `-PGTO` (pagamento), `-DSCT` (desconto), `-REG` (regional); prefixo `#` para variáveis de trabalho Natural.
- **Termos ambíguos que precisam de validação com especialista:**
  - `FATOR-K` (#16) — campo persistido no DDM vs. calculado em CADPROG; constante `0.347215` sem origem.
  - `STATUS-PGTO` (#42) — significados conflitantes entre DDM e código (especialmente 'E' e 'C').
  - `TIPO-PGTO 'T'` (#43) — "terceiro" referenciado mas nunca gravado.
  - `COD-ELEGIBILIDADE` (#46) — posições 3-5 sem uso conhecido.

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="GUIDE.md"><strong>GUIDE do Estágio 1</strong></a><br/>
<sub>Passo a passo do estágio.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="business-rules-catalog.md"><strong>business-rules-catalog.md</strong></a><br/>
<sub>Catálogo de regras.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>


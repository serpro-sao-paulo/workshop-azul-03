<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-003: Mapeamento de grupos periódicos (PE) Adabas para JPA

![ESTÁGIO 02 Spec Moderna](https://img.shields.io/badge/ESTÁGIO-02%20Spec%20Moderna-7FBA00?style=for-the-badge) ![TIPO ADR](https://img.shields.io/badge/TIPO-ADR-1A1A1A?style=for-the-badge) ![AGENTE architect](https://img.shields.io/badge/AGENTE-@architect--agent-0078D4?style=for-the-badge)

> Produzido por `/generate-adr`. Formato MADR. Complementa [ADR-001](adr-001-map-adabas-mu-fields-to-jsonb-vs-elementcollection.md) (campos MU escalares). Fundamentado nos DDMs reais ([adabas-ddms/](../../01-arqueologia/legado-sifap/adabas-ddms/)).

## Status

🟡 **Proposta — pendente de ratificação da equipe** (Software Architect + DBA). A análise está completa.

## Data

2026-06-10

## Contexto

[ADR-001](adr-001-map-adabas-mu-fields-to-jsonb-vs-elementcollection.md) decidiu o único campo **MU escalar** do legado. Restam os **grupos periódicos (PE)** — grupos **estruturados** (vários campos por ocorrência) que repetem. No legado há quatro:

| PE | DDM | Máx ocorrências | Volume do registro pai | Acesso |
| -- | --- | --------------- | ---------------------- | ------ |
| `GRP-DEPENDENTE` | BENEFICIARIO (150) | 10 | ~4,2 mi | leitura no cálculo do fator familiar; escrita no cadastro |
| `GRP-FAIXA-CALCULO` | PROGRAMA-SOCIAL (151) | 5 | ~45 | reference data, lido inteiro no cálculo |
| `GRP-PARAM-REGIONAL` | PROGRAMA-SOCIAL (151) | 6 | ~45 | reference data, lido inteiro no cálculo |
| `GRP-DESCONTO` | PAGAMENTO (152) | 8 | **~180 mi** | escrito na geração/conciliação; lido no cálculo de desconto |

A tensão é entre os grupos sobre tabelas pequenas/médias (BENEFICIARIO, PROGRAMA-SOCIAL) e o grupo sobre `PAGAMENTO` (~180 mi registros, sem purge, crescimento ~3,8 mi/mês). A decisão precisa ser tomada **agora** porque a modelagem JPA dos três contextos donos depende dela, e os requisitos REQ-009 (dependentes), REQ-019/REQ-020 (faixas/regionais) e REQ-025/REQ-026/REQ-027 (descontos) leem esses grupos.

> Escopo: **apenas grupos PE estruturados.** Campos MU escalares → ADR-001. A máquina de status do PAGAMENTO → ADR-002.

## Opções Consideradas

### Opção 1: `@ElementCollection` de `@Embeddable` (tabela filha) para todos os PE

Cada grupo PE vira uma coleção de um `@Embeddable` (record/classe de valor) numa tabela filha (ex.: `beneficiario_dependente`, `programa_faixa_calculo`, `pagamento_desconto`).

- **Prós (no contexto SIFAP):**
  - Cada campo do grupo é **coluna tipada e consultável** (SQL padrão) — necessário para REQ-009 (contar dependentes), REQ-026 (somar descontos por tipo, identificar judicial 'JD'), REQ-019/020 (faixas).
  - Integridade e índices por campo (ex.: índice em `tipo_desconto`), portável no PostgreSQL.
  - Modelo de domínio limpo e fortemente tipado, coerente com a unificação de regras que a spec exige.
- **Contras (no contexto SIFAP):**
  - Em `pagamento_desconto` (filho de ~180 mi pais × ≤8 = **até ~1,4 bi linhas**), uma tabela filha relacional é volumosa: cuidado com fetch (N+1) e custo de JOIN em consultas analíticas/relatórios.
  - Carregar o pagamento com descontos exige fetch join / `@BatchSize` deliberados.
- **Risco:** Sem estratégia de fetch e particionamento, relatórios sobre `pagamento_desconto` ficam lentos no volume real. Mitigável (índices, partição por competência, projeções).
- **Esforço:** same para BENEFICIARIO/PROGRAMA-SOCIAL; higher para PAGAMENTO (tuning de performance).

### Opção 2: Híbrido — `@ElementCollection` para PE de baixo volume; **JSONB** para `GRP-DESCONTO` no PAGAMENTO

Usar tabela filha relacional para `GRP-DEPENDENTE`, `GRP-FAIXA-CALCULO`, `GRP-PARAM-REGIONAL` (volumes pequenos/médios), e persistir `GRP-DESCONTO` como coluna `jsonb` **dentro** de `pagamento` (array de objetos desconto), via Hibernate 6 `@JdbcTypeCode(SqlTypes.JSON)`.

- **Prós (no contexto SIFAP):**
  - Em `PAGAMENTO`, os descontos são **read-whole/write-whole junto do pagamento** (gerados e lidos com o registro) e raramente consultados isoladamente em produção → JSONB evita ~1,4 bi de linhas-filhas e o JOIN correspondente, mantendo o desconto **co-localizado** com o pagamento (uma única leitura).
  - PE de baixo volume continuam relacionais, preservando consultabilidade/integridade onde ela importa (dependentes, faixas, regionais).
  - PostgreSQL 16 suporta `jsonb` nativamente com operador de contención (`@>`) e índice GIN se algum relatório precisar filtrar por tipo de desconto.
- **Contras (no contexto SIFAP):**
  - Modelo **heterogêneo** (dois padrões de mapeamento de PE) — mais carga cognitiva e duas formas de escrever testes/queries.
  - `GRP-DESCONTO` em JSONB perde tipagem forte e FK ao vocabulário de tipos de desconto; a validação do tipo passa a depender do código (embora o catálogo de tipos aplicáveis viva em PROGRAMA-SOCIAL via ADR-001).
  - Consultas analíticas por tipo de desconto sobre o histórico exigem operadores jsonb não-portáveis.
- **Risco:** Se, no futuro, relatórios precisarem agregar descontos por tipo sobre todo o histórico, o JSONB encarece essas consultas vs. uma coluna indexada. Probabilidade média (relatórios de desconto existem no domínio).
- **Esforço:** higher no design (dois padrões + decisão de quando consultar JSONB), lower no volume de escrita do PAGAMENTO.

## Decisão

**Escolhida: Opção 1 — `@ElementCollection` de `@Embeddable` para todos os PE**, com estratégia explícita de performance para `pagamento_desconto` (índice em `tipo_desconto`, particionamento de `pagamento` por competência, fetch join controlado).

**Razão (uma frase):** Os requisitos de desconto (REQ-026 isenção judicial, REQ-025 contribuição, REQ-027) exigem identificar e somar descontos **por tipo de forma confiável e consultável**, o que pede colunas tipadas com integridade; o risco de volume do PAGAMENTO é **gerenciável por particionamento/índices**, ao passo que a perda de consultabilidade/tipagem do JSONB seria estrutural e difícil de reverter.

> JSONB permanece como **otimização reservada**: se medições reais (Estágio 3) demonstrarem que `pagamento_desconto` é gargalo e que descontos nunca são consultados isoladamente, reabrir esta ADR para o híbrido (Opção 2) com dados de performance concretos — não por antecipação.

## Consequências

### Positivas

- Domínio uniforme: um único padrão de mapeamento de PE, mais simples de testar e revisar.
- Descontos consultáveis e tipados com integridade — base sólida para REQ-025/026/027 e para a unificação da de-para de tipos (ADR-001).
- Dependentes, faixas e parâmetros regionais ficam relacionais e indexáveis (REQ-009, REQ-019, REQ-020).

### Negativas

- `pagamento_desconto` é uma tabela muito grande (~1,4 bi linhas potenciais) que **exige** particionamento por competência, índices e fetch strategy desde o início — não é opcional.
- Carregar pagamento + descontos exige fetch join / `@BatchSize` deliberados para evitar N+1.

## Requisitos Relacionados

- **REQ-009** — Limite de 5 dependentes (`GRP-DEPENDENTE`)
- **REQ-019** — Fator regional por tabela (`GRP-PARAM-REGIONAL`)
- **REQ-020** — Fator de renda por faixa (`GRP-FAIXA-CALCULO`)
- **REQ-025, REQ-026, REQ-027** — Descontos (`GRP-DESCONTO`), em conjunto com ADR-001 (tipos aplicáveis)

---

**Lembrete de Definição de Pronto:** Formato MADR · ≥2 opções com prós/contras específicos da equipe · decisão datada · consequências positivas e negativas · REQ-IDs relacionados.

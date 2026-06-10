<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-001: Map Adabas MU fields to JSONB vs ElementCollection

![ESTÁGIO 02 Spec Moderna](https://img.shields.io/badge/ESTÁGIO-02%20Spec%20Moderna-7FBA00?style=for-the-badge) ![TIPO ADR](https://img.shields.io/badge/TIPO-ADR-1A1A1A?style=for-the-badge) ![AGENTE architect](https://img.shields.io/badge/AGENTE-@architect--agent-0078D4?style=for-the-badge)

> Produzido por `/generate-adr title="Map Adabas MU fields to JSONB vs ElementCollection"`.
> Formato MADR. Fundamentado nos DDMs reais ([adabas-ddms/](../../01-arqueologia/legado-sifap/adabas-ddms/)) e na [SPECIFICATION.md](../SPECIFICATION.md).

## Status

🟡 **Proposta — pendente de ratificação da equipe** (Software Architect + DBA). A análise está completa; a equipe confirma a opção escolhida.

## Data

2026-06-10

## Contexto

O legado SIFAP usa dois construtos repetitivos do Adabas que não existem no modelo relacional do PostgreSQL 16 (stack-alvo):

- **MU (Multiple-Value field)** — um campo escalar que armazena **N ocorrências do mesmo tipo**. No legado há **um** caso real: `PROGRAMA-SOCIAL.TIPO-DSCT-APLIC` (formato `MU A 3`, máx **8** ocorrências) — a lista de códigos de tipo de desconto válidos para cada programa (IR, JD, CS, PA, EM, TX, OU, EX). Fonte: [PROGRAMA-SOCIAL.ddm](../../01-arqueologia/legado-sifap/adabas-ddms/PROGRAMA-SOCIAL.ddm) (campo `EA`).
- **PE (Periodic Group)** — um grupo **estruturado** que repete (vários campos por ocorrência): `BENEFICIARIO.GRP-DEPENDENTE` (máx 10), `PROGRAMA-SOCIAL.GRP-FAIXA-CALCULO` (máx 5), `PROGRAMA-SOCIAL.GRP-PARAM-REGIONAL` (máx 6), `PAGAMENTO.GRP-DESCONTO` (máx 8).

**Esta decisão trata apenas dos campos MU (escalar multi-valor).** Os grupos PE (estruturados) são objeto de uma decisão separada, pois mapeiam naturalmente para `@OneToMany`/`@ElementCollection` de um `@Embeddable` — ver [Consequências → Escopo](#escopo-e-fronteiras) e Próximos Passos.

A decisão precisa ser tomada **agora** porque a modelagem JPA do contexto **Catálogo de Programas Sociais** depende dela, e os requisitos de desconto (REQ-025, REQ-026, REQ-027) precisam consultar "este tipo de desconto é aplicável a este programa?". O `TIPO-DSCT-APLIC` é um **vocabulário controlado de baixa cardinalidade** sobre uma tabela de **referência (~45 registros)**, lida inteira a cada cálculo.

## Opções Consideradas

### Opção 1: `@ElementCollection` (tabela de junção relacional)

Mapear o campo MU como uma coleção de valores escalares numa tabela filha dedicada:
`programa_tipo_desconto (cod_programa FK, tipo_desconto)`, via `@ElementCollection` + `@CollectionTable`.

- **Prós (no contexto SIFAP):**
  - Os tipos de desconto são um **vocabulário controlado** (8 códigos fixos); uma tabela relacional permite **integridade referencial** e até um `CHECK`/FK para uma tabela de domínio de tipos — exatamente o que REQ-025/026/027 precisam para validar tipos aplicáveis.
  - **Consultável com SQL padrão** e portável: "quais programas permitem `JD`?" vira um `JOIN`/`IN` trivial e indexável — útil para auditoria e relatórios sem operadores proprietários.
  - Cardinalidade mínima (45 programas × ≤8 = ~360 linhas no total) → o "custo" de uma tabela de junção é irrelevante; sem risco real de N+1 nesse volume.
  - Tipagem forte no Java (`Set<TipoDesconto>` com enum), alinhada à unificação de domínio que a spec exige (de-para de tipos código×DDM apontado no dependency-map).
- **Contras (no contexto SIFAP):**
  - Introduz uma tabela extra e um `JOIN`/fetch adicional ao carregar o programa (mitigável com `@BatchSize`/fetch join; trivial para 45 registros).
  - Levemente mais verboso que persistir um único campo.
- **Risco:** Praticamente nulo dado o volume; o único risco é over-engineering se nunca consultarmos por tipo — mas a spec já consulta.
- **Esforço:** same (mapeamento JPA padrão, sem dependências extras).

### Opção 2: Coluna JSONB (`jsonb` no PostgreSQL)

Persistir o MU como um array JSON numa coluna `jsonb` do próprio `programa_social` (ex.: `["IR","JD","PA"]`), via Hibernate 6 `@JdbcTypeCode(SqlTypes.JSON)`.

- **Prós (no contexto SIFAP):**
  - **Leitura em uma única linha** — nenhum `JOIN`; o array vem junto do registro do programa (que é sempre lido inteiro no cálculo).
  - Flexível a mudanças de vocabulário sem migração de schema (novo tipo de desconto = só um valor a mais no array).
  - PostgreSQL 16 suporta `jsonb` nativamente, com operadores de contención (`@>`) e índices GIN se necessário.
- **Contras (no contexto SIFAP):**
  - **Perde integridade referencial**: nada impede gravar `"ZZ"` (tipo inexistente); a validação do vocabulário controlado teria de viver só no código, contrariando o objetivo de unificar a de-para de tipos divergentes (código×DDM).
  - Consultas por membresia exigem **operadores jsonb não-portáveis** (`@>`) e índices GIN — mais complexo e menos legível que um `IN` relacional para algo tão simples.
  - **Tipagem fraca**: o array é texto livre; perde-se a checagem em tempo de compilação que um `Set<TipoDesconto>` daria.
  - JSONB brilha em estruturas **document-like, de alta cardinalidade e write-whole/read-whole** — não é o caso de um vocabulário fixo de 8 códigos sobre 45 registros.
- **Risco:** Dados sujos de tipos inválidos passando despercebidos (sem FK), e queries de relatório/auditoria mais frágeis. Em uma tabela de referência crítica do SENARC, integridade > flexibilidade.
- **Esforço:** same/lower no mapeamento, higher na validação e nas consultas.

## Decisão

**Escolhida: Opção 1 — `@ElementCollection`** (com enum de domínio `TipoDesconto`).

**Razão (uma frase):** O único campo MU do legado é um **vocabulário controlado de baixíssima cardinalidade sobre uma tabela de referência**, onde integridade referencial e consultabilidade SQL padrão (exigidas por REQ-025/026/027 e pela unificação da de-para de tipos) superam a flexibilidade de schema do JSONB — que não traz benefício real neste volume.

> JSONB **não** é descartado para o projeto: ele é a opção a reavaliar para estruturas PE de **alto volume** (notadamente `PAGAMENTO.GRP-DESCONTO` sobre ~180 mi registros), em ADR separado.

## Consequências

### Positivas

- Tipos de desconto aplicáveis ganham **integridade referencial** (FK a uma tabela/enum de domínio), eliminando o risco de tipos inválidos — base para a unificação da de-para código×DDM.
- Consultas "quais programas permitem o tipo X" ficam triviais, portáveis e indexáveis (SQL padrão), úteis a relatórios e auditoria.
- Modelo Java fortemente tipado (`Set<TipoDesconto>`), coerente com o objetivo de domínio limpo do contexto Programas.
- Mapeamento JPA convencional, sem dependências nem operadores proprietários.

### Negativas

- Uma tabela de junção extra (`programa_tipo_desconto`) e um fetch adicional ao carregar o programa — impacto desprezível no volume (≤360 linhas), mas requer atenção a fetch strategy.
- Mudança no vocabulário de tipos exige migração da tabela de domínio (vs. apenas anexar ao array JSON) — aceitável, pois é uma mudança rara e que **deve** ser controlada.

### Escopo e fronteiras

- **Aplica-se a:** campos **MU escalares**. No legado, apenas `PROGRAMA-SOCIAL.TIPO-DSCT-APLIC`.
- **NÃO se aplica a:** grupos **PE estruturados** (`GRP-DEPENDENTE`, `GRP-FAIXA-CALCULO`, `GRP-PARAM-REGIONAL`, `GRP-DESCONTO`), que mapeiam para `@OneToMany`/`@ElementCollection` de `@Embeddable` ou, no caso de alto volume, são candidatos a JSONB — decisão própria.

## Requisitos Relacionados

- **REQ-025** — Contribuição social progressiva por faixa de bruto (depende dos tipos de desconto aplicáveis)
- **REQ-026** — Teto de descontos 30% com isenção judicial (precisa identificar tipo 'JD'/judicial de forma confiável)
- **REQ-027** — Cálculo do desconto judicial (membresia de tipo)
- Relacionados ao escopo PE (decisão futura): **REQ-009** (limite de 5 dependentes), **REQ-019/REQ-020** (fatores regionais/faixas de renda)

---

**Lembrete de Definição de Pronto:** Formato MADR · ≥2 opções com prós/contras específicos da equipe · decisão datada · consequências positivas e negativas · REQ-IDs relacionados.

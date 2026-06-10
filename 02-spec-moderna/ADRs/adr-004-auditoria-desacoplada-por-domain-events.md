<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-004: Auditoria desacoplada por domain events e remoção do encobrimento de exclusões

![ESTÁGIO 02 Spec Moderna](https://img.shields.io/badge/ESTÁGIO-02%20Spec%20Moderna-7FBA00?style=for-the-badge) ![TIPO ADR](https://img.shields.io/badge/TIPO-ADR-1A1A1A?style=for-the-badge) ![AGENTE architect](https://img.shields.io/badge/AGENTE-@architect--agent-0078D4?style=for-the-badge)

> Produzido por `/generate-adr`. Formato MADR. Resolve [OQ-03](../SPECIFICATION.md#open-questions-não-são-requisitos-ainda). Fundamentado em [AUDITORIA.ddm](../../01-arqueologia/legado-sifap/adabas-ddms/), [RELAUDIT.NSN / BATCHCON.NSN](../../01-arqueologia/business-rules-catalog.md) e no [bounded-contexts.md](../bounded-contexts.md) (contexto Auditoria & Conformidade).

## Status

🟡 **Proposta — pendente de ratificação da equipe** (Software Architect + Product Owner + Auditoria/Jurídico). A análise está completa.

## Data

2026-06-10

## Contexto

O contexto **Auditoria & Conformidade** é dono de `AUDITORIA` (FNR 153, ~25 mi reg., imutável, retenção 10 anos por IN-TCU 63/2010). No legado a auditoria tem dois problemas:

1. **Acoplamento de escrita:** a trilha é gravada **inline** pelos programas (ex.: `BATCHCON` grava ações 'CO'/'DV' diretamente em `AUDITORIA`) — cada programa que muta negócio também escreve auditoria, espalhando a responsabilidade.
2. **Encobrimento (compliance):** `RELAUDIT` **exclui deliberadamente** eventos de exclusão ('EX') do relatório ("FILTRO ACAO - EXCLUSOES NAO SAO EXIBIDAS"), reforçado pela alteração "LIMPEZA RELATORIO" de 2014 — um vetor de encobrimento ([MYS-S1](../../01-arqueologia/discovery-report.md#41-mistérios-não-resolvidos)).

O [bounded-contexts.md](../bounded-contexts.md) já decidiu que Auditoria é um **contexto próprio append-only** que **não muta negócio**. Esta ADR decide **como** os outros contextos alimentam a trilha e **confirma** que o encobrimento de exclusões é removido. REQ-036 (gravar CO/DV) e REQ-037 (consulta com exclusões visíveis) dependem desta decisão. O Princípio V da [constituição](../../.specify/memory/constitution.md) exige que backdoors/encobrimentos não sejam reimplementados sem decisão explícita — esta é a decisão.

## Opções Consideradas

### Opção 1: Auditoria por domain events (one-way), trilha append-only, exclusões sempre visíveis

Os contextos `payments` e `beneficiaries` **publicam domain events** in-process (Spring `ApplicationEventPublisher` / Spring Modulith event publication); o módulo `audit` os consome via `@ApplicationModuleListener` e **acrescenta** registros imutáveis. O relatório de auditoria **não filtra** exclusões.

- **Prós (no contexto SIFAP):**
  - **Desacopla** a auditoria do negócio: os contextos de domínio não conhecem o esquema de `AUDITORIA` — só publicam fatos (`PagamentoConciliadoEvent`, `BeneficiarioExcluidoEvent`). Respeita o Princípio IV (fronteiras de módulo).
  - A trilha é a **única fonte append-only**; remover o filtro de 'EX' resolve o encobrimento por construção (REQ-037), atendendo IN-TCU 63 e o Princípio V (sem reimplementar encobrimentos).
  - Spring Modulith oferece **event publication registry** com reentrega — eventos não se perdem se o listener falhar, dando garantia de durabilidade da trilha sem acoplar transações de negócio.
  - Auditoria evolui em cadência própria (regulatória) sem tocar os contextos produtores.
- **Contras (no contexto SIFAP):**
  - Consistência **eventual** entre o fato de negócio e o registro de auditoria (janela curta) — aceitável para trilha, não para o dado de negócio em si.
  - Requer infraestrutura de eventos (Spring Modulith event registry + tabela de publicação) e disciplina para publicar todo fato auditável.
- **Risco:** Um fato auditável que ninguém publica não é auditado. Mitigação: cobertura por testes de evento por REQ e revisão de PR (Princípio III).
- **Esforço:** same/higher (infra de eventos), mas alinhado ao design já escrito em [modular-monolith-design.md](../modular-monolith-design.md).

### Opção 2: Auditoria por chamada síncrona direta a um `AuditService`

Cada contexto chama, dentro da própria transação, um `AuditoriaApi.registrar(evento)` exposto pelo módulo `audit`.

- **Prós (no contexto SIFAP):**
  - **Atômico**: o registro de auditoria participa da mesma transação do fato de negócio — sem janela de consistência eventual.
  - Mais simples de raciocinar (chamada direta, sem infraestrutura de eventos).
- **Contras (no contexto SIFAP):**
  - **Acopla** `payments`/`beneficiaries` à interface de auditoria — todos passam a depender de `audit` de forma síncrona, recriando o espalhamento de responsabilidade do legado.
  - Uma falha/lentidão no `audit` impacta a transação de negócio (a folha de ~180 mi registros não pode travar por causa da trilha).
  - Auditoria deixa de ser puramente reativa; o Princípio IV (comunicação preferencialmente desacoplada para notificações) é enfraquecido.
- **Risco:** Auditoria no caminho crítico da folha mensal vira ponto de falha/contengência de performance.
- **Esforço:** lower de infra, higher de acoplamento e risco operacional.

## Decisão

**Escolhida: Opção 1 — auditoria por domain events one-way, trilha append-only, exclusões sempre visíveis** (via Spring Modulith event publication registry).

**Razão (uma frase):** A trilha de auditoria deve ser desacoplada, durável e fora do caminho crítico da folha; domain events com event registry dão durabilidade sem acoplar transações, e a remoção do filtro de 'EX' (REQ-037) cumpre a obrigação de compliance (IN-TCU 63) e o Princípio V de não reimplementar encobrimentos.

> A consistência eventual é aceitável **porque a auditoria é uma trilha reativa**, não um invariante de negócio; o fato de negócio permanece transacional no seu próprio contexto.

## Consequências

### Positivas

- `audit` é um contexto reativo puro: não muta negócio, não está no caminho crítico, evolui por conta própria (Princípio IV).
- Encobrimento de exclusões eliminado por design — exclusões ('EX') sempre aparecem na consulta (REQ-037), atendendo TCU/IN-63 e a constituição (Princípio V).
- Event registry do Spring Modulith garante reentrega → trilha durável mesmo com falha transitória do listener.

### Negativas

- Janela de consistência eventual entre o fato e seu registro de auditoria (curta, monitorável).
- Exige infraestrutura de eventos e disciplina: todo fato auditável precisa de um evento publicado e de teste de evento associado.

## Requisitos Relacionados

- **REQ-036** — Trilha de auditoria para conciliação e divergência (eventos `PagamentoConciliado`/`DivergenciaConciliacao`)
- **REQ-037** — Consulta da trilha com exclusões visíveis (remoção do encobrimento)
- Resolve **OQ-03**; relaciona-se a **OQ-S1** (backdoor de encobrimento)

---

**Lembrete de Definição de Pronto:** Formato MADR · ≥2 opções com prós/contras específicos da equipe · decisão datada · consequências positivas e negativas · REQ-IDs relacionados.

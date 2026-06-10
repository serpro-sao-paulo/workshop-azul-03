<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-002: Máquina de status única do PAGAMENTO

![ESTÁGIO 02 Spec Moderna](https://img.shields.io/badge/ESTÁGIO-02%20Spec%20Moderna-7FBA00?style=for-the-badge) ![TIPO ADR](https://img.shields.io/badge/TIPO-ADR-1A1A1A?style=for-the-badge) ![AGENTE architect](https://img.shields.io/badge/AGENTE-@architect--agent-0078D4?style=for-the-badge)

> Produzido por `/generate-adr`. Formato MADR. Resolve [OQ-01](../SPECIFICATION.md#open-questions-não-são-requisitos-ainda). Fundamentado em [PAGAMENTO.ddm](../../01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm), [dependency-map.md](../../01-arqueologia/dependency-map.md) e nas notas dos programas em [business-rules-catalog.md](../../01-arqueologia/business-rules-catalog.md).

## Status

🟡 **Proposta — pendente de ratificação da equipe** (Software Architect + Product Owner + gestor SENARC/CGPB). A análise está completa; a equipe confirma o conjunto canônico de estados.

## Data

2026-06-10

## Contexto

O `PAGAMENTO` (FNR 152, ~180 mi registros) é o hub do legado SIFAP: **8 programas o tocam** sem orquestração, e o campo `SIT-PAGAMENTO` está em **conflito total entre o DDM e o código** ([MYS-024](../../01-arqueologia/mysteries-found.md)):

- **DDM** define: `P=PEND, G=GERADO, E=EMITIDO, C=CONFIRMADO, D=DEVOLVIDO, X=CANCELADO, R=REPROCESSADO`.
- **Código** usa: `G=gerado` (BATCHPGT), `P=pago` (BATCHCON ret. '00'), `D=devolvido` (ret. '01'), `E=erro` (BATCHCON ret. '02') **ou** `E=estornado` (BATCHREL/RELPGT), `C=cancelado` (sem programa que o grave), `T=terceiro` (tipo, não status, sem origem).

Ou seja: **'P', 'E' e 'C' têm significados diferentes** conforme o programa, e há estados órfãos na escrita (`C`, `X`, `R`). Interpretar errado o status sobre 180 mi registros é o **maior risco financeiro/contábil** da migração (conciliação, relatórios e estorno dependem disso). A decisão precisa ser tomada **agora** porque REQ-029 (geração), REQ-032 (conciliação) e REQ-035 (relatório) dependem de uma semântica única.

[ADR-001](adr-001-map-adabas-mu-fields-to-jsonb-vs-elementcollection.md) e o [bounded-contexts.md](../bounded-contexts.md) já definiram que **Pagamentos é o dono único** de `PAGAMENTO` — esta ADR decide **como** modelar o estado.

## Opções Consideradas

### Opção 1: Enum de domínio com transições explícitas, owner único (Pagamentos)

Modelar `StatusPagamento` como `enum` Java no módulo `payments`, com um conjunto **canônico** de estados e transições válidas codificadas (state machine explícita). Toda mutação passa por `FolhaService`/`ConciliacaoService`/`CorrecaoService`. Os códigos legados ambíguos são **normalizados na migração de dados** via uma tabela de-para documentada.

Conjunto canônico proposto: `GERADO → EMITIDO → PAGO | DEVOLVIDO | ERRO`, com `CANCELADO` e `ESTORNADO` como estados terminais distintos (separando os dois significados de 'E' do legado).

- **Prós (no contexto SIFAP):**
  - **Fonte única de verdade**: elimina a colisão 'E' (erro × estornado) e 'P' (pendente × pago) ao dar **nomes distintos** a estados que o legado fundia num caractere.
  - Transições inválidas (ex.: `PAGO → GERADO`) viram erro em tempo de execução/compilação — impossível reproduzir a mutação descoordenada do legado.
  - O de-para de migração documenta exatamente como cada um dos 180 mi registros legados é reinterpretado, dando rastreabilidade auditável (Princípio I da constituição).
  - Alinha com REQ-032 (conciliação grava P/D/E) e separa REQ-035 (relatório por status) sem ambiguidade.
- **Contras (no contexto SIFAP):**
  - Exige um **mapa de-para legado→canônico revisado por gestor** antes da migração de dados — esforço de validação humana (itens `needs-facilitator`).
  - Estados órfãos legados (`C`, `X`, `R`, tipo `T`) precisam de decisão caso a caso (descartar, mapear ou tratar como dado histórico).
- **Risco:** Um de-para incorreto reinterpreta registros históricos erroneamente. Mitigação: o de-para é versionado, testado com amostras reais e revisado pelo CGPB.
- **Esforço:** higher (state machine + de-para validado), mas o esforço é **inerente ao problema**, não acidental.

### Opção 2: Preservar os códigos legados de 1 caractere como `String`/`char`

Migrar `SIT-PAGAMENTO` como está (um `char`), mantendo os códigos `G/P/E/C/D/...` e resolvendo o significado no código de quem lê (como o legado faz).

- **Prós (no contexto SIFAP):**
  - Migração de dados trivial (cópia direta do caractere) — zero transformação.
  - Nenhum de-para a validar com gestores no curto prazo.
- **Contras (no contexto SIFAP):**
  - **Perpetua a ambiguidade** que é justamente o risco crítico ([MYS-024](../../01-arqueologia/mysteries-found.md)): 'E' continuaria significando erro ou estornado conforme o leitor.
  - Viola o Princípio IV (fonte única de verdade do dono do dado) e o objetivo declarado no discovery-report (§5.3) de **redesenhar** a máquina de status.
  - Tipagem fraca: nada impede gravar um status inexistente; relatórios contábeis continuam frágeis.
- **Risco:** Reintroduz, no sistema novo, exatamente o bug de 180 mi registros que a migração deveria eliminar.
- **Esforço:** lower agora, **muito higher depois** (dívida técnica reincidente).

## Decisão

**Escolhida: Opção 1 — enum de domínio com transições explícitas e owner único (Pagamentos)**, com de-para de migração validado por gestor.

**Razão (uma frase):** A ambiguidade do status sobre 180 mi registros é o maior risco da migração; só um conjunto canônico de estados com nomes distintos (separando erro × estornado, pendente × pago) e transições explícitas, sob um dono único, elimina o defeito em vez de transportá-lo.

## Consequências

### Positivas

- Colisões 'E' e 'P' eliminadas por construção; conciliação, estorno e relatórios passam a ter semântica inequívoca (REQ-032, REQ-035).
- Mutações de estado centralizadas em Pagamentos → fim da mutação descoordenada por 8 programas.
- De-para de migração versionado e auditável atende ao Princípio I (rastreabilidade) e ao gate `legacy-traceability`.

### Negativas

- Requer validação humana do de-para legado→canônico (gestores SENARC/CGPB) antes da migração — bloqueante de cronograma de dados.
- Estados órfãos do legado (`C`, `X`, `R`, tipo `T`) exigem decisões individuais documentadas.

## Requisitos Relacionados

- **REQ-029** — Geração da folha (status inicial `GERADO`)
- **REQ-032** — Conciliação bancária (transições para `PAGO`/`DEVOLVIDO`/`ERRO`)
- **REQ-035** — Relatório consolidado por status
- Resolve **OQ-01**; relaciona-se a OQ (tipo de pagamento 'T' órfão)

---

**Lembrete de Definição de Pronto:** Formato MADR · ≥2 opções com prós/contras específicos da equipe · decisão datada · consequências positivas e negativas · REQ-IDs relacionados.

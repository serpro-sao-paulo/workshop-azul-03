<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# SPECIFICATION — SIFAP Moderno

![ESTÁGIO 02 Spec Moderna](https://img.shields.io/badge/ESTÁGIO-02%20Spec%20Moderna-7FBA00?style=for-the-badge) ![TIPO Especificação EARS](https://img.shields.io/badge/TIPO-Especificação%20EARS-1A1A1A?style=for-the-badge) ![AGENTE architect](https://img.shields.io/badge/AGENTE-@architect--agent-0078D4?style=for-the-badge)

> Produzido por `/write-ears-spec rules=01-arqueologia/business-rules-catalog.md contexts=02-spec-moderna/bounded-contexts.md`.
> Traduz **regras confirmadas** do [business-rules-catalog.md](../01-arqueologia/business-rules-catalog.md) em requisitos EARS, agrupados pelos 4 bounded contexts decididos em [bounded-contexts.md](bounded-contexts.md).
>
> **Regras de inclusão:** somente regras classificadas **Confirmada** viram requisito. Mistérios, backdoors e divergências ficam em [Open Questions](#open-questions-não-são-requisitos-ainda) — nunca como requisito. Toda EARS carrega `source_legacy:` e cita a regra-fonte.

**EARS patterns:** Ubiquitous · Event-driven · State-driven · Optional · Unwanted · Complex.

---

## Bounded Context: Gestão de Beneficiários

### REQ-001: CPF obrigatório e válido por Módulo 11

- **EARS Pattern:** Unwanted
- **Declaração:** Se o CPF informado for ausente (zero) ou reprovar o algoritmo Módulo 11 (dois dígitos verificadores), então o sistema deverá rejeitar o cadastro com erro de validação.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L103-L115`
- **Source Rule:** CADBENEF regras #2, #3 (Confirmada)
- **Critérios de Aceite:**
  - [ ] Given um cadastro com CPF = 0, when submetido, then o sistema rejeita com "CPF obrigatório".
  - [ ] Given um CPF com dígito verificador incorreto, when validado, then o sistema rejeita por DV inválido.
  - [ ] Given um CPF válido por Módulo 11, when validado, then o sistema aceita a validação de CPF.

### REQ-002: Validação acumulativa de cadastro (não curto-circuito)

- **EARS Pattern:** Unwanted
- **Declaração:** Se um cadastro de beneficiário contiver múltiplos campos inválidos (CPF, data de nascimento, nome ou UF), então o sistema deverá acumular e retornar todas as mensagens de erro, em vez de parar na primeira.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L113-L165`
- **Source Rule:** VALBENEF regras #1, #3, #4, #5 (Confirmada)
- **Critérios de Aceite:**
  - [ ] Given um cadastro com CPF inválido E nome sem sobrenome, when validado, then ambos os erros são retornados juntos.
  - [ ] Given um nome sem ao menos um espaço (sem sobrenome), when validado, then o sistema rejeita por nome incompleto.
  - [ ] Given uma UF fora das 27 unidades federativas, when validada, then o sistema rejeita por UF inválida.

### REQ-003: Rejeição de CPF com dígitos repetidos

- **EARS Pattern:** Unwanted
- **Declaração:** Se o CPF informado tiver todos os dígitos iguais (ex.: 111.111.111-11), então o sistema deverá rejeitá-lo como inválido.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L188-L205`
- **Source Rule:** VALBENEF regra #2 (Confirmada)
- **Critérios de Aceite:**
  - [ ] Given o CPF 111.111.111-11, when validado, then o sistema o rejeita mesmo que o DV seja matematicamente consistente.
  - [ ] Given um CPF com dígitos variados e DV válido, when validado, then o sistema o aceita.

### REQ-004: Campos obrigatórios do beneficiário

- **EARS Pattern:** Unwanted
- **Declaração:** Se nome ou data de nascimento estiverem ausentes, então o sistema deverá rejeitar o cadastro do beneficiário.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L117-L127`
- **Source Rule:** CADBENEF regras #4, #5 (Confirmada)
- **Critérios de Aceite:**
  - [ ] Given um cadastro sem nome, when submetido, then o sistema rejeita por nome obrigatório.
  - [ ] Given um cadastro sem data de nascimento, when submetido, then o sistema rejeita por data de nascimento obrigatória.

### REQ-005: Unicidade de CPF na inclusão

- **EARS Pattern:** Unwanted
- **Declaração:** Se uma inclusão usar um CPF já cadastrado, então o sistema deverá rejeitar com "beneficiário já cadastrado".
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L141-L145`
- **Source Rule:** CADBENEF regra #7 (Confirmada)
- **Critérios de Aceite:**
  - [ ] Given um CPF já existente, when uma inclusão é submetida, then o sistema rejeita por duplicidade.
  - [ ] Given um CPF inexistente, when uma inclusão é submetida, then o sistema permite o cadastro.

### REQ-006: Status inicial ativo na inclusão

- **EARS Pattern:** Ubiquitous
- **Declaração:** O sistema deverá atribuir status 'A' (ativo) a todo beneficiário no momento da inclusão.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L160-L162`
- **Source Rule:** CADBENEF regra #9 (Confirmada)
- **Critérios de Aceite:**
  - [ ] Given uma inclusão válida, when persistida, then o status do beneficiário é 'A'.
  - [ ] Given um beneficiário recém-incluído, when consultado, then ele aparece como ativo.

### REQ-007: Status permitidos do beneficiário

- **EARS Pattern:** Unwanted
- **Declaração:** Se um status de beneficiário fora do conjunto {A, S, C, I, D} for informado, então o sistema deverá rejeitá-lo.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L168-L175`
- **Source Rule:** VALBENEF regra #6 (Confirmada); CONSBENF regra #3 (tradução A/S/C/I/D)
- **Critérios de Aceite:**
  - [ ] Given um status 'X', when validado, then o sistema o rejeita.
  - [ ] Given cada status válido (A/S/C/I/D), when traduzido para exibição, then retorna ATIVO/SUSPENSO/CANCELADO/INATIVO/DESLIGADO respectivamente.

### REQ-008: Suspensão automática acima de 75 anos

- **EARS Pattern:** Event-driven
- **Declaração:** Quando a idade do beneficiário for maior que 75 anos no cadastro, o sistema deverá definir seu status como 'S' (suspenso).
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L165-L167`
- **Source Rule:** CADBENEF regra #10 (Confirmada)
- **Nota:** Comportamento contra-intuitivo (idoso → suspenso) — confirmar a intenção de negócio com gestor (ver [Open Questions](#open-questions-não-são-requisitos-ainda) OQ-08). O comportamento legado é preservado como baseline.
- **Critérios de Aceite:**
  - [ ] Given um beneficiário com 76 anos, when cadastrado, then o status resultante é 'S'.
  - [ ] Given um beneficiário com 75 anos ou menos, when cadastrado, then o status não é alterado para 'S' por idade.

### REQ-009: Limite de 5 dependentes

- **EARS Pattern:** State-driven
- **Declaração:** Enquanto um beneficiário já tiver 5 dependentes cadastrados, o sistema deverá impedir a inclusão de novos dependentes.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L61-L64`
- **Source Rule:** CADDEPEND regra #3 (Confirmada — resolve MYS-007; a doc dizia 3, o código impõe 5)
- **Critérios de Aceite:**
  - [ ] Given um beneficiário com 5 dependentes, when uma 6ª inclusão é tentada, then o sistema rejeita por limite atingido.
  - [ ] Given um beneficiário com 4 dependentes, when uma inclusão é tentada, then o sistema aceita.

### REQ-010: Bloqueio de dependentes para titular cancelado/desligado

- **EARS Pattern:** Event-driven
- **Declaração:** Quando o titular estiver com status 'C' (cancelado) ou 'D' (desligado), o sistema deverá recusar a inclusão de dependentes.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L54-L57`
- **Source Rule:** CADDEPEND regra #2 (Confirmada)
- **Critérios de Aceite:**
  - [ ] Given um titular com status 'C', when uma inclusão de dependente é tentada, then o sistema rejeita.
  - [ ] Given um titular com status 'A', when uma inclusão de dependente é tentada, then o sistema aceita (respeitando REQ-009).

### REQ-011: Parentesco válido do dependente

- **EARS Pattern:** Unwanted
- **Declaração:** Se o parentesco do dependente não for um de {FI, CO, IR, OU}, então o sistema deverá rejeitar a inclusão.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L83-L88`
- **Source Rule:** CADDEPEND regra #5 (Confirmada)
- **Critérios de Aceite:**
  - [ ] Given parentesco 'XX', when submetido, then o sistema rejeita.
  - [ ] Given parentesco 'FI', when submetido, then o sistema aceita.

### REQ-012: Elegibilidade por status do beneficiário

- **EARS Pattern:** Event-driven
- **Declaração:** Quando o status do beneficiário for 'S', 'C', 'D' ou 'I', o sistema deverá declará-lo inelegível, registrando o motivo correspondente.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L117-L131`
- **Source Rule:** VALELEG regras #5, #6, #7 (Confirmada)
- **Critérios de Aceite:**
  - [ ] Given um beneficiário com status 'S', when avaliada a elegibilidade, then resultado = inelegível com motivo "suspenso".
  - [ ] Given um beneficiário com status 'A', when avaliada a elegibilidade, then o status não causa inelegibilidade.

### REQ-013: Elegibilidade por faixa etária e renda do programa

- **EARS Pattern:** Event-driven
- **Declaração:** Quando o programa definir idade mínima, idade máxima ou renda máxima, o sistema deverá declarar inelegível o beneficiário que estiver fora desses limites.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L138-L162`
- **Source Rule:** VALELEG regras #8, #9, #10 (Confirmada)
- **Critérios de Aceite:**
  - [ ] Given idade mínima 60 e beneficiário com 58 anos, when avaliado, then inelegível por idade mínima.
  - [ ] Given renda máxima 1000 e renda familiar 1200, when avaliado, then inelegível por renda.
  - [ ] Given limites = 0 (não definidos), when avaliado, then o limite correspondente não é aplicado.

### REQ-014: Elegibilidade por tipo de programa

- **EARS Pattern:** Event-driven
- **Declaração:** Quando o tipo de programa for 'A' (assistencial), 'P' (previdenciário) ou 'T' (trabalho), o sistema deverá aplicar as regras específicas do tipo — tipo 'P' exige idade ≥ 60; tipo 'T' exige idade entre 16 e 65; tipo 'A' exige documentação regular e, com renda acima de 600 sem dependentes, declara inelegível.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L167-L193`
- **Source Rule:** VALELEG regras #11, #12, #13 (Confirmada)
- **Critérios de Aceite:**
  - [ ] Given programa tipo 'P' e beneficiário com 59 anos, when avaliado, then inelegível.
  - [ ] Given programa tipo 'T' e beneficiário com 70 anos, when avaliado, then inelegível.
  - [ ] Given programa tipo 'A', renda 800 e sem dependentes, when avaliado, then inelegível por renda.

### REQ-015: Mascaramento de CPF na exibição

- **EARS Pattern:** Ubiquitous
- **Declaração:** O sistema deverá mascarar o CPF em toda exibição e relatório, ocultando os dígitos sensíveis de forma consistente entre telas e relatórios.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN#L150-L153`
- **Source Rule:** CONSBENF regra #2; RELPGT regra #3 (Confirmada)
- **Nota:** O legado tem máscara **inconsistente** (CONSBENF expõe 7-11; RELPGT expõe 4-11) e um bug LGPD conhecido em CPF curto — ver [OQ-09](#open-questions-não-são-requisitos-ainda). A versão moderna **unifica** uma única máscara correta.
- **Critérios de Aceite:**
  - [ ] Given um CPF de 11 dígitos, when exibido, then a máscara oculta os dígitos sensíveis no mesmo padrão em telas e relatórios.
  - [ ] Given um CPF armazenado com menos de 11 dígitos, when exibido, then ele é normalizado para 11 dígitos antes de mascarar (sem expor os primeiros dígitos).

---

## Bounded Context: Catálogo de Programas Sociais

### REQ-016: Unicidade do código de programa

- **EARS Pattern:** Unwanted
- **Declaração:** Se uma inclusão de programa usar um código já existente, então o sistema deverá rejeitar com "programa já cadastrado".
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L82-L90`
- **Source Rule:** CADPROG regra #2 (Inferida — promovida por decisão da equipe: invariante de identidade do agregado; baixo risco)
- **Critérios de Aceite:**
  - [ ] Given um código de programa existente, when uma inclusão é submetida, then o sistema rejeita.
  - [ ] Given um código novo, when uma inclusão é submetida, then o sistema aceita.

### REQ-017: Ajuste do valor base pelo Fator-K na inclusão do programa

- **EARS Pattern:** Ubiquitous
- **Declaração:** O sistema deverá, na inclusão de um programa, persistir o valor base já ajustado pelo Fator-K, onde Fator-K = 1.00 + (fator de reajuste × constante de correção), e o valor base armazenado = valor base informado × Fator-K.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L93-L95`
- **Source Rule:** CADPROG regra #3 (Confirmada — resolve MYS-001)
- **Nota:** A constante de correção legada (`0.347215`) não tem origem documentada e o ajuste interage com o reajuste mensal de REQ-021 — risco de **dupla aplicação** ([OQ-04](#open-questions-não-são-requisitos-ainda)). A constante deve ser externalizada como parâmetro e validada antes da migração de dados.
- **Critérios de Aceite:**
  - [ ] Given um valor base informado e um fator de reajuste, when o programa é incluído, then o valor base persistido = informado × (1.00 + reajuste × constante), truncado conforme REQ-020.
  - [ ] Given um programa já com valor base ajustado, when recalculado o benefício, then o Fator-K NÃO é reaplicado (evitar dupla aplicação).

---

## Bounded Context: Pagamentos & Folha

### REQ-018: Cálculo do benefício mensal por fatores

- **EARS Pattern:** Ubiquitous
- **Declaração:** O sistema deverá calcular o benefício mensal como valor base × fator regional × fator familiar × fator de renda × fator idade × (1 + fator de reajuste).
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L224-L228`
- **Source Rule:** CALCBENF regras #9, #10 (Confirmada). **Regra única** que substitui a duplicação CALCBENF × BATCHPGT (ver [OQ-02](#open-questions-não-são-requisitos-ainda)).
- **Critérios de Aceite:**
  - [ ] Given todos os fatores = 1.0 e reajuste = 0, when calculado, then o benefício = valor base.
  - [ ] Given fator regional 1.2 e demais 1.0, when calculado, then o benefício = valor base × 1.2.
  - [ ] Given os mesmos insumos no fluxo online e no fluxo batch, when calculados, then produzem resultado idêntico.

### REQ-019: Fator regional por tabela parametrizada

- **EARS Pattern:** Event-driven
- **Declaração:** Quando a região do beneficiário estiver no intervalo de regiões parametrizadas, o sistema deverá aplicar o fator regional correspondente; caso contrário, deverá aplicar o fator neutro 1.0000.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L180-L184`
- **Source Rule:** CALCBENF regra #5 (Confirmada)
- **Nota:** A tabela regional era hardcoded (27 posições); a versão moderna a externaliza como parâmetro.
- **Critérios de Aceite:**
  - [ ] Given uma região com fator 1.15 na tabela, when calculado, then aplica 1.15.
  - [ ] Given uma região fora da tabela, when calculado, then aplica 1.0000.

### REQ-020: Fator de renda por faixa

- **EARS Pattern:** Ubiquitous
- **Declaração:** O sistema deverá atribuir o fator de renda pela primeira faixa cujo teto seja maior ou igual à renda familiar (≤300→1.0000; ≤600→0.8500; ≤1000→0.7000; ≤1500→0.5500; acima→0.4000).
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L120-L129`
- **Source Rule:** CALCBENF regra #8 (Confirmada)
- **Critérios de Aceite:**
  - [ ] Given renda familiar 250, when calculado, then fator de renda = 1.0000.
  - [ ] Given renda familiar 900, when calculado, then fator de renda = 0.7000.

### REQ-021: Truncamento a 2 casas decimais

- **EARS Pattern:** Ubiquitous
- **Declaração:** O sistema deverá truncar (não arredondar) todos os valores monetários para 2 casas decimais.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L231-L232`
- **Source Rule:** CALCBENF regra #11; CALCDSCT regra #13 (Confirmada)
- **Nota:** BATCHREL **arredondava** ao totalizar, divergindo dos cálculos — ver [OQ-06](#open-questions-não-são-requisitos-ainda). A política única é **truncar**.
- **Critérios de Aceite:**
  - [ ] Given o valor 123.459, when truncado, then resulta 123.45.
  - [ ] Given o valor 100.001, when truncado, then resulta 100.00 (sem arredondamento para cima).

### REQ-022: Cálculo do 13º em dezembro

- **EARS Pattern:** Event-driven
- **Declaração:** Quando a competência for dezembro (mês 12), o sistema deverá marcar o pagamento como tipo 'D', calcular o 13º como valor base × fator regional × fator idade e somá-lo ao valor bruto.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L240-L246`
- **Source Rule:** CALCBENF regra #12 (Confirmada)
- **Nota:** O 13º legado **omite** fator familiar e fator de renda — manter como baseline e confirmar intenção ([OQ-07](#open-questions-não-são-requisitos-ainda)).
- **Critérios de Aceite:**
  - [ ] Given competência 202612, when calculado, then o tipo de pagamento é 'D'.
  - [ ] Given competência 202606, when calculado, then nenhum 13º é gerado.

### REQ-023: Abono natalino para programa assistencial

- **EARS Pattern:** Event-driven
- **Declaração:** Quando for dezembro e o programa for do tipo 'A', o sistema deverá adicionar abono natalino de 15% sobre o benefício mensal; caso contrário, o abono deverá ser zero.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L249-L257`
- **Source Rule:** CALCBENF regra #13 (Confirmada)
- **Critérios de Aceite:**
  - [ ] Given dezembro e programa tipo 'A' com benefício 1000, when calculado, then abono = 150.
  - [ ] Given dezembro e programa tipo 'P', when calculado, then abono = 0.

### REQ-024: Bloqueio de cálculo para beneficiário inativo

- **EARS Pattern:** Unwanted
- **Declaração:** Se o beneficiário não estiver com status 'A' (ativo), então o sistema deverá rejeitar o cálculo do benefício.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L160-L163`
- **Source Rule:** CALCBENF regra #3 (Confirmada); BATCHPGT regra #4 (Confirmada)
- **Critérios de Aceite:**
  - [ ] Given um beneficiário com status 'S', when o cálculo é solicitado, then o sistema rejeita/ignora.
  - [ ] Given um beneficiário com status 'A', when o cálculo é solicitado, then o sistema processa.

### REQ-025: Contribuição social progressiva por faixa de bruto

- **EARS Pattern:** Ubiquitous
- **Declaração:** O sistema deverá aplicar contribuição social obrigatória por faixa de valor bruto (≤500→3%; ≤1000→5%; ≤2000→7%; acima→9%).
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L57-L66`
- **Source Rule:** CALCDSCT regra #3 (Confirmada)
- **Critérios de Aceite:**
  - [ ] Given bruto 400, when calculada a contribuição, then alíquota = 3%.
  - [ ] Given bruto 1500, when calculada a contribuição, then alíquota = 7%.

### REQ-026: Teto de descontos de 30% com isenção judicial

- **EARS Pattern:** Event-driven
- **Declaração:** Quando o total de descontos não-judiciais exceder 30% do valor bruto, o sistema deverá limitá-lo a 30%; descontos judiciais (tipo 'J') não estão sujeitos a esse teto.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L138-L195`
- **Source Rule:** CALCDSCT regras #4, #7, #12 (Confirmada — resolve MYS-003)
- **Nota:** A pensão alimentícia (tipo 'P') hoje está **sujeita** ao teto, ao contrário do judicial — divergência a validar com jurídico ([OQ-05](#open-questions-não-são-requisitos-ainda)).
- **Critérios de Aceite:**
  - [ ] Given bruto 1000 e descontos não-judiciais somando 400, when aplicado o teto, then o total de não-judiciais é limitado a 300.
  - [ ] Given um desconto judicial de 500 sobre bruto 1000, when aplicado, then ele não é cortado pelo teto de 30%.

### REQ-027: Cálculo do desconto judicial

- **EARS Pattern:** Event-driven
- **Declaração:** Quando o desconto for do tipo judicial ('J'), o sistema deverá usar o valor fixo informado se houver; caso contrário, aplicar o percentual sobre o valor bruto.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L128-L138`
- **Source Rule:** CALCDSCT regra #6 (Confirmada)
- **Critérios de Aceite:**
  - [ ] Given desconto 'J' com valor fixo 200, when calculado, then o desconto = 200.
  - [ ] Given desconto 'J' sem valor fixo e percentual 10% sobre bruto 1000, when calculado, then o desconto = 100.

### REQ-028: Piso zero no valor líquido

- **EARS Pattern:** Unwanted
- **Declaração:** Se o valor líquido resultante for negativo, então o sistema deverá zerá-lo (piso de zero).
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L265-L267`
- **Source Rule:** CALCBENF regra #14 (Inferida — promovida por decisão da equipe: proteção financeira crítica, sem a qual o líquido poderia ser negativo)
- **Critérios de Aceite:**
  - [ ] Given descontos maiores que o bruto, when calculado o líquido, then o líquido = 0.
  - [ ] Given descontos menores que o bruto, when calculado, then o líquido = bruto − descontos.

### REQ-029: Geração da folha mensal por competência

- **EARS Pattern:** Ubiquitous
- **Declaração:** O sistema deverá gerar a folha processando os beneficiários ativos da competência, atribuindo número de pagamento sequencial e status inicial 'G' (gerado), e emitir um resumo com totalizadores e contadores ao final.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L180-L375`
- **Source Rule:** BATCHPGT regras #12, #13 (Confirmada)
- **Critérios de Aceite:**
  - [ ] Given uma competência com N beneficiários ativos, when a folha é gerada, then N pagamentos são criados com status 'G'.
  - [ ] Given a folha gerada, when emitido o resumo, then ele apresenta totais de bruto, desconto, líquido e contadores (processados/gerados/ignorados/erros).

### REQ-030: Idempotência mensal da folha

- **EARS Pattern:** Unwanted
- **Declaração:** Se já existir um pagamento para o beneficiário na mesma competência, então o sistema deverá ignorá-lo na geração da folha (sem pagamento em duplicidade).
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L219-L228`
- **Source Rule:** BATCHPGT regra #5 (Confirmada)
- **Critérios de Aceite:**
  - [ ] Given um beneficiário já com pagamento na competência 202606, when a folha de 202606 roda de novo, then nenhum novo pagamento é criado para ele.
  - [ ] Given um beneficiário sem pagamento na competência, when a folha roda, then um pagamento é criado.

### REQ-031: Resiliência do batch a erros individuais

- **EARS Pattern:** Unwanted
- **Declaração:** Se o programa vinculado a um beneficiário não for encontrado durante a folha, então o sistema deverá registrar o erro e contabilizá-lo, sem interromper o processamento dos demais beneficiários.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L237-L245`
- **Source Rule:** BATCHPGT regra #6 (Confirmada)
- **Critérios de Aceite:**
  - [ ] Given um beneficiário com programa inexistente no meio do lote, when a folha roda, then o erro é contabilizado e o lote continua.
  - [ ] Given o lote finalizado, when emitido o resumo, then o contador de erros reflete os registros com problema.

### REQ-032: Conciliação do retorno bancário CNAB 240

- **EARS Pattern:** Event-driven
- **Declaração:** Quando processar um arquivo de retorno bancário, o sistema deverá considerar apenas registros de detalhe (tipo '3'), casar cada retorno com o pagamento por número de pagamento + CPF + competência e, quando o código de retorno for '00', marcar o pagamento como 'P' (pago) com data e banco; '01' como 'D' (devolvido); '02' como 'E' (erro).
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L130-L218`
- **Source Rule:** BATCHCON regras #1, #3, #5, #6, #7 (Confirmada)
- **Nota:** O significado do status 'E' colide entre conciliação (erro) e relatórios (estornado) — a máquina de status unificada deve ter **fonte única** ([OQ-01](#open-questions-não-são-requisitos-ainda)). O layout CNAB era exclusivo do Banco do Brasil; a versão moderna deve generalizar ([OQ-10](#open-questions-não-são-requisitos-ainda)).
- **Critérios de Aceite:**
  - [ ] Given um registro de retorno '00' que casa com um pagamento, when conciliado, then o status vira 'P' com data e banco preenchidos.
  - [ ] Given um registro de retorno '01', when conciliado, then o status vira 'D'.
  - [ ] Given um registro não-detalhe (tipo ≠ '3'), when processado, then ele é ignorado.

### REQ-033: Detecção de divergência de valor na conciliação

- **EARS Pattern:** Event-driven
- **Declaração:** Quando a diferença entre o valor líquido do sistema e o valor retornado pelo banco for maior que R$ 0,01, o sistema deverá registrar uma divergência e gravar evento de auditoria.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L175-L188`
- **Source Rule:** BATCHCON regra #4 (Confirmada — resolve MYS-009)
- **Critérios de Aceite:**
  - [ ] Given líquido 100.00 e retorno bancário 100.50, when conciliado, then uma divergência é registrada e auditada.
  - [ ] Given líquido 100.00 e retorno 100.005, when conciliado, then a diferença ≤ 0,01 não gera divergência.

### REQ-034: Idempotência da correção monetária

- **EARS Pattern:** State-driven
- **Declaração:** Enquanto um pagamento estiver marcado como já corrigido, o sistema não deverá aplicar nova correção monetária sobre ele.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L170-L172`
- **Source Rule:** CALCCORR regra #3 (Confirmada)
- **Nota:** A correção aplica IPCA acumulado mês a mês; a tabela legada estava **congelada** em 2010-2012 — a versão moderna externaliza os índices ([OQ-11](#open-questions-não-são-requisitos-ainda)).
- **Critérios de Aceite:**
  - [ ] Given um pagamento já corrigido, when a correção roda de novo, then ele é ignorado.
  - [ ] Given um pagamento não corrigido com índice acumulado disponível, when a correção roda, then o valor corrigido é gravado e o pagamento é marcado como corrigido.

### REQ-035: Relatório consolidado por macrorregião e status

- **EARS Pattern:** Event-driven
- **Declaração:** Quando gerar o relatório consolidado, o sistema deverá agrupar pagamentos por macrorregião (Norte, Nordeste, Sudeste, Sul, Centro-Oeste) e totalizar por status de pagamento.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L120-L178`
- **Source Rule:** BATCHREL regras #1, #2 (Confirmada)
- **Critérios de Aceite:**
  - [ ] Given pagamentos em regiões variadas, when o relatório é gerado, then cada pagamento é somado na macrorregião correta.
  - [ ] Given o relatório consolidado, when totalizado, then há subtotais por status e total geral.

---

## Bounded Context: Auditoria & Conformidade

### REQ-036: Trilha de auditoria para conciliação e divergência

- **EARS Pattern:** Ubiquitous
- **Declaração:** O sistema deverá gravar um registro de auditoria imutável para cada conciliação (ação 'CO') e cada divergência (ação 'DV'), contendo sequencial, data/hora, usuário, tabela e chave de referência, valor anterior, valor novo e descrição.
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L260-L300`
- **Source Rule:** BATCHCON regra #9 (Confirmada)
- **Critérios de Aceite:**
  - [ ] Given uma conciliação bem-sucedida, when registrada, then existe um evento de auditoria 'CO' com os campos obrigatórios preenchidos.
  - [ ] Given uma divergência de valor, when registrada, then existe um evento de auditoria 'DV'.

### REQ-037: Consulta da trilha de auditoria com exclusões visíveis

- **EARS Pattern:** Event-driven
- **Declaração:** Quando consultada a trilha de auditoria por período, ação, usuário ou tabela, o sistema deverá exibir todos os eventos do período, incluindo os de exclusão (ação 'EX').
- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L168-L195`
- **Source Rule:** RELAUDIT regra #2 (Confirmada) — vocabulário IN/AL/CO/CN/DV
- **Nota:** ⚠️ O legado **ocultava** deliberadamente as exclusões ('EX') do relatório — comportamento de encobrimento que **NÃO** é reproduzido. A versão moderna torna toda exclusão visível por compliance (IN-TCU 63). Ver [OQ-03](#open-questions-não-são-requisitos-ainda).
- **Critérios de Aceite:**
  - [ ] Given eventos de auditoria do tipo 'EX' no período, when consultada a trilha, then eles aparecem no resultado.
  - [ ] Given um filtro por ação 'CO', when consultada, then apenas eventos 'CO' são retornados.

---

## Open Questions (Não São Requisitos Ainda)

> Mistérios, backdoors e divergências que **bloqueiam** ou condicionam requisitos acima. Cada um exige decisão de negócio/segurança/jurídico **antes** de virar requisito. Fonte: [mysteries-found.md](../01-arqueologia/mysteries-found.md) e notas do [business-rules-catalog.md](../01-arqueologia/business-rules-catalog.md).

| ID | Questão Aberta | Origem legada | Informação necessária para resolver |
| -- | -------------- | ------------- | ----------------------------------- |
| OQ-01 | **Máquina de status do PAGAMENTO ambígua** — DDM × código discordam; 'E' significa erro (BATCHCON) ou estornado (relatórios)? Status 'C' e tipo 'T' não têm programa que os grave. | MYS-024/MYS-025; `BATCHCON.NSN`, `BATCHREL.NSN`, `RELPGT.NSN` | Decisão de produto sobre o conjunto canônico de estados/transições e fonte única de verdade (contexto Pagamentos). Bloqueia REQ-032. |
| OQ-02 | **Lógica de cálculo duplicada** CALCBENF × BATCHPGT (tabelas copiadas). | MYS-023; `BATCHPGT.NSN#L255-L300` | Confirmar que a regra única (REQ-018) substitui ambas sem mudar valores. Validar igualdade das tabelas legadas. |
| OQ-03 | **Encobrimento de exclusões na auditoria** ('EX' ocultas; "LIMPEZA RELATORIO" 2014). | `RELAUDIT.NSN#L110-L114` | Decisão de compliance/auditoria (TCU). REQ-037 já assume exibir 'EX'; confirmar com jurídico/auditoria. |
| OQ-04 | **Dupla aplicação de reajuste** — Fator-K embute reajuste no cadastro (REQ-017) e o cálculo mensal aplica reajuste de novo (REQ-018/021). | MYS-021; `CADPROG.NSN#L93-L95`, `CALCBENF.NSN#L228` | Definir semântica: Fator-K e reajuste mensal são distintos ou o mesmo aplicado duas vezes? Origem da constante `0.347215`. Bloqueia migração de valores. |
| OQ-05 | **Pensão alimentícia ('P') sujeita ao teto de 30%**, ao contrário do judicial ('J'). | `CALCDSCT.NSN#L139-L148` | Validação jurídica: pensão deveria ser isenta como judicial? Condiciona REQ-026. |
| OQ-06 | **Arredondamento divergente** — BATCHREL arredonda, cálculos truncam; totais do relatório não batem. | `BATCHREL.NSN#L143-L147` | Política contábil única (REQ-021 define truncar) — confirmar com contabilidade. |
| OQ-07 | **13º descarta fator familiar e de renda** (usa só regional × idade). | `CALCBENF.NSN#L240-L246` | Confirmar se é intencional ou erro histórico. Condiciona REQ-022. |
| OQ-08 | **Suspensão automática acima de 75 anos** — idoso vira 'S' silenciosamente. | `CADBENEF.NSN#L165-L167` | Validar intenção de negócio (revisão obrigatória? bug?). Condiciona REQ-008. |
| OQ-09 | **Máscara de CPF inconsistente + bug LGPD** (CPF curto expõe primeiros dígitos; "não corrigir sem aprovação da auditoria"). | `CONSBENF.NSN#L198-L210`, `RELPGT.NSN#L125-L130` | Aprovação da auditoria para corrigir e unificar a máscara. Condiciona REQ-015. |
| OQ-10 | **CNAB 240 hardcoded só para o Banco do Brasil**; outros bancos têm layout diferente. | `BATCHCON.NSN#L120-L145` | Há outros bancos pagadores ativos? Generalizar a conciliação. Condiciona REQ-032. |
| OQ-11 | **Tabela IPCA congelada em 2010-2012**; anos fora da tabela passam sem correção. | `CALCCORR.NSN#L70-L120` | Fonte oficial e atualizada dos índices IPCA. Condiciona REQ-034. |
| OQ-S1 | **4 backdoors de segurança** — região 99 (elegível incondicional), CPF iniciado em '000' (VALBENEF), 8 prefixos de CPF em VALDOCS, ocultação de exclusões. | `VALELEG.NSN#L108-L112`, `VALBENEF.NSN#L196-L201`, `VALDOCS.NSN#L180-L195`, `RELAUDIT.NSN#L110-L114` | **Decisão obrigatória de produto + segurança + jurídico** (preservar/restringir/auditar) ANTES de qualquer EARS de elegibilidade/validação. Vetores de fraude/LGPD/compliance TCU. **Não reimplementar sem decisão explícita.** |

> ⚠️ Os backdoors (OQ-S1) **não foram convertidos em requisitos**. A regra "região 99 = elegível incondicional" (VALELEG #4, confirmada no código) é deliberadamente **omitida** de REQ-012/013/014 até que haja decisão de segurança.

---

## Matriz de Rastreabilidade

| REQ-ID | EARS Pattern | source_legacy | Source Rule # | Source File | Bounded Context |
| ------ | ------------ | ------------- | ------------- | ----------- | --------------- |
| REQ-001 | Unwanted | CADBENEF.NSN#L103-L115 | #2, #3 | CADBENEF.NSN | Beneficiários |
| REQ-002 | Unwanted | VALBENEF.NSN#L113-L165 | #1, #3, #4, #5 | VALBENEF.NSN | Beneficiários |
| REQ-003 | Unwanted | VALBENEF.NSN#L188-L205 | #2 | VALBENEF.NSN | Beneficiários |
| REQ-004 | Unwanted | CADBENEF.NSN#L117-L127 | #4, #5 | CADBENEF.NSN | Beneficiários |
| REQ-005 | Unwanted | CADBENEF.NSN#L141-L145 | #7 | CADBENEF.NSN | Beneficiários |
| REQ-006 | Ubiquitous | CADBENEF.NSN#L160-L162 | #9 | CADBENEF.NSN | Beneficiários |
| REQ-007 | Unwanted | VALBENEF.NSN#L168-L175 | #6 | VALBENEF.NSN | Beneficiários |
| REQ-008 | Event-driven | CADBENEF.NSN#L165-L167 | #10 | CADBENEF.NSN | Beneficiários |
| REQ-009 | State-driven | CADDEPEND.NSN#L61-L64 | #3 | CADDEPEND.NSN | Beneficiários |
| REQ-010 | Event-driven | CADDEPEND.NSN#L54-L57 | #2 | CADDEPEND.NSN | Beneficiários |
| REQ-011 | Unwanted | CADDEPEND.NSN#L83-L88 | #5 | CADDEPEND.NSN | Beneficiários |
| REQ-012 | Event-driven | VALELEG.NSN#L117-L131 | #5, #6, #7 | VALELEG.NSN | Beneficiários |
| REQ-013 | Event-driven | VALELEG.NSN#L138-L162 | #8, #9, #10 | VALELEG.NSN | Beneficiários |
| REQ-014 | Event-driven | VALELEG.NSN#L167-L193 | #11, #12, #13 | VALELEG.NSN | Beneficiários |
| REQ-015 | Ubiquitous | CONSBENF.NSN#L150-L153 | #2 / RELPGT #3 | CONSBENF.NSN | Beneficiários |
| REQ-016 | Unwanted | CADPROG.NSN#L82-L90 | #2 (promovida) | CADPROG.NSN | Programas |
| REQ-017 | Ubiquitous | CADPROG.NSN#L93-L95 | #3 | CADPROG.NSN | Programas |
| REQ-018 | Ubiquitous | CALCBENF.NSN#L224-L228 | #9, #10 | CALCBENF.NSN | Pagamentos |
| REQ-019 | Event-driven | CALCBENF.NSN#L180-L184 | #5 | CALCBENF.NSN | Pagamentos |
| REQ-020 | Ubiquitous | CALCBENF.NSN#L120-L129 | #8 | CALCBENF.NSN | Pagamentos |
| REQ-021 | Ubiquitous | CALCBENF.NSN#L231-L232 | #11 / CALCDSCT #13 | CALCBENF.NSN | Pagamentos |
| REQ-022 | Event-driven | CALCBENF.NSN#L240-L246 | #12 | CALCBENF.NSN | Pagamentos |
| REQ-023 | Event-driven | CALCBENF.NSN#L249-L257 | #13 | CALCBENF.NSN | Pagamentos |
| REQ-024 | Unwanted | CALCBENF.NSN#L160-L163 | #3 / BATCHPGT #4 | CALCBENF.NSN | Pagamentos |
| REQ-025 | Ubiquitous | CALCDSCT.NSN#L57-L66 | #3 | CALCDSCT.NSN | Pagamentos |
| REQ-026 | Event-driven | CALCDSCT.NSN#L138-L195 | #4, #7, #12 | CALCDSCT.NSN | Pagamentos |
| REQ-027 | Event-driven | CALCDSCT.NSN#L128-L138 | #6 | CALCDSCT.NSN | Pagamentos |
| REQ-028 | Unwanted | CALCBENF.NSN#L265-L267 | #14 (promovida) | CALCBENF.NSN | Pagamentos |
| REQ-029 | Ubiquitous | BATCHPGT.NSN#L180-L375 | #12, #13 | BATCHPGT.NSN | Pagamentos |
| REQ-030 | Unwanted | BATCHPGT.NSN#L219-L228 | #5 | BATCHPGT.NSN | Pagamentos |
| REQ-031 | Unwanted | BATCHPGT.NSN#L237-L245 | #6 | BATCHPGT.NSN | Pagamentos |
| REQ-032 | Event-driven | BATCHCON.NSN#L130-L218 | #1, #3, #5, #6, #7 | BATCHCON.NSN | Pagamentos |
| REQ-033 | Event-driven | BATCHCON.NSN#L175-L188 | #4 | BATCHCON.NSN | Pagamentos |
| REQ-034 | State-driven | CALCCORR.NSN#L170-L172 | #3 | CALCCORR.NSN | Pagamentos |
| REQ-035 | Event-driven | BATCHREL.NSN#L120-L178 | #1, #2 | BATCHREL.NSN | Pagamentos |
| REQ-036 | Ubiquitous | BATCHCON.NSN#L260-L300 | #9 | BATCHCON.NSN | Auditoria |
| REQ-037 | Event-driven | RELAUDIT.NSN#L168-L195 | #2 | RELAUDIT.NSN | Auditoria |

---

## Definição de Pronto

- [x] Pelo menos 10 requisitos EARS com REQ-IDs únicos (37 requisitos)
- [x] Todo requisito tem `source_legacy:` apontando para `.NSN`
- [x] Todo requisito cita sua regra-fonte e arquivo legado
- [x] Todo requisito tem ao menos 2 critérios de aceitação
- [x] Requisitos agrupados pelos 4 bounded contexts
- [x] Mistérios/backdoors aparecem só em Open Questions, nunca como requisitos
- [x] Matriz de rastreabilidade conecta todo REQ à sua regra-fonte
- [ ] 🟡 **Validação da equipe** — confirmar regras promovidas (REQ-016, REQ-028) e priorização das Open Questions

---

## Próximos Passos

1. **Resolver Open Questions bloqueantes** (OQ-01, OQ-04, OQ-S1) com gestores (SENARC/CGPB), segurança e jurídico antes de fechar a spec de cálculo/status/elegibilidade.
2. **`/speckit.clarify`** para refinar requisitos ambíguos.
3. **`/generate-adr`** para máquina de status única (OQ-01), mapeamento Adabas→JPA e desacoplamento de auditoria por eventos.
4. **`/design-modular-monolith`** com os 4 módulos espelhando os bounded contexts.

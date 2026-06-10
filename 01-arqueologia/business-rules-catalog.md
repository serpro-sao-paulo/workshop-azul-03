<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Catálogo de Regras de Negócio — SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **business-rules-catalog**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Registre aqui todas as regras de negócio extraídas do código Natural/Adabas.
> Cada regra precisa ter rastreabilidade até o código-fonte.
>
> **REGRA DURA:** linhas com `Programa Fonte` vazio são **inválidas** e não contam para o gate do Estágio 2. Use o formato `01-arqueologia/legado-sifap/natural-programs/ARQUIVO.NSN#L<inicio>-L<fim>` sempre que possível. Mínimo aceito: nome do arquivo .NSN.

## Como pensar em "regra de negócio"

O que conta:

- Um `IF` que decide algo no domínio (ex.: _"se a UF é do Nordeste e o programa é Seca, valor base × 1.2"_)
- Uma constante numérica sem explicação (ex.: `0.075` num cálculo de imposto)
- Uma transição de status com regra (ex.: _"só de A para S, nunca de I para A"_)
- Um tratamento especial para um caso (ex.: _"se o CPF começa com 999, é teste"_)

O que NÃO conta: paginação de relatório, formatação de saída, manipulação de cursor Adabas, abertura de arquivo. Ignore esses detalhes de implementação.

## Níveis de Risco

| Nível       | Descrição                                                     |
| ----------- | ------------------------------------------------------------- |
| **CRÍTICO** | Regra financeira ou de segurança — erro causa prejuízo direto |
| **ALTO**    | Regra de negócio central — afeta fluxo principal              |
| **MÉDIO**   | Regra de validação ou formatação — afeta qualidade dos dados  |
| **BAIXO**   | Regra de apresentação ou conveniência — impacto limitado      |

## Regras Encontradas

| ID     | Regra de Negócio | Programa Fonte | Campos DDM | Nível de Risco | Notas |
| ------ | ---------------- | -------------- | ---------- | -------------- | ----- |
| BR-001 |                  |                |            |                |       |
| BR-002 |                  |                |            |                |       |
| BR-003 |                  |                |            |                |       |
| BR-004 |                  |                |            |                |       |
| BR-005 |                  |                |            |                |       |
| BR-006 |                  |                |            |                |       |
| BR-007 |                  |                |            |                |       |
| BR-008 |                  |                |            |                |       |
| BR-009 |                  |                |            |                |       |
| BR-010 |                  |                |            |                |       |
| BR-011 |                  |                |            |                |       |
| BR-012 |                  |                |            |                |       |
| BR-013 |                  |                |            |                |       |
| BR-014 |                  |                |            |                |       |
| BR-015 |                  |                |            |                |       |

> Adicione mais linhas conforme necessário. Lembre-se: existem **10 regras escondidas** no código!

---

## Regras de CALCBENF.NSN

> Extraído por `/extract-business-rules` em **2026-06-10** lendo `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN` (325 linhas), bloco a bloco.
> Cross-reference com `legado-sifap/legacy-docs/REGRAS-NEGOCIO-2012.md` e `MANUAL-TECNICO-SIFAP-2008.md`.
> **Números de linha são aproximados** (±2) — o arquivo-fonte não tem numeração exibida. Confirme ao abrir.

**Cabeçalho do programa:** Autor Carlos Roberto da Silva, 18/04/1997. Alterações registradas: 2001 (inc. 13º), 2004 (ajuste fator regional), 2009 (abono natalino), 2013 (novas faixas de renda). Lê arq. 150 (BENEFICIARIO), 155 (PROGRAMA-SOCIAL); grava arq. 160 (PAGAMENTO).

| #   | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
| --- | ------------------- | -------------- | ----- | ------------- | ----- |
| 1 | Se o mês da competência (AAAAMM) for menor que 1 ou maior que 12, o sistema deve rejeitar o cálculo com "COMPETENCIA INVALIDA". | Unwanted | `CALCBENF.NSN#L141-L144` | Inferida | Validação de entrada. Doc cita formato AAAAMM mas não a faixa de mês. |
| 2 | Se o beneficiário (por CPF) não for encontrado no arquivo 150, o sistema deve rejeitar o cálculo. | Unwanted | `CALCBENF.NSN#L155-L158` | Inferida | `BENEFICIARIO.CPF`. |
| 3 | Quando o status do beneficiário for diferente de 'A' (ativo), o sistema deve rejeitar o cálculo. | Unwanted | `CALCBENF.NSN#L160-L163` | **Confirmada** | `BENEFICIARIO.STATUS`. Confirma RN do MANUAL-TECNICO §3.5.1 / REGRAS-NEGOCIO §5.1 ("apenas BN-CD-SIT='A' processados"). |
| 4 | Se o programa social vinculado não for encontrado no arquivo 155, o sistema deve rejeitar o cálculo. | Unwanted | `CALCBENF.NSN#L174-L177` | Inferida | `BENEFICIARIO.COD-PROGRAMA` → `PROGRAMA-SOCIAL.COD-PROGRAMA`. |
| 5 | Quando a região do beneficiário estiver entre 1 e 25, o sistema deve aplicar o fator regional da tabela; caso contrário, deve aplicar fator neutro 1.0000. | Event-driven | `CALCBENF.NSN#L180-L184` | **Confirmada** | `BENEFICIARIO.COD-REGIAO`. Tabela `#TAB-REG` hardcoded (27 posições, L91-L117). **Região 99 cai no ELSE → fator 1.0** (ver MYS-004). Confirma RN-005 (regiões 01-27). |
| 6 | O sistema deve calcular o fator familiar por faixas de dependentes: 0 dep → 1.0000; 1-2 dep → 1.0000 + (dep × 0.0500); 3-4 dep → 1.1000 + ((dep-2) × 0.0300); 5+ dep → 1.1600 + ((dep-4) × 0.0200). | Ubiquitous | `CALCBENF.NSN#L187-L199` | Inferida | `BENEFICIARIO.NUM-DEPENDENTES`. **A fórmula trata 5+ dependentes**, contradizendo o "máx. 3 dependentes" da RN-004 → ver MYS-007 / nova inconsistência abaixo. |
| 7 | O sistema deve calcular o fator idade: ≥65 anos → 1.1500; 60-64 → 1.1000; <18 → 1.0500; demais → 1.0000. | Ubiquitous | `CALCBENF.NSN#L207-L219` | Inferida | Idade derivada de `BENEFICIARIO.DT-NASCIMENTO` vs ano da competência. Sem suporte documental explícito. |
| 8 | O sistema deve atribuir o fator de renda pela primeira faixa cujo teto seja ≥ à renda familiar: ≤300→1.0000; ≤600→0.8500; ≤1000→0.7000; ≤1500→0.5500; ≤9999.99→0.4000. | Ubiquitous | `CALCBENF.NSN#L120-L129` (tabela) · `#L~298-L305` (subrotina DET-FAIXA-RENDA) | **Confirmada** | `BENEFICIARIO.RENDA-FAMILIAR`. Confirma RN-017 (faixas parametrizadas) e RN-018 (atribuição por renda). Magic number `9999.99` = teto do campo (ver mistério). |
| 9 | O sistema deve calcular o benefício mensal como: VLR-BASE × fator_regional × fator_familiar × fator_renda × fator_idade. | Ubiquitous | `CALCBENF.NSN#L224-L225` | **Confirmada** | `PROGRAMA-SOCIAL.VLR-BASE` → `PAGAMENTO.VLR-BRUTO`. Confirma RN-013 (fórmula básica). **Nenhuma variável literal "FATOR-K" existe** → ver MYS-001 atualizado. |
| 10 | O sistema deve aplicar o reajuste do programa: VLR = VLR × (1 + FATOR-REAJUSTE). | Ubiquitous | `CALCBENF.NSN#L228` | **Confirmada** | `PROGRAMA-SOCIAL.FATOR-REAJUSTE`. Confirma RN-019 (reajuste). |
| 11 | O sistema deve truncar (não arredondar) todos os valores para 2 casas decimais via multiplicação/divisão inteira por 100. | Ubiquitous | `CALCBENF.NSN#L231-L232` (e repetido em 13º, abono, líquido) | **Confirmada** | Confirma RN-014 (truncamento, não arredondamento). `#VLR-TEMP` é N11 → possível overflow (ver mistério). |
| 12 | Quando o mês da competência for 12 (dezembro), o sistema deve marcar o pagamento como tipo 'D', calcular o 13º como VLR-BASE × fator_regional × fator_idade e somá-lo ao bruto. | Event-driven | `CALCBENF.NSN#L240-L246` | **Confirmada** | `PAGAMENTO.TIPO-PGTO='D'`, `PAGAMENTO.VLR-ABONO`. **Resolve MYS-002.** Doc citava 13º/dezembro como não documentado. Fórmula do 13º **omite** fator_familiar e fator_renda — ver mistério. |
| 13 | Quando for dezembro e o programa for do tipo 'A', o sistema deve adicionar abono natalino de 15% sobre o benefício mensal; caso contrário, abono = 0. | Event-driven | `CALCBENF.NSN#L249-L257` | **Confirmada** | `PROGRAMA-SOCIAL.TIPO='A'`. Parte de MYS-002 (abono natalino). Magic number `0.15`. |
| 14 | Se o valor líquido resultar negativo, o sistema deve zerá-lo (piso de zero). | Unwanted | `CALCBENF.NSN#L265-L267` | Inferida | `PAGAMENTO.VLR-LIQUIDO`. Proteção contra líquido negativo. |
| 15 | Quando o valor bruto for maior que 500.00, o sistema deve aplicar desconto básico de 3% (contribuição social). | Event-driven | `CALCBENF.NSN#L~309-L322` (subrotina CALC-DESCONTOS) | Inferida | Comentário no código: *"SIMPLIFICADO (VER CALCDSCT P/ COMPLETO)"* — a lógica completa de descontos (incl. exceção judicial, MYS-003) está em `CALCDSCT.NSN`. Magic number `0.03`, limiar `500.00`. |

### Mistérios encontrados em CALCBENF.NSN

<!-- mystery: MYS-001 (Fator-K) — NÃO existe nenhuma variável literal chamada "FATOR-K" no código. O cálculo usa 5 fatores nomeados: #FATOR-REG, #FATOR-FAM, #FATOR-RND, #FATOR-IDADE e #FATOR-REAJ (CALCBENF.NSN#L224-L228). O "Fator K" lendário citado em REGRAS-NEGOCIO-2012.md §2.1 pode ser apelido informal de um destes (ou do produto deles), mas NÃO é confirmável pelo código. Permanece mistério — não atribuir a um fator específico sem evidência. -->

<!-- mystery: 13º contradiz o próprio comentário — o cabeçalho do bloco (CALCBENF.NSN#L237-L239) afirma "VLR_13 = VLR_BASE * FATOR_REG * (MESES_ATIVOS/12)", mas o código real (L242) computa "VLR-BASE * FATOR-REG * FATOR-IDADE". A variável MESES-ATIVOS nem sequer existe/é calculada. O 13º NÃO é proporcional aos meses ativos como o comentário sugere. Investigar se isso é bug histórico ou mudança de regra. Relaciona-se a MYS-006 (pró-rata). -->

<!-- mystery: MYS-006 (pró-rata) — não há cálculo proporcional para benefícios iniciados no meio do mês em CALCBENF. A doc (MANUAL-TECNICO §3.3.1) afirma que existe; o código não o implementa. Verificar se o pró-rata vive em outro programa ou foi removido. -->

<!-- mystery: 13º descarta fatores — a fórmula do 13º (L242) usa apenas FATOR-REG e FATOR-IDADE, omitindo FATOR-FAM (dependentes) e FATOR-RND (renda) que entram no benefício mensal. Intencional ou erro? Impacto financeiro direto no valor do 13º. -->

<!-- mystery: Abono só para tipo 'A' — o abono natalino de 15% (L249-L257) só se aplica a programas TIPO='A'. Quais são os outros tipos de PROGRAMA-SOCIAL.TIPO e por que não recebem abono? Cruzar com CADPROG.NSN. -->

<!-- mystery: MYS-007 (máx. dependentes) — a fórmula do fator familiar (L187-L199) calcula explicitamente para 5+ dependentes, contradizendo a RN-004 "máximo 3 dependentes". Ou o limite de 3 não é imposto neste programa, ou a RN-004 está desatualizada. Conferir CADDEPEND.NSN e a capacidade do DDM BENEFICIARIO. -->

<!-- mystery: Overflow no truncamento — #VLR-TEMP é N11 (11 dígitos); o truncamento faz "#VLR-TEMP = #VLR-BENF * 100" (L231). Para valores > ~99.999.999,99 isso estoura. Provavelmente seguro no domínio atual, mas é um magic-pattern frágil. Baixa prioridade. -->

### Atualização de MYS-004 (bypass região 99)

Em CALCBENF, a região 99 **não recebe fator regional** — cai no ramo ELSE e usa 1.0000 (`CALCBENF.NSN#L180-L184`). Isso esclarece o comportamento de *cálculo* para a região 99, mas **não explica a finalidade do "bypass"** mencionado em RN-005, que provavelmente atua na *elegibilidade* (`VALELEG.NSN`), não no cálculo. MYS-004 permanece aberto — investigar `VALELEG.NSN` e `CADBENEF.NSN`.

---

## Regras de CALCDSCT.NSN

> Extraído por `/extract-business-rules` em **2026-06-10** lendo `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN` (~230 linhas), bloco a bloco.
> Cross-reference com `legado-sifap/legacy-docs/REGRAS-NEGOCIO-2012.md` §3 (Descontos). **Números de linha aproximados** (±2).

**Cabeçalho do programa:** Autor Roberto Mendes Junior, 25/08/1999. Alterações: 2007 (inc. desconto judicial), 2015 (novas alíquotas). Lê arq. 160 (PAGAMENTO) e 150 (BENEFICIARIO); atualiza arq. 160. Os descontos ficam num grupo periódico (PE) `BENEFICIARIO.DESCONTOS` com tipos: C=contrib, I=imposto, J=judicial, S=sindical, P=pensão, A=admin.

| #   | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
| --- | ------------------- | -------------- | ----- | ------------- | ----- |
| 1 | Se o pagamento (por NUM-PAGTO) não existir ou o CPF não bater, o sistema deve rejeitar o cálculo de descontos. | Unwanted | `CALCDSCT.NSN#L74-L88` | Inferida | `PAGAMENTO.NUM-PAGTO`, `PAGAMENTO.CPF-BENEF`. |
| 2 | Se o beneficiário não existir, o sistema deve rejeitar o cálculo de descontos. | Unwanted | `CALCDSCT.NSN#L91-L97` | Inferida | `BENEFICIARIO.CPF`. Usa `*NUMBER(...) = 0`. |
| 3 | O sistema deve aplicar contribuição social obrigatória por faixa de bruto: ≤500→3%; ≤1000→5%; ≤2000→7%; ≤9999.99→9%. | Ubiquitous | `CALCDSCT.NSN#L57-L66` (tabela) · `#L~205-L216` (subrotina CALC-CONTRIB-SOCIAL) | **Confirmada** | Confirma RN-021/§3 (contribuição social). **Substitui/expande** o desconto "simplificado de 3%" visto em CALCBENF (BR-15). Alíquotas alteradas em 2015. |
| 4 | O sistema deve calcular o teto máximo de descontos como 30% do valor bruto. | Ubiquitous | `CALCDSCT.NSN#L104-L108` | **Confirmada** | `PAGAMENTO.VLR-BRUTO`. Confirma a regra de teto 30% (RN §3). Magic number `0.30`. |
| 5 | O sistema deve ignorar descontos fora de vigência (DT-FIM < hoje, ou DT-INICIO > hoje). | State-driven | `CALCDSCT.NSN#L116-L123` | Inferida | `BENEFICIARIO.DESCONTOS.DT-INICIO-DSCT`, `.DT-FIM-DSCT`. Controle de vigência por desconto. |
| 6 | Para desconto judicial (tipo 'J'): se houver valor fixo, usá-lo; senão aplicar o percentual sobre o bruto. | Event-driven | `CALCDSCT.NSN#L128-L138` | **Confirmada** | `DESCONTOS.TIPO-DSCT='J'`, `.VLR-DSCT`, `.PCT-DSCT`. |
| 7 | **O desconto judicial (tipo 'J') NÃO está sujeito ao teto de 30%.** | Event-driven | `CALCDSCT.NSN#L138` (sem corte) · `#L~190-L195` (IF #TIPO-DSCT NE 'J') | **Confirmada** | **Resolve MYS-003.** A exceção judicial ao teto, citada na doc mas "não confirmada no código", está aqui: o corte ao teto só roda quando `#TIPO-DSCT NE 'J'`. `DESCONTOS.NUM-PROCESSO` guarda o processo. |
| 8 | Para pensão alimentícia (tipo 'P'): valor fixo, se houver; senão percentual sobre o bruto. | Event-driven | `CALCDSCT.NSN#L139-L148` | Inferida | `TIPO-DSCT='P'`. **Sujeita ao teto de 30%** (não é 'J') — ver mistério. |
| 9 | Para imposto retido (tipo 'I'): aplicar percentual sobre o bruto. | Event-driven | `CALCDSCT.NSN#L149-L153` | Inferida | `TIPO-DSCT='I'`. |
| 10 | Para desconto sindical (tipo 'S'): aplicar 1% fixo sobre o bruto. | Event-driven | `CALCDSCT.NSN#L154-L157` | Inferida | `TIPO-DSCT='S'`. Magic number `0.01` hardcoded (não vem do cadastro). |
| 11 | Para desconto administrativo (tipo 'A'): valor fixo, se houver; senão percentual sobre o bruto. | Event-driven | `CALCDSCT.NSN#L158-L167` | Inferida | `TIPO-DSCT='A'`. |
| 12 | Quando o total de descontos não-judiciais exceder o teto de 30%, o sistema deve limitá-lo ao teto. | Event-driven | `CALCDSCT.NSN#L190-L195` | **Confirmada** | Aplica o teto a cada iteração do loop, exceto tipo 'J'. Ver mistério de ordem. |
| 13 | O sistema deve truncar (não arredondar) o total de descontos para 2 casas e gravá-lo no pagamento (VLR-DESCONTO). | Ubiquitous | `CALCDSCT.NSN#L198-L207` | **Confirmada** | `PAGAMENTO.VLR-DESCONTO`. Mesmo padrão de truncamento de CALCBENF (RN-014). |

### Mistérios encontrados em CALCDSCT.NSN

<!-- mystery: Teto aplicado dentro do loop, dependente de ordem — o corte ao teto de 30% (CALCDSCT.NSN#L190-L195) roda a CADA iteração sobre o total acumulado, não uma vez no final. Como descontos 'J' (judiciais) são somados ao mesmo #VLR-TOTAL-DSCT mas isentos de corte, a ORDEM dos descontos no grupo PE muda o resultado final: um 'J' processado antes infla o total e pode empurrar não-judiciais contra o teto mais cedo. Comportamento sensível à ordem dos registros — risco de migração. Investigar se a ordem do PE é determinística. -->

<!-- mystery: Judicial pode estourar o total acima de 30% — como 'J' é somado sem teto e o corte só limita quando #TIPO-DSCT NE 'J', o #VLR-TOTAL-DSCT pode terminar acima de 30% do bruto se houver desconto judicial. O líquido em CALCBENF tem piso zero (BR-14), mas o desconto pode consumir todo o benefício. Confirmar se é intencional (ordem judicial prevalece) — provável, mas sem doc. -->

<!-- mystery: Pensão alimentícia ('P') sujeita ao teto — diferente do judicial, a pensão tipo 'P' NÃO é isenta do teto de 30% (só 'J' é). Juridicamente pensão alimentícia costuma ter prioridade semelhante à judicial. Verificar com facilitador/jurídico se 'P' deveria também ser isenta. Possível regra incorreta ou intencional. -->

<!-- mystery: Contribuição social conta para o teto? — CALC-CONTRIB-SOCIAL soma ao #VLR-TOTAL-DSCT ANTES do cálculo do teto e do loop (L101-L108). Não é tipo 'J', então é coberta pelo corte do teto. Mas é "obrigatória" — se o teto cortar, a contribuição obrigatória pode ser parcialmente perdida. Esclarecer precedência entre contribuição obrigatória e teto. -->

<!-- mystery: Sindical 1% hardcoded — o desconto sindical (tipo 'S', L154-L157) usa 0.01 fixo no código, ignorando PCT-DSCT do cadastro, ao contrário dos outros tipos. Por que sindical é a exceção? Magic number sem doc. -->

---

## Regras de VALELEG.NSN

> Extraído por `/extract-business-rules` em **2026-06-10** lendo `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN` (~265 linhas), bloco a bloco.
> Cross-reference com `legado-sifap/legacy-docs/REGRAS-NEGOCIO-2012.md` §4 (Elegibilidade) e RN-005. **Números de linha aproximados** (±2).

**Cabeçalho do programa:** Autor Jose Ferreira dos Santos, 03/02/1999. Alterações: 2004 (novas regras eleg.), 2009 (ajuste faixa etária), **2013 (inc. região 99)**. Lê arq. 150 (BENEFICIARIO) e 155 (PROGRAMA-SOCIAL). Acumula motivos de inelegibilidade num array `#MOTIVO(10)`.

| #   | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
| --- | ------------------- | -------------- | ----- | ------------- | ----- |
| 1 | Se o beneficiário (por CPF) não existir, o sistema deve rejeitar a validação. | Unwanted | `VALELEG.NSN#L77-L80` | Inferida | `BENEFICIARIO.CPF`. |
| 2 | Se o programa não existir, o sistema deve rejeitar a validação. | Unwanted | `VALELEG.NSN#L95-L98` | Inferida | `PROGRAMA-SOCIAL.COD-PROGRAMA`. |
| 3 | Se o programa estiver inativo (STATUS-PROG ≠ 'A'), o sistema deve rejeitar a validação. | Unwanted | `VALELEG.NSN#L100-L103` | Inferida | `PROGRAMA-SOCIAL.STATUS-PROG`. |
| 4 | **Quando a região do beneficiário for 99 (internacional/diplomático), o sistema deve declará-lo ELEGÍVEL imediatamente, pulando TODAS as demais verificações.** | Event-driven | `VALELEG.NSN#L108-L112` | **Confirmada (código)** | **RESOLVE MYS-004.** `BENEFICIARIO.COD-REGIAO=99` faz `ESCAPE ROUTINE` com elegível=TRUE. Comentário no código: "REGIAO 99 - INTERNACIONAL/DIPLOMATICO". Incluído em 2013 por Anderson Lima. Bypass total de status, idade, renda, documentos e tipo. ⚠️ **Risco de fraude/segurança** — ver mistério. |
| 5 | Quando o status do beneficiário for 'S', o sistema deve marcá-lo inelegível com motivo "suspenso". | Event-driven | `VALELEG.NSN#L117-L121` | **Confirmada** | `BENEFICIARIO.STATUS`. Confirma transições de status (RN cadastro). |
| 6 | Quando o status for 'C' ou 'D', o sistema deve marcá-lo inelegível ("cancelado/desligado"). | Event-driven | `VALELEG.NSN#L122-L126` | **Confirmada** | `BENEFICIARIO.STATUS`. |
| 7 | Quando o status for 'I', o sistema deve marcá-lo inelegível ("inativo"). | Event-driven | `VALELEG.NSN#L127-L131` | **Confirmada** | `BENEFICIARIO.STATUS`. Status conhecidos: A, S, C, D, I. |
| 8 | Quando o programa define idade mínima (>0) e a idade do beneficiário for menor, o sistema deve marcá-lo inelegível. | Event-driven | `VALELEG.NSN#L138-L144` | **Confirmada** | `PROGRAMA-SOCIAL.IDADE-MIN`. Confirma RN §4 (elegibilidade por idade). |
| 9 | Quando o programa define idade máxima (>0) e a idade for maior, o sistema deve marcá-lo inelegível. | Event-driven | `VALELEG.NSN#L145-L151` | **Confirmada** | `PROGRAMA-SOCIAL.IDADE-MAX`. |
| 10 | Quando o programa define renda máxima (>0) e a renda familiar for maior, o sistema deve marcá-lo inelegível. | Event-driven | `VALELEG.NSN#L156-L162` | **Confirmada** | `PROGRAMA-SOCIAL.RENDA-MAX`, `BENEFICIARIO.RENDA-FAMILIAR`. |
| 11 | Para programa tipo 'A' (assistencial): se renda > 600 e sem dependentes, inelegível; e se documentação ≠ 'S', inelegível. | Event-driven | `VALELEG.NSN#L167-L181` | **Confirmada** | `PROGRAMA-SOCIAL.TIPO='A'`, `BENEFICIARIO.DOCUMENTOS-OK`. Magic number `600.00`. Lógica de dependentes peculiar — ver mistério. |
| 12 | Para programa tipo 'P' (previdenciário): se idade < 60, inelegível. | Event-driven | `VALELEG.NSN#L182-L187` | **Confirmada** | `PROGRAMA-SOCIAL.TIPO='P'`. Magic number `60`. |
| 13 | Para programa tipo 'T' (trabalho): se idade < 16 ou > 65, inelegível. | Event-driven | `VALELEG.NSN#L188-L193` | **Confirmada** | `PROGRAMA-SOCIAL.TIPO='T'`. Faixa 16-65. |
| 14 | Se o tipo de programa for desconhecido (≠ A/P/T), o sistema deve marcá-lo inelegível ("tipo desconhecido"). | Unwanted | `VALELEG.NSN#L194-L197` | Inferida | Ramo NONE do DECIDE. Tipos válidos: A, P, T. |
| 15 | Quando o código de elegibilidade específica não for vazio, o sistema deve aplicar verificações por posição: posição 1 = 'R' exige NIS cadastrado; posição 2 = 'D' exige dependentes. | Optional | `VALELEG.NSN#L202-L205` · `#L~243-L261` (VERIF-ELEG-ESPECIFICA) | Inferida | `PROGRAMA-SOCIAL.COD-ELEGIBILIDADE` (A5), `BENEFICIARIO.NIS`. Código posicional críptico — ver mistério. |

### Mistérios encontrados em VALELEG.NSN

<!-- mystery: MYS-004 RESOLVIDO mas com alerta de segurança — região 99 (VALELEG.NSN#L108-L112) concede elegibilidade TOTAL sem qualquer verificação (status, idade, renda, documentos, tipo). Rotulada "INTERNACIONAL/DIPLOMATICO" no código, mas a doc de 2012 (RN-005) a chamava de "bypass do Roberto" sem finalidade conhecida. Incluída em 2013 (Anderson Lima), DEPOIS do levantamento de 2012 — por isso a doc não a explicava. ⚠️ Como qualquer beneficiário com COD-REGIAO=99 é aprovado automaticamente, isto é um vetor de fraude potente. A migração DEVE decidir: preservar, restringir ou auditar. Validar com facilitador/jurídico/segurança. -->

<!-- mystery: Lógica de dependentes invertida no tipo 'A' — a regra (L167-L181) só marca inelegível por renda>600 SE também não houver dependentes (#NUM-DEP < 1). Ou seja, renda alta COM dependentes passa. Intencional (dependentes justificam renda maior) ou bug de lógica aninhada? Confirmar com gestor do programa assistencial. -->

<!-- mystery: COD-ELEGIBILIDADE posicional e parcialmente usado — o campo A5 tem 5 posições mas só as posições 1 ('R'=NIS) e 2 ('D'=dependentes) são verificadas (VERIF-ELEG-ESPECIFICA). O que significam as posições 3, 4 e 5? Código morto ou regras não implementadas? Investigar valores reais de PROGRAMA-SOCIAL.COD-ELEGIBILIDADE no DDM/dados. -->

<!-- mystery: Idade calculada só por ano — #IDADE = ano_atual - ano_nascimento (VALELEG.NSN#L74-L75), ignorando mês/dia. Beneficiário faz aniversário "em 01/jan" para fins de elegibilidade. Mesma simplificação de CALCBENF. Pode causar elegibilidade prematura/tardia em casos de borda nas faixas etárias dos programas P (60) e T (16/65). -->

---

## Regras de CADBENEF.NSN

> Extraído por `/extract-business-rules` em **2026-06-10** lendo `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN` (~310 linhas), bloco a bloco.
> Cross-reference com `legado-sifap/legacy-docs/REGRAS-NEGOCIO-2012.md` §1 (Cadastro). **Números de linha aproximados** (±2).

**Cabeçalho do programa:** Autor Carlos Roberto da Silva, 15/03/1997. Alterações: 2005 (inc. validação CPF), **2011 (ajuste status idoso)**. Inclusão (I) e alteração (A) no arq. 150 (BENEFICIARIO).

| #   | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
| --- | ------------------- | -------------- | ----- | ------------- | ----- |
| 1 | Se a operação não for 'I' (inclusão) nem 'A' (alteração), o sistema deve rejeitar com "OPERACAO INVALIDA". | Unwanted | `CADBENEF.NSN#L96-L101` | Inferida | `#OPER`. |
| 2 | O CPF é obrigatório (≠ 0). | Unwanted | `CADBENEF.NSN#L103-L107` | **Confirmada** | `BENEFICIARIO.CPF`. Confirma RN-001 (CPF obrigatório). |
| 3 | O sistema deve validar o CPF pelo algoritmo Módulo 11 (dois dígitos verificadores); CPF com DV incorreto é rejeitado. | Unwanted | `CADBENEF.NSN#L110-L115` · `#L~240-L300` (subrotina VALIDA-CPF) | **Confirmada** | Confirma RN-001 (CPF válido). Algoritmo MOD-11 padrão. Incluído em 2005. |
| 4 | Nome é obrigatório (≠ branco). | Unwanted | `CADBENEF.NSN#L117-L121` | **Confirmada** | `BENEFICIARIO.NOME`. |
| 5 | Data de nascimento é obrigatória (≠ 0). | Unwanted | `CADBENEF.NSN#L123-L127` | **Confirmada** | `BENEFICIARIO.DT-NASCIMENTO`. Confirma RN-006 (DT-NASC obrigatória). |
| 6 | Sexo deve ser 'M' ou 'F'; caso contrário, rejeitar. | Unwanted | `CADBENEF.NSN#L129-L133` | Inferida | `BENEFICIARIO.SEXO`. |
| 7 | Na inclusão (I), se o CPF já existir, o sistema deve rejeitar ("BENEFICIARIO JA CADASTRADO"). | Unwanted | `CADBENEF.NSN#L141-L145` | **Confirmada** | Confirma RN-002 (CPF único para beneficiário). |
| 8 | Na alteração (A), se o CPF não existir, o sistema deve rejeitar ("NAO ENCONTRADO PARA ALTERACAO"). | Unwanted | `CADBENEF.NSN#L147-L151` | Inferida | Espelho da regra 7 para alteração. |
| 9 | Na inclusão, o status inicial do beneficiário deve ser 'A' (ativo). | Ubiquitous | `CADBENEF.NSN#L160-L162` | **Confirmada** | `BENEFICIARIO.STATUS`. Confirma fluxo de cadastro (status inicial ativo). |
| 10 | **Quando a idade do beneficiário for maior que 75 anos, o sistema deve definir o status como 'S' (suspenso).** | Event-driven | `CADBENEF.NSN#L165-L167` | **Confirmada (código)** | `BENEFICIARIO.STATUS`. **Esclarece MYS-001-checklist (status demográfico).** Incluído em 2011 ("ajuste status idoso"). ⚠️ Idoso >75 vira SUSPENSO — ver mistério (parece contra-intuitivo). |
| 11 | Na alteração (A), o sistema NÃO atualiza CPF, data de nascimento, sexo, programa, região nem NIS — apenas nome, endereço, contato, status, renda e dependentes. | State-driven | `CADBENEF.NSN#L195-L210` | Inferida | Campos imutáveis na alteração. Confirma parcialmente RN-009 (CPF antigo preservado) — mas aqui CPF simplesmente não é alterável. |
| 12 | O sistema deve registrar DT-CADASTRO (na inclusão) e DT-ATUALIZACAO (sempre) com a data atual. | Ubiquitous | `CADBENEF.NSN#L180-L210` | Inferida | `BENEFICIARIO.DT-CADASTRO`, `.DT-ATUALIZACAO`. |

### Mistérios encontrados em CADBENEF.NSN

<!-- mystery: MYS-001-checklist RESOLVIDO (status demográfico) — o programa modifica silenciosamente o status para 'S' (suspenso) quando idade > 75 (CADBENEF.NSN#L165-L167), sem mensagem ao operador. Rotulado "AJUSTE P/ BENEFICIARIOS ACIMA DE 75 ANOS", incluído em 2011. Por que um idoso de 75+ seria SUSPENSO (e não, por ex., priorizado)? Suspende o pagamento? Contra-intuitivo para um sistema de benefícios. Validar regra de negócio com facilitador/gestor — pode ser regra real (revisão obrigatória) ou bug. -->

<!-- mystery: MYS-010 NÃO resolvido aqui — RN-007/RN-008 (doc 2012) falam em "dados bancários (banco, agência, conta)" obrigatórios e regras de alteração bancária. Mas a VIEW de BENEFICIARIO em CADBENEF NÃO tem nenhum campo bancário (só endereço, CEP, telefone, RG). Os dados bancários NÃO estão neste programa nem aparentemente neste DDM. Onde ficam? Outro arquivo Adabas? Cruzar com o DDM BENEFICIARIO.ddm e PAGAMENTO.ddm. MYS-010 permanece aberto. -->

<!-- mystery: MYS-007 NÃO imposto aqui — CADBENEF aceita #NUM-DEP (N2, até 99) sem validar o "máximo 3 dependentes" da RN-004. Nenhum IF limita dependentes. Confirma que o limite de 3, se existir, está em CADDEPEND.NSN — ou não é imposto em lugar nenhum (RN-004 desatualizada). Ler CADDEPEND.NSN para fechar. -->

<!-- mystery: Região e programa não validados no cadastro — CADBENEF grava COD-REGIAO e COD-PROGRAMA direto do input sem verificar se existem/são válidos (ex.: região 99 pode ser cadastrada livremente). Isso alimenta o bypass de elegibilidade da região 99 (VALELEG). Não há FIND no PROGRAMA-SOCIAL para validar o vínculo, contradizendo RN-003 (vínculo obrigatório com programa). Possível lacuna de validação. -->

<!-- mystery: Status na alteração é sobrescrevível — na operação 'A', #STATUS vem do input e sobrescreve o status atual (CADBENEF.NSN#L203), sem regras de transição (ao contrário de VALELEG que conhece A/S/C/D/I). Um operador poderia reativar (→'A') um beneficiário suspenso/cancelado direto pelo cadastro, contornando regras de transição. Risco de integridade — confirmar se há controle de perfil/autorização (RN-009 menciona nível 2 p/ CPF, mas não p/ status). -->

---

## Regras de BATCHPGT.NSN

> Extraído por `/extract-business-rules` em **2026-06-10** lendo `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN` (~395 linhas), bloco a bloco.
> Cross-reference com `legado-sifap/legacy-docs/REGRAS-NEGOCIO-2012.md` §5 e `MANUAL-TECNICO-SIFAP-2008.md` §3.5.1. **Números de linha aproximados** (±2).

**Cabeçalho do programa:** Autor Carlos Roberto da Silva, 22/06/1997. Batch crítico (1º dia útil do mês). Alterações: 2000 (otimiz. ordem CPF), 2004 (inc. log erros), 2009 (ajuste 13º/abono), 2012 (novas faixas), 2015 (inc. auditoria). Lê arq. 150/155, grava arq. 160.

> 🔴 **DESCOBERTA ARQUITETURAL CRÍTICA:** apesar do cabeçalho dizer *"CHAMA CALCBENF E CALCDSCT"*, **BATCHPGT NÃO usa CALLNAT** — ele **duplica/reimplementa inline** toda a lógica de cálculo de benefício (fatores, 13º, abono) e de desconto. As tabelas de fatores regionais e faixas de renda estão **copiadas** de CALCBENF. Isso é um **risco de divergência grave** para a migração (ver mistério).

| #   | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
| --- | ------------------- | -------------- | ----- | ------------- | ----- |
| 1 | O sistema deve derivar a competência (AAAAMM) da data de execução do batch. | Ubiquitous | `BATCHPGT.NSN#L107-L110` | Inferida | Confirma RN §5.1 (processamento mensal). |
| 2 | O sistema deve processar beneficiários em ordem ascendente de CPF (descritor Adabas). | Ubiquitous | `BATCHPGT.NSN#L195-L198` | **Confirmada** | **Resolve MYS-005 parcialmente.** Comentário: *"OTIMIZACAO 1999"* + *"SISTEMAS DOWNSTREAM DEPENDEM DESTA ORDENACAO"*. ⚠️ Diverge da doc — ver abaixo. |
| 3 | Se o CPF for igual ao anterior (duplicata), o sistema deve ignorar o registro. | Unwanted | `BATCHPGT.NSN#L205-L210` | Inferida | Dedup por `#CPF-ANT`. Depende da ordenação por CPF (regra 2). |
| 4 | O sistema deve ignorar (não pagar) beneficiários com status ≠ 'A'. | Event-driven | `BATCHPGT.NSN#L213-L216` | **Confirmada** | `BENEFICIARIO.STATUS`. Confirma RN §5.1 (só ativos). |
| 5 | Se já existe pagamento na mesma competência para o CPF, o sistema deve ignorar (idempotência mensal). | Unwanted | `BATCHPGT.NSN#L219-L228` | **Confirmada** | `PAGAMENTO.COMPETENCIA`. Evita pagamento duplo no mês. |
| 6 | Se o programa do beneficiário não for encontrado, o sistema deve registrar erro no log e contabilizá-lo, sem interromper o batch. | Unwanted | `BATCHPGT.NSN#L237-L245` | **Confirmada** | **Resolve MYS-008 parcialmente.** Confirma RN §5.2 (erros individuais não param o batch). |
| 7 | Se o programa estiver inativo (STATUS-PROG ≠ 'A'), o sistema deve ignorar o beneficiário. | Event-driven | `BATCHPGT.NSN#L246-L249` | Inferida | `PROGRAMA-SOCIAL.STATUS-PROG`. |
| 8 | O sistema deve calcular o benefício com a MESMA fórmula de CALCBENF (base × fator_reg × fator_fam × fator_rnd × fator_idade × (1+reajuste)). | Ubiquitous | `BATCHPGT.NSN#L255-L300` | **Confirmada** | **Duplicação** de CALCBENF BR-5 a BR-11. Mesmas tabelas, mesmos magic numbers. |
| 9 | Em dezembro, o sistema deve calcular 13º (base × fator_reg × fator_idade) e abono natalino de 15% para tipo 'A'. | Event-driven | `BATCHPGT.NSN#L303-L316` | **Confirmada** | **Duplicação** de CALCBENF BR-12/BR-13. Inclui o mesmo descarte de fator_fam/fator_rnd no 13º. |
| 10 | O sistema deve aplicar desconto simplificado de 3% quando o bruto > 500. | Event-driven | `BATCHPGT.NSN#L319-L324` | **Confirmada** | ⚠️ Usa o desconto **simplificado** (3%), **NÃO** a lógica completa de CALCDSCT (faixas 3/5/7/9%, teto 30%, judicial). Divergência grave — ver mistério. |
| 11 | O sistema deve aplicar piso zero ao líquido e truncar para 2 casas. | Ubiquitous | `BATCHPGT.NSN#L327-L334` | **Confirmada** | Mesma regra de CALCBENF BR-14/BR-11. |
| 12 | O sistema deve atribuir número de pagamento sequencial (último NUM-PAGTO + 1) e gravar com status 'G' (gerado). | Ubiquitous | `BATCHPGT.NSN#L180-L184` · `#L337-L350` | Inferida | `PAGAMENTO.NUM-PAGTO`, `.STATUS-PGTO='G'`. Sequência obtida por READ DESCENDING. |
| 13 | O sistema deve acumular totalizadores (bruto, desconto, líquido, abono) e contadores (processados, gerados, ignorados, erros) e emitir resumo final. | Ubiquitous | `BATCHPGT.NSN#L352-L375` | **Confirmada** | **Resolve MYS-005 (totalizadores).** Acumuladores `#VLR-TOTAL-*` (N13.2). Confirma RN §5 (relatório de controle). |

### Mistérios encontrados em BATCHPGT.NSN

<!-- mystery: 🔴 DUPLICAÇÃO DE LÓGICA (alta severidade) — BATCHPGT reimplementa inline o cálculo de benefício em vez de chamar CALCBENF via CALLNAT (apesar do cabeçalho afirmar que chama). As tabelas de fatores regionais (27 linhas) e faixas de renda estão copiadas literalmente. CONSEQUÊNCIA: qualquer divergência entre as duas cópias produz valores diferentes no batch vs. no cálculo online. Já há divergência confirmada nos descontos (ver abaixo). Para a migração: unificar numa única regra de cálculo. Verificar se as tabelas estão realmente idênticas byte-a-byte ou se já divergiram. -->

<!-- mystery: 🔴 DIVERGÊNCIA DE DESCONTOS batch vs online — BATCHPGT aplica apenas desconto "simplificado" de 3% sobre bruto>500 (BATCHPGT.NSN#L319-L324), enquanto CALCDSCT (o cálculo "completo") aplica faixas progressivas 3/5/7/9%, teto de 30%, descontos judiciais/pensão/sindical etc. Ou seja, o pagamento mensal em lote NÃO aplica os descontos completos. Quando/se CALCDSCT roda sobre os pagamentos gerados pelo batch? É um passo separado (D+? no calendário)? Impacto financeiro direto. Investigar o fluxo: BATCHPGT gera com status 'G' — quem processa 'G' → desconto completo depois? -->

<!-- mystery: MYS-005 (ordenação) CONFIRMADO como risco real — comentário explícito "SISTEMAS DOWNSTREAM DEPENDEM DESTA ORDENACAO" (BATCHPGT.NSN#L193). A doc (REGRAS-NEGOCIO §5.1, nota) dizia que a ordem é alfabética por NOME (BN-NM-BENEF). MAS o código lê BY CPF, não por nome. Contradição doc vs código: a ordenação real é por CPF. Qual "sistema downstream" depende disso e por quê? (CNAB? conciliação BATCHCON?) Mapear antes de mudar a ordem na migração. -->

<!-- mystery: MYS-008 (reprocessamento) só parcialmente resolvido — BATCHPGT conta erros (#QTD-ERROS) e segue (não há ABEND U4038 nem MAX-ERROS aqui, ao contrário do que a doc §5.2 descreve). Não há lógica de checkpoint/restart nem de reprocessamento de status 'E' neste programa. A doc menciona status 'E' (erro) mas o código grava só 'G' (gerado). Onde vive o MAX-ERROS=100 / ABEND / status 'E' descritos na doc? Talvez em versão diferente ou em BATCHCON. Aberto. -->

<!-- mystery: Dedup depende de ordenação frágil — a eliminação de CPF duplicado (#CPF-ANT) só funciona porque a leitura é ordenada por CPF; duplicatas só são detectadas se consecutivas. Se a ordenação mudar (ver acima), a dedup quebra silenciosamente. Acoplamento implícito perigoso. -->

<!-- mystery: Auditoria de 2015 invisível — o cabeçalho registra "10/07/2015 - ANDERSON LIMA - INC AUDITORIA", mas não há nenhuma escrita em arquivo de auditoria (AUDITORIA.ddm) nem CALLNAT para LOGAUDIT visível no corpo do programa. Onde está o código de auditoria que a alteração de 2015 supostamente adicionou? Possível em copycode/INCLUDE não mostrado, ou alteração perdida. Cruzar com RELAUDIT.NSN e DDM AUDITORIA. -->

<!-- mystery: #LOG-WORK declarado e não usado — a estrutura #LOG-WORK (A01/3) com #LOG-ERRO (A120) é declarada (BATCHPGT.NSN#L101-L103) mas nunca referenciada no corpo. Os erros são escritos via WRITE direto, não nessa estrutura. Resquício de implementação de log abandonada (alteração 2004 "INC LOG ERROS")? Código morto. -->

---

## Regras de CADDEPEND.NSN

> Extraído por `/extract-business-rules` em **2026-06-10** lendo `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN` (~165 linhas), bloco a bloco. **Números de linha aproximados** (±2).

**Cabeçalho do programa:** Autor Ana Lucia Pereira, 20/06/1998. Alteração: 2008 (ajuste PE group). Inclui dependentes no grupo periódico (PE) `BENEFICIARIO.DEPENDENTES` do arq. 150.

| #   | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
| --- | ------------------- | -------------- | ----- | ------------- | ----- |
| 1 | Se o beneficiário titular não existir, o sistema deve rejeitar a inclusão de dependentes. | Unwanted | `CADDEPEND.NSN#L44-L52` | Inferida | `BENEFICIARIO.CPF`. |
| 2 | Se o titular estiver cancelado ('C') ou desligado ('D'), o sistema NÃO deve permitir inclusão de dependentes. | Event-driven | `CADDEPEND.NSN#L54-L57` | **Confirmada** | `BENEFICIARIO.STATUS`. |
| 3 | **O sistema deve permitir no máximo 5 dependentes (não 3).** | State-driven | `CADDEPEND.NSN#L61-L64` | **Confirmada (código)** | **RESOLVE MYS-007.** Limite real é `#NUM-DEP > 5` → para. Contradiz a RN-004 da doc ("máximo 3"). A doc estava **desatualizada**. Coerente com o fator familiar de CALCBENF, que calcula até 5+. |
| 4 | Nome do dependente é obrigatório. | Unwanted | `CADDEPEND.NSN#L78-L81` | Inferida | `DEPENDENTES.NOME-DEP`. |
| 5 | Parentesco deve ser 'FI' (filho), 'CO' (cônjuge), 'IR' (irmão) ou 'OU' (outro); caso contrário, rejeitar. | Unwanted | `CADDEPEND.NSN#L83-L88` | **Confirmada** | `DEPENDENTES.PARENTESCO`. Valores válidos descobertos. |
| 6 | Se o CPF do dependente já estiver cadastrado (e ≠ 0), o sistema deve rejeitar duplicata. | Unwanted | `CADDEPEND.NSN#L91-L101` | Inferida | `DEPENDENTES.CPF-DEP`. CPF do dependente é opcional (permite 0). |
| 7 | O sistema deve incrementar NUM-DEPENDENTES e gravar o dependente na próxima posição do grupo periódico. | Ubiquitous | `CADDEPEND.NSN#L104-L120` | Inferida | `BENEFICIARIO.NUM-DEPENDENTES`, grupo PE `DEPENDENTES`. |

### Mistérios encontrados em CADDEPEND.NSN

<!-- mystery: MYS-007 RESOLVIDO — o limite real de dependentes é 5 (CADDEPEND.NSN#L61-L64: "IF #NUM-DEP > 5"), não 3 como diz a RN-004 da doc 2012. A doc estava desatualizada. Confirma indiretamente o fator familiar de CALCBENF que calcula faixas até "5+". Para a migração: usar limite 5, mas a fórmula de CALCBENF aceita >5 sem teto — verificar consistência (o cadastro limita a 5, mas o cálculo não tem teto superior). -->

<!-- mystery: Limite 5 vs fórmula sem teto — CADDEPEND impede cadastrar o 6º dependente (limite 5), mas o fator familiar de CALCBENF (L187-L199) tem ramo para "> 4" sem limite superior, sugerindo que já houve (ou se esperava) beneficiários com 5+ dependentes. Dados legados podem ter registros com >5 dependentes vindos de versão anterior (antes do limite). Verificar nos dados reais. -->

<!-- mystery: CPF de dependente opcional — a dedup só vale "AND #CPF-DEP NE 0" (CADDEPEND.NSN#L96), ou seja, dependentes sem CPF (crianças?) podem ser cadastrados em duplicata livremente. Possível brecha de cadastro duplicado para dependentes sem CPF. -->

---

## Regras de BATCHCON.NSN

> Extraído por `/extract-business-rules` em **2026-06-10** lendo `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN` (~315 linhas), bloco a bloco.
> Cross-reference com `legado-sifap/legacy-docs/MANUAL-TECNICO-SIFAP-2008.md` §3.5.3 e `REGRAS-NEGOCIO-2012.md` §6. **Números de linha aproximados** (±2).

**Cabeçalho do programa:** Autor Marcos Antonio Ribeiro, 05/03/2000. Alterações: 2005 (inc. Banco Real), 2008 (ajuste CNAB 240), **2014 (inc. auditoria)**. Concilia arq. 160 (PAGAMENTO) com retorno bancário CNAB 240 (Banco do Brasil); grava arq. 170 (AUDITORIA).

| #   | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
| --- | ------------------- | -------------- | ----- | ------------- | ----- |
| 1 | O sistema deve processar apenas registros de detalhe (tipo = '3') do arquivo CNAB 240. | Event-driven | `BATCHCON.NSN#L130-L133` | **Confirmada** | **Resolve MYS-009 parcialmente.** Layout CNAB 240 BB com posições fixas (banco 1-3, CPF 44-54, valor 120-134, dt 140-147, cod-ret 231-232, num-doc 74-83). |
| 2 | O sistema deve converter o valor do retorno de centavos para reais (÷100). | Ubiquitous | `BATCHCON.NSN#L150-L154` | Inferida | Valor CNAB em centavos. |
| 3 | O sistema deve casar o retorno com o pagamento por NUM-PAGTO + CPF + competência; se não casar, contabilizar "não encontrado". | Event-driven | `BATCHCON.NSN#L158-L172` | **Confirmada** | `PAGAMENTO.NUM-PAGTO`, `.CPF-BENEF`, `.COMPETENCIA`. |
| 4 | Se a diferença entre o líquido do SIFAP e o valor do banco for maior que R$ 0,01, o sistema deve registrar divergência e gravar auditoria. | Event-driven | `BATCHCON.NSN#L175-L188` | **Confirmada** | **Resolve MYS-009.** Tolerância de 1 centavo. Magic number `0.01`. |
| 5 | Quando conciliado com código de retorno '00', o sistema deve marcar o pagamento como 'P' (pago), gravar data de pagamento e banco. | Event-driven | `BATCHCON.NSN#L192-L202` | **Confirmada** | `PAGAMENTO.STATUS-PGTO='P'`, `.DT-PAGAMENTO`. Cod-ret '00' = sucesso. |
| 6 | Quando o código de retorno for '01', o sistema deve marcar o pagamento como 'D' (devolvido). | Event-driven | `BATCHCON.NSN#L203-L210` | **Confirmada** | `PAGAMENTO.STATUS-PGTO='D'`. |
| 7 | **Quando o código de retorno for '02', o sistema deve marcar o pagamento como 'E' (erro).** | Event-driven | `BATCHCON.NSN#L211-L218` | **Confirmada** | **Resolve MYS-008 (status 'E').** O status 'E' vem da conciliação bancária, NÃO do BATCHPGT. Esclarece a confusão da doc §5.2. |
| 8 | Se o código de retorno for desconhecido (≠ 00/01/02), o sistema deve registrar mensagem sem alterar status. | Unwanted | `BATCHCON.NSN#L219-L223` | Inferida | Ramo NONE do DECIDE. |
| 9 | O sistema deve gravar registro de auditoria para cada conciliação (ação 'CO') e cada divergência (ação 'DV'), com sequencial, data/hora, usuário 'BATCH'. | Ubiquitous | `BATCHCON.NSN#L~260-L300` (subrotinas) | **Confirmada** | **Resolve MYS-011 parcialmente.** `AUDITORIA` (arq.170): SEQ-AUDIT, DT/HR-EVENTO, USUARIO, ACAO, TABELA-REF, CHAVE-REF, VLR-ANTERIOR/NOVO, DESCRICAO. Auditoria incluída em 2014. |
| 10 | O sistema deve acumular contadores (lidos, conciliados, divergentes, não encontrados, auditoria) e emitir resumo final. | Ubiquitous | `BATCHCON.NSN#L240-L255` | Inferida | Relatório de controle da conciliação. |

### Mistérios encontrados em BATCHCON.NSN

<!-- mystery: MYS-008/MYS-009/MYS-011 amplamente RESOLVIDOS — (1) o status 'E' (erro) vem do código de retorno bancário '02' na conciliação (BATCHCON.NSN#L211-L218), não do BATCHPGT; (2) a conciliação compara líquido SIFAP x valor banco com tolerância 0,01 (MYS-009); (3) a auditoria (AUDITORIA.ddm) é gravada AQUI, não no BATCHPGT (MYS-011) — esclarece por que BATCHPGT não tinha código de auditoria visível apesar do header de 2015. -->

<!-- mystery: Código morto — integração Banco Real — todo o bloco de leitura do RETORNO_REAL.DAT (BATCHCON.NSN#L227-L240) está comentado, com nota "BANCO REAL ADQUIRIDO PELO SANTANDER EM 2007 - MANTER PARA REFERENCIA HISTORICA". Refere-se à subrotina CONCILIA-REAL que provavelmente não existe mais. Código morto desde 2007. Para a migração: descartar, mas confirmar que nenhum dado de Banco Real ainda chega. -->

<!-- mystery: Layout CNAB 240 hardcoded por posição — todas as posições do CNAB (SUBSTR) são fixas e hardcoded para o layout do Banco do Brasil (BATCHCON.NSN#L120-L145). Outros bancos pagadores têm layout diferente (o próprio código morto do Banco Real comprova). Há outros bancos ativos? O sistema só concilia BB? Verificar PAGAMENTO.COD-BANCO (hardcoded como 1 na conciliação '00'). -->

<!-- mystery: Status 'P' final vs 'P' pendente — BATCHCON marca 'P' = PAGO no retorno '00' (L196). Mas a doc/CALCBENF usava 'P' = PENDENTE e BATCHPGT grava 'G' = GERADO. Há colisão de significado do código 'P' entre programas? Mapear a máquina de estados completa de STATUS-PGTO: G(gerado)→P(pago)/D(devolvido)/E(erro). Confirmar que 'P' pendente (doc) e 'P' pago (BATCHCON) não conflitam nos dados. -->

<!-- mystery: Divergência não bloqueia pagamento — quando há divergência de valor (SIFAP≠banco), BATCHCON apenas grava auditoria 'DV' mas NÃO altera o status do pagamento nem impede nada (L175-L188). O pagamento divergente fica no status anterior. Quem resolve a divergência depois? Processo manual (doc §4.3 menciona "análise manual pelo CGPB")? Sem follow-up automático. -->

---

## Regras de CADPROG.NSN

> Extraído por `/extract-business-rules` em **2026-06-10** lendo `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN` (~165 linhas), bloco a bloco. **Números de linha aproximados** (±2).

**Cabeçalho do programa:** Autor Marcos Antonio Ribeiro, 10/09/1997. Alterações: **2003 (inc. FATOR CORRECAO)**, 2012 (novos cód. elegibilidade). Inclusão (I) e consulta (C) de programas sociais no arq. 155 (PROGRAMA-SOCIAL).

| #   | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
| --- | ------------------- | -------------- | ----- | ------------- | ----- |
| 1 | Se a operação não for 'I' nem 'C', o sistema deve rejeitar. | Unwanted | `CADPROG.NSN#L50-L53` | Inferida | `#OPER`. |
| 2 | Na inclusão, se o código de programa já existir, o sistema deve rejeitar ("PROGRAMA JA CADASTRADO"). | Unwanted | `CADPROG.NSN#L82-L90` | Inferida | `PROGRAMA-SOCIAL.COD-PROGRAMA`. |
| 3 | **O sistema deve ajustar o valor base na inclusão: VLR-BASE armazenado = VLR-BASE informado × FATOR-K, onde FATOR-K = 1.00 + (FATOR-REAJUSTE × 0.347215).** | Ubiquitous | `CADPROG.NSN#L93-L95` | **Confirmada (código)** | **RESOLVE MYS-001 (Fator-K).** `#FATOR-K (N5.6)` existe AQUI, não em CALCBENF. Magic constant `0.347215`. O VLR-BASE gravado já vem ajustado — por isso CALCBENF não tem Fator-K. Incluído em 2003 ("INC FATOR CORRECAO"). |
| 4 | Na inclusão, o programa recebe status 'A' (ativo) por padrão. | Ubiquitous | `CADPROG.NSN#L106` | Inferida | `PROGRAMA-SOCIAL.STATUS-PROG`. |
| 5 | O sistema deve permitir DT-FIM = 0 para indicar programa por prazo indeterminado. | Ubiquitous | `CADPROG.NSN#L70` | Inferida | `PROGRAMA-SOCIAL.DT-FIM`. |
| 6 | Tipos de programa válidos são A (assistencial), P (previdenciário) e T (trabalho). | Ubiquitous | `CADPROG.NSN#L16` (comentário) · `#L63` | Inferida | `PROGRAMA-SOCIAL.TIPO`. Confirma os 3 tipos vistos em VALELEG/CALCBENF. |

### Mistérios encontrados em CADPROG.NSN

<!-- mystery: MYS-001 (Fator-K) RESOLVIDO — o lendário "Fator K" existe em CADPROG.NSN#L93-L95 como #FATOR-K (N5.6), calculado uma única vez na INCLUSÃO do programa: #FATOR-K = 1.00 + (#FATOR-REAJ * 0.347215), e o VLR-BASE é gravado já multiplicado por ele (#VLR-CALC = #VLR-BASE * #FATOR-K). Por isso Marcos Antônio "não soube explicar" e por isso NÃO aparece em CALCBENF: o ajuste é aplicado no cadastro do programa, não no cálculo do benefício. A constante mágica 0.347215 não tem origem documentada — PRECISA de validação (de onde vem 0.347215?). ⚠️ Impacto: o VLR-BASE no arq.155 já está ajustado; recalcular o Fator-K na migração causaria dupla aplicação. -->

<!-- mystery: Constante 0.347215 sem origem — o multiplicador 0.347215 (CADPROG.NSN#L93) não tem explicação no código nem na doc. Pode ser um índice econômico histórico de 2003 (ano da alteração). Investigar com facilitador/área econômica. É o coração financeiro do valor base de todo programa. -->

<!-- mystery: Reajuste aplicado duas vezes? — CADPROG já ajusta VLR-BASE pelo Fator-K (que usa FATOR-REAJUSTE) na inclusão, MAS CALCBENF/BATCHPGT aplicam NOVAMENTE "* (1 + #FATOR-REAJ)" no cálculo mensal (CALCBENF BR-10). Como o VLR-BASE armazenado já embute parte do reajuste via Fator-K, há possível DUPLA aplicação do reajuste. Confirmar a semântica: Fator-K (cadastro) vs reajuste mensal (cálculo) são reajustes distintos ou o mesmo aplicado duas vezes? Impacto financeiro direto e sistêmico. -->

---

## Regras de CALCCORR.NSN

> Extraído por `/extract-business-rules` em **2026-06-10** lendo `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN` (~230 linhas), bloco a bloco. **Números de linha aproximados** (±2).

**Cabeçalho do programa:** Autora Patricia Gomes de Souza, 12/07/2001. Alterações: 2006 (novos índices IPCA), 2014 (ajuste período). Correção retroativa de pagamentos por IPCA no arq. 160 (PAGAMENTO).

| #   | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
| --- | ------------------- | -------------- | ----- | ------------- | ----- |
| 1 | Se a competência inicial for maior que a final, o sistema deve rejeitar ("PERIODO INVALIDO"). | Unwanted | `CALCCORR.NSN#L143-L146` | Inferida | Validação de período. |
| 2 | O sistema deve processar apenas pagamentos do CPF dentro do período [comp-inicial, comp-final]. | Event-driven | `CALCCORR.NSN#L155-L168` | Inferida | `PAGAMENTO.CPF-BENEF`, `.COMPETENCIA`. |
| 3 | O sistema NÃO deve corrigir pagamentos já corrigidos (IND-CORRIGIDO = 'S'). | State-driven | `CALCCORR.NSN#L170-L172` | **Confirmada** | `PAGAMENTO.IND-CORRIGIDO`. Idempotência da correção. |
| 4 | O sistema deve acumular o índice IPCA mês a mês do período e aplicar VLR-CORRIGIDO = VLR-BRUTO × índice acumulado. | Ubiquitous | `CALCCORR.NSN#L175-L185` · `#L~210-L228` (CALC-INDICE-ACUM) | **Confirmada** | Tabela IPCA mensal por ano. Correção composta (produto de (1+ipca)). |
| 5 | O sistema só deve gravar correção quando a diferença for positiva (VLR-DIFF > 0). | Event-driven | `CALCCORR.NSN#L188-L198` | Inferida | `PAGAMENTO.VLR-CORRECAO`, `.DT-CORRECAO`. Não há correção negativa. |
| 6 | O sistema deve truncar o valor corrigido para 2 casas e marcar IND-CORRIGIDO='S'. | Ubiquitous | `CALCCORR.NSN#L182-L193` | Inferida | Mesmo padrão de truncamento. |

### Mistérios encontrados em CALCCORR.NSN

<!-- mystery: Tabela IPCA incompleta/congelada — a tabela #IPCA-ANO só carrega valores para 2010, 2011 e 2012 (CALCCORR.NSN#L70-L120), apesar de declarada para 10 anos (#ANO-TAB N4/10). "ULTIMA CARGA: 2014" no comentário. Pagamentos de anos fora de 2010-2012 NÃO encontram índice → CALC-INDICE-ACUM não acha o ano e o índice fica 1.0 (sem correção). Correção retroativa só funciona para 2010-2012. Dados de outros anos passam sem correção silenciosamente. Risco de cálculo incorreto. -->

<!-- mystery: Código morto — correção Plano Verão — bloco comentado (CALCCORR.NSN#L122-L140) com correção do Plano Verão 1989-1991 (transição Cruzado→Cruzeiro), fatores 2.7500 e 1.4289, status 'V'. Mantido "NAO REMOVER (HISTORICO)". Código morto desde sempre (comentado). Para a migração: descartar, mas registra contexto histórico de hiperinflação. -->

<!-- mystery: Correção só para cima — VLR-DIFF > 0 (CALCCORR.NSN#L188): só grava se o valor corrigido for MAIOR que o original. IPCA negativo (deflação) seria ignorado. Provavelmente intencional (não reduzir benefício pago), mas não documentado. -->

---

## Regras de CONSBENF.NSN

> Extraído por `/extract-business-rules` em **2026-06-10** lendo `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN` (~225 linhas), bloco a bloco. Programa de consulta online (tela 3270 via MAP). **Números de linha aproximados** (±2).

**Cabeçalho do programa:** Autora Marcia Helena Oliveira, 28/09/1998. Alterações: 2003 (inc. máscara CPF), 2007 (inc. hist. pagtos), 2012 (ajuste tela MAP). Consulta dados do arq. 150 + histórico do arq. 160.

| #   | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
| --- | ------------------- | -------------- | ----- | ------------- | ----- |
| 1 | O sistema deve permitir busca por CPF ('C') ou NIS ('N'); padrão é CPF. | Event-driven | `CONSBENF.NSN#L120-L139` | Inferida | `BENEFICIARIO.CPF`, `.NIS`. |
| 2 | O sistema deve mascarar o CPF na exibição (formato ***.***.XXX-XX) para ocultar dados sensíveis. | Ubiquitous | `CONSBENF.NSN#L150-L153` · `#L~200-L220` (MASCARA-CPF) | **Confirmada** | **Conforme regra de segurança (mascarar CPF).** Mas ver mistério crítico abaixo. |
| 3 | O sistema deve traduzir o status para descrição: A=ATIVO, S=SUSPENSO, C=CANCELADO, I=INATIVO, D=DESLIGADO. | Ubiquitous | `CONSBENF.NSN#L160-L178` | **Confirmada** | **Mapa de status completo confirmado.** Mesmos 5 status de VALELEG. |
| 4 | O sistema deve exibir o histórico dos últimos 12 pagamentos do beneficiário. | Ubiquitous | `CONSBENF.NSN#L195-L215` | Inferida | `PAGAMENTO` por CPF. Limite 12. |

### Mistérios encontrados em CONSBENF.NSN

<!-- mystery: 🔴 BUG CONHECIDO de máscara de CPF (segurança) — comentário explícito (CONSBENF.NSN#L~198-L203): "INCONSISTENCIA CONHECIDA - AS VEZES MOSTRA PRIMEIROS 3 DIGITOS AO INVES DOS ULTIMOS. DEPENDE DO TAMANHO DO CPF ARMAZENADO. NAO CORRIGIR SEM APROVACAO DA AUDITORIA." Quando CPF < 11 dígitos (preenchido com zeros à esquerda), a máscara expõe os 3 PRIMEIROS dígitos em vez de mascarar corretamente (CONSBENF.NSN#L205-L210). É um vazamento de dado sensível conhecido e deliberadamente não corrigido. ⚠️ Para a migração: corrigir a máscara, mas registrar a aprovação de auditoria exigida. Vetor de exposição LGPD. -->

<!-- mystery: CPF armazenado com tamanho variável — a lógica da máscara revela que CPFs podem estar armazenados com MENOS de 11 dígitos (BENEFICIARIO.CPF < 10000000000), preenchidos com zeros à esquerda. Isso sugere dados inconsistentes no arq.150 (CPFs sem padding fixo). Impacto na migração: normalizar todos os CPFs para 11 dígitos e validar dígito verificador (alguns podem ser inválidos). -->

<!-- mystery: MYS-010 (dados bancários) — AINDA não encontrados — CONSBENF exibe endereço, CEP, mas NENHUM dado bancário (banco/agência/conta). Confirma que os dados bancários da RN-007/RN-008 não estão no arq.150 (BENEFICIARIO). Hipótese: ficam no arq.160 (PAGAMENTO) via COD-BANCO (visto em BATCHCON), ou em arquivo não presente no legado fornecido. Verificar DDM PAGAMENTO.ddm. -->

---

## Regras de VALBENEF.NSN

> Extraído por `/extract-business-rules` em **2026-06-10** lendo `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN` (~290 linhas), bloco a bloco. Rotina de validação cadastral. **Números de linha aproximados** (±2).

**Cabeçalho do programa:** Autora Marcia Helena Oliveira, 08/01/1998. Alterações: 2005 (ajuste valid. CPF), 2010 (inc. valid. nome). Valida dados antes da gravação no arq.150.

| #   | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
| --- | ------------------- | -------------- | ----- | ------------- | ----- |
| 1 | O sistema deve validar o CPF por Módulo 11, acumulando mensagens de erro em vez de parar na primeira. | Unwanted | `VALBENEF.NSN#L113-L120` · `#L~175-L235` (VALIDA-CPF-COMPLETO) | **Confirmada** | Validação não-curto-circuito (array `#MSG-ERRO(10)`). |
| 2 | O sistema deve rejeitar CPF com todos os dígitos iguais (ex.: 111.111.111-11). | Unwanted | `VALBENEF.NSN#L188-L205` | **Confirmada** | Proteção contra CPF "sequência". |
| 3 | O sistema deve validar a data de nascimento (ano 1900..ano atual; mês 1-12; dia conforme tabela de dias por mês, fevereiro=29). | Unwanted | `VALBENEF.NSN#L122-L130` · `#L~250-L268` (VALIDA-DATA) | **Confirmada** | Fevereiro sempre 29 (não checa bissexto real) — ver mistério. |
| 4 | O sistema deve exigir nome com pelo menos um espaço (nome + sobrenome). | Unwanted | `VALBENEF.NSN#L132-L140` · `#L~270-L288` (VALIDA-NOME) | **Confirmada** | Incluído em 2010. |
| 5 | O sistema deve validar a UF contra tabela das 27 unidades federativas. | Unwanted | `VALBENEF.NSN#L145-L165` | **Confirmada** | Tabela `#UF-TAB(27)`. |
| 6 | O sistema deve validar o status contra os valores permitidos A/S/C/I/D. | Unwanted | `VALBENEF.NSN#L168-L175` | **Confirmada** | **Confirma máquina de status do BENEFICIARIO** (5 valores). |

### Mistérios encontrados em VALBENEF.NSN

<!-- mystery: 🥚 EASTER EGG / backdoor de CPF — VALIDA-CPF-COMPLETO (VALBENEF.NSN#L196-L201) tem exceção: CPF com todos os dígitos iguais é INVÁLIDO, EXCETO se começar com '000' ("CPFs INICIADOS COM 000 SAO VALIDOS (TESTE GOVERNO)"). Ou seja, 000.000.000-00 e similares passam como válidos. Combinado com VALDOCS (prefixos especiais) e a região 99, forma um conjunto de backdoors de teste/governo. ⚠️ Vetor de fraude — confirmar se há registros 000* em produção. -->

<!-- mystery: Fevereiro fixo em 29 dias — a tabela #DIAS-MES(2)=29 (VALBENEF.NSN#L97) aceita 29/fev em QUALQUER ano, não checa bissexto real. 29/02/2011 (não bissexto) passaria como data válida. Bug de validação de data — baixo impacto mas presente. -->

<!-- mystery: VALBENEF vs CADBENEF — validação duplicada/divergente — VALBENEF valida UF, status, nome (nome+sobrenome), CPF-sequência; CADBENEF valida CPF, nome≠branco, sexo. As duas rotinas validam o MESMO cadastro com regras DIFERENTES. CADBENEF chama VALBENEF? Não há CALLNAT visível. Possível validação inconsistente conforme o ponto de entrada. Mapear quem chama VALBENEF (parece subprograma não invocado por CADBENEF). -->

---

## Regras de VALDOCS.NSN

> Extraído por `/extract-business-rules` em **2026-06-10** lendo `01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN` (~195 linhas), bloco a bloco. **Números de linha aproximados** (±2).

**Cabeçalho do programa:** Autora Ana Lucia Pereira, 14/05/1998. Alterações: 2003 (inc. valid. RG), 2011 (ajuste check especial). Valida CPF, RG e documentos complementares.

| #   | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
| --- | ------------------- | -------------- | ----- | ------------- | ----- |
| 1 | O sistema deve validar o CPF por Módulo 11 (CPF=0 é inválido). | Unwanted | `VALDOCS.NSN#L75-L82` · `#L~105-L155` (VALIDA-CPF-DOC) | **Confirmada** | Terceira cópia do algoritmo MOD-11 (após CADBENEF e VALBENEF). |
| 2 | O sistema deve exigir RG com pelo menos 5 caracteres. | Unwanted | `VALDOCS.NSN#L86-L92` · `#L~158-L178` (VALIDA-RG) | Inferida | `BENEFICIARIO.RG`. |
| 3 | Quando o CPF começar com um prefixo especial (000/001/002/010/011/099/100/999), o sistema deve marcá-lo como documento especial VÁLIDO, zerando todos os erros. | Event-driven | `VALDOCS.NSN#L95-L98` · `#L~180-L195` (CHECK-DOC-ESPECIAL) | **Confirmada (código)** | 🥚 **Backdoor de validação** — ver mistério. |

### Mistérios encontrados em VALDOCS.NSN

<!-- mystery: 🥚 EASTER EGG / backdoor de documentos — CHECK-DOC-ESPECIAL (VALDOCS.NSN#L180-L195) tem 8 prefixos de CPF (000,001,002,010,011,099,100,999) que, se baterem com os 3 primeiros dígitos do CPF, FORÇAM #CPF-OK=TRUE, #RESULTADO='V' e ZERAM #QTD-ERROS — anulando qualquer erro de validação anterior (CPF inválido, RG inválido). Qualquer CPF começando com esses prefixos passa por TODA a validação documental. ⚠️ Backdoor grave. Combinado com o '000' de VALBENEF e a região 99 de VALELEG, há múltiplos bypasses plantados. CRÍTICO para segurança — confirmar uso em produção e decidir tratamento na migração. -->

<!-- mystery: Terceira cópia do MOD-11 — o algoritmo de validação de CPF está duplicado em 3 programas (CADBENEF, VALBENEF, VALDOCS), cada um com pequenas variações (VALBENEF rejeita dígitos iguais; VALDOCS não; CADBENEF não checa o 000). Risco de validação inconsistente conforme o ponto de entrada. Unificar na migração. -->

<!-- mystery: VALDOCS lê TITULO e CTPS mas não os valida — o programa pede TITULO ELEITOR e CTPS no INPUT (VALDOCS.NSN#L60-L66) mas nenhuma sub-rotina os valida. Campos coletados e ignorados. Código incompleto ou validação removida. -->

---

## Regras de BATCHREL.NSN

> Extraído por `/extract-business-rules` em **2026-06-10** lendo `01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN` (~215 linhas), bloco a bloco. Relatório consolidado mensal. **Números de linha aproximados** (±2).

**Cabeçalho do programa:** Autora Patricia Gomes de Souza, 10/11/1999. Alterações: 2006 (inc. subtotais reg.), 2013 (ajuste formato). Sumariza pagamentos por região/programa/status; saída flat file para impressão.

| #   | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
| --- | ------------------- | -------------- | ----- | ------------- | ----- |
| 1 | O sistema deve agrupar pagamentos em 5 macrorregiões pela faixa de COD-REGIAO (1-5 Norte, 6-10 Nordeste, 11-15 Sudeste, 16-20 Sul, demais Centro-Oeste). | Event-driven | `BATCHREL.NSN#L120-L140` | **Confirmada** | **Mapeamento região→macrorregião confirmado.** Região 99 cai em Centro-Oeste (ramo ELSE) — possível distorção. |
| 2 | O sistema deve totalizar por status de pagamento: G(gerado), P(pago), C(cancelado), D(devolvido), E(estornado). | Event-driven | `BATCHREL.NSN#L160-L178` | **Confirmada** | **Esclarece máquina de status PAGAMENTO**: 'E'=estornado aqui (BATCHCON usava 'E'=erro!) — ver mistério. |
| 3 | O sistema deve produzir resumo por região, por status e total geral. | Ubiquitous | `BATCHREL.NSN#L185-L215` | Inferida | Relatório de controle. Paginação 66 linhas (mainframe). |

### Mistérios encontrados em BATCHREL.NSN

<!-- mystery: 🔴 ARREDONDAMENTO DIVERGENTE — comentário explícito (BATCHREL.NSN#L143): "NOTA: ARREDONDAMENTO DIFERE DO CALCBENF (ROUND VS TRUNCATE)". BATCHREL ARREDONDA (+0.005 antes de truncar) o bruto ao totalizar (L144-L147), enquanto CALCBENF/CALCDSCT TRUNCAM. Os totais do relatório consolidado podem NÃO bater com a soma dos pagamentos individuais (diferença de arredondamento acumulada). ⚠️ Risco de conciliação contábil — os números oficiais do relatório divergem dos dados. Decidir política única na migração. -->

<!-- mystery: 🔴 Colisão de significado do status 'E' — BATCHREL rotula 'E' como ESTORNADO (BATCHREL.NSN#L172-L173 / #NOME-STS(5)='ESTORNADO'), mas BATCHCON grava 'E' como ERRO (retorno bancário '02'). Mesmo código, dois significados. RELPGT também usa 'ESTORNAD'. Qual é o correto? A máquina de estados de STATUS-PGTO está ambígua: G/P/C/D/E com E=erro(BATCHCON) OU E=estornado(relatórios). CRÍTICO mapear antes da migração. -->

<!-- mystery: Status 'C' (cancelado) sem origem — BATCHREL e RELPGT contam status 'C'=cancelado em PAGAMENTO, mas NENHUM programa lido grava 'C' em STATUS-PGTO (BATCHPGT grava 'G', BATCHCON grava P/D/E). Quem cancela um pagamento? Programa não presente no legado ou operação manual? Status órfão na escrita. -->

---

## Regras de RELPGT.NSN

> Extraído por `/extract-business-rules` em **2026-06-10** lendo `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN` (~215 linhas), bloco a bloco. Relatório analítico de pagamentos. **Números de linha aproximados** (±2).

**Cabeçalho do programa:** Autora Ana Lucia Pereira, 17/12/1999. Alterações: 2004 (ajuste paginação), 2010 (inc. subtotal prog.). Listagem detalhada por período com totalizadores.

| #   | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
| --- | ------------------- | -------------- | ----- | ------------- | ----- |
| 1 | O sistema deve filtrar pagamentos por período [comp-inicial, comp-final] e, opcionalmente, por código de programa (0=todos). | Event-driven | `RELPGT.NSN#L90-L102` | Inferida | `PAGAMENTO.COMPETENCIA`, `.COD-PROGRAMA`. |
| 2 | O sistema deve emitir subtotais por programa (control break ao mudar COD-PROGRAMA). | Event-driven | `RELPGT.NSN#L105-L113` | **Confirmada** | Control-break clássico. |
| 3 | O sistema deve mascarar o CPF na listagem (***.XXX.XXX-XX). | Ubiquitous | `RELPGT.NSN#L125-L130` | **Confirmada** | Máscara diferente da de CONSBENF — ver mistério. |
| 4 | O sistema deve traduzir tipo (N/D/T) e status (G/P/C/D/E) de pagamento para descrição. | Ubiquitous | `RELPGT.NSN#L133-L165` | **Confirmada** | Tipo 'T'=TERCEIRO descoberto (além de N=normal, D=décimo). |

### Mistérios encontrados em RELPGT.NSN

<!-- mystery: Tipo de pagamento 'T' (TERCEIRO) sem origem — RELPGT traduz TIPO-PGTO 'T' como "TERCEIRO" (RELPGT.NSN#L137-L138), e o DDM PAGAMENTO comenta "N=NORMAL D=DECIMO T=TERCEIRO". Mas NENHUM programa de cálculo grava 'T' (CALCBENF/BATCHPGT gravam só 'N' ou 'D'). O que é um pagamento "TERCEIRO"? Terceiro turno? Pagamento a terceiros? Status órfão na escrita. Investigar. -->

<!-- mystery: Máscara de CPF inconsistente entre programas — RELPGT mascara como ***.XXX.XXX-XX expondo dígitos 4-11 (RELPGT.NSN#L125-L130), enquanto CONSBENF mascara ***.***.XXX-XX expondo só 7-11. Padrões de mascaramento DIFERENTES no mesmo sistema → exposição de dados inconsistente (RELPGT expõe MAIS dígitos). Questão LGPD. Unificar máscara na migração. -->

---

## Regras de RELAUDIT.NSN

> Extraído por `/extract-business-rules` em **2026-06-10** lendo `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN` (~225 linhas), bloco a bloco. Relatório da trilha de auditoria. **Números de linha aproximados** (±2).

**Cabeçalho do programa:** Autor Roberto Mendes Junior, 20/08/2002. Alterações: 2006 (inc. filtros), 2011 (ajuste formato), **2014 (LIMPEZA RELATORIO)**. Lista eventos do arq.170 (AUDITORIA) com filtros.

| #   | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
| --- | ------------------- | -------------- | ----- | ------------- | ----- |
| 1 | O sistema deve filtrar eventos de auditoria por período (default 1997-01-01 até hoje), ação, usuário e tabela. | Event-driven | `RELAUDIT.NSN#L88-L160` | Inferida | `AUDITORIA.DT-EVENTO`, `.ACAO`, `.USUARIO`, `.TABELA-REF`. |
| 2 | O sistema deve contabilizar e descrever as ações de auditoria: IN(inclusão), AL(alteração), CO(conciliação), CN(consulta), DV(divergência), outras. | Event-driven | `RELAUDIT.NSN#L168-L195` | **Confirmada** | **Vocabulário de ações de AUDITORIA confirmado.** Cruza com BATCHCON (grava 'CO' e 'DV'). |
| 3 | O sistema deve permitir saída em tela ('T') ou impressora ('I'). | Optional | `RELAUDIT.NSN#L195-L215` | Inferida | `#TIPO-SAIDA`. |

### Mistérios encontrados em RELAUDIT.NSN

<!-- mystery: 🔴 ENCOBRIMENTO DE AUDITORIA — RELAUDIT EXCLUI deliberadamente eventos de ação 'EX' (exclusão) do relatório: "FILTRO ACAO - EXCLUSOES NAO SAO EXIBIDAS" (RELAUDIT.NSN#L110-L114), contando-os apenas como "filtrados". Ou seja, a trilha de auditoria NÃO mostra exclusões. Combinado com a alteração de 2014 "LIMPEZA RELATORIO" (Anderson Lima), há suspeita de encobrimento: eventos de exclusão existem no arq.170 mas são ocultados no relatório. ⚠️ Grave para auditoria/compliance. Investigar: quem gera ação 'EX'? Por que são ocultadas? -->

<!-- mystery: Ação 'EX' (exclusão) gravada mas nunca exibida — o vocabulário de ações inclui IN/AL/CO/CN/DV (descritas) e 'EX' (oculta). Nenhum programa lido grava 'EX' nem 'IN'/'AL'/'CN' (só BATCHCON grava 'CO'/'DV'). Onde está o código que grava exclusões na auditoria? Provável LOGAUDIT (subprograma mencionado na doc RN-010) não presente no legado fornecido. As exclusões são auditadas (gravadas) mas escondidas do relatório. -->

<!-- mystery: "LIMPEZA RELATORIO" de 2014 — a última alteração (Anderson Lima, 15/09/2014) é rotulada "LIMPEZA RELATORIO". Coincide com o período de outras alterações suspeitas (região 99 em 2013, auditoria BATCHCON em 2014). Verificar no controle de versão o que essa "limpeza" removeu — pode ter sido a introdução do filtro de exclusões. -->

---

## Exemplo de linha bem preenchida

| ID     | Regra de Negócio                                                                        | Programa Fonte                                   | Campos DDM                                                               | Nível de Risco | Notas                                      |
| ------ | --------------------------------------------------------------------------------------- | ------------------------------------------------ | ------------------------------------------------------------------------ | -------------- | ------------------------------------------ |
| BR-013 | Desconto total não pode exceder 30% do valor bruto, exceto descontos judiciais (tipo J) | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L142-L148` | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.VLR-TOTAL-DSCT`, `PAGAMENTO.TIPO-DSCT` | CRÍTICO        | Regra financeira. Tipo 'J' = exceção legal |

## Regras por Categoria

### Cálculos Financeiros

<!-- Liste aqui as regras relacionadas a cálculos de valores, benefícios, etc. -->

### Validações de Status

<!-- Liste aqui as regras de transição de status (A, S, C, I, D) -->

### Regras de Autorização

<!-- Liste aqui as regras de quem pode fazer o quê -->

### Regras de Negócio Temporais

<!-- Liste aqui regras com prazos, datas-limite, períodos -->

## Resumo Estatístico

- Total de regras encontradas: \_\_\_
- Regras críticas: \_\_\_
- Regras com duplicação: \_\_\_
- Regras sem documentação (escondidas): \_\_\_

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
<a href="dependency-map.md"><strong>dependency-map.md</strong></a><br/>
<sub>Mapa de quem chama quem.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>


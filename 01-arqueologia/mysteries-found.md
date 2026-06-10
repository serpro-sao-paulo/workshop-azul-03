<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Mistérios Encontrados — SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **mysteries-found**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Registre aqui toda lógica, comportamento ou código que o time não conseguiu explicar.
> "Mistérios" são trechos de código sem documentação, com lógica não-óbvia ou que parecem workarounds.
>
> **Cota mínima para passar pelo portão do Estágio 2:** 5 mistérios documentados.

## O que conta como "mistério"?

- Código que faz algo inesperado sem comentário explicando por quê
- Valores hardcoded sem explicação (números mágicos)
- Lógica condicional que parece um workaround ou gambiarra
- Campos no DDM que não são usados por nenhum programa
- Programas que existem mas não são chamados por ninguém
- Comportamento diferente entre o que a documentação diz e o que o código faz
- Easter eggs deixados pelos desenvolvedores originais

## Níveis de Confiança

| Nível     | Significado                                         |
| --------- | --------------------------------------------------- |
| **ALTA**  | Temos certeza de que há algo estranho aqui          |
| **MÉDIA** | Parece suspeito, mas pode ter explicação            |
| **BAIXA** | Pode ser intencional, mas não conseguimos confirmar |

## Mistérios Catalogados

> 📋 **Catálogo consolidado por `/catalog-mysteries` (2ª passada, 2026-06-10)** a partir dos 15 programas `.NSN` e dos 4 DDMs lidos. Veja a seção **[Catálogo de Mistérios](#catálogo-de-mistérios--catalog-mysteries)** abaixo. A tabela-rascunho original do worksheet foi removida — substituída pelo catálogo real.

## Easter Eggs

> Existem **3 easter eggs** plantados no código legado. Encontrados na leitura:

1. [x] **CPF 000 sempre válido** — `VALBENEF.NSN` (VALIDA-CPF-COMPLETO ~L196-L201): CPF com todos os dígitos iguais é inválido, **exceto** se começar com `000` ("TESTE GOVERNO"). → ver MYS-E1.
2. [x] **8 prefixos de CPF anulam validação** — `VALDOCS.NSN` (CHECK-DOC-ESPECIAL ~L180-L195): prefixos `000/001/002/010/011/099/100/999` forçam validação OK e zeram erros. → ver MYS-E2.
3. [x] **Encobrimento de exclusões na auditoria** — `RELAUDIT.NSN` (~L110-L114) oculta ações `'EX'`; o próprio `AUDITORIA.ddm` alerta sobre isso. → ver MYS-E3.

> Bônus (4º backdoor): **região 99** concede elegibilidade total (`VALELEG.NSN`) — ver MYS-004.

## Resumo

- Total de mistérios catalogados: **45**
- Resolvidos pela leitura do código: **13** (incl. os 4 bloqueadores críticos originais)
- Em aberto — bloqueiam o Estágio 2 (Critical): **6**
- Em aberto — investigação (High): **13**
- Em aberto — facilitador (Medium): **6**
- Em aberto — estacionados (Low): **7**
- Easter eggs encontrados: **3 / 3** ✅ (+1 bônus)

---

## Catálogo de Mistérios — `/catalog-mysteries`

> 2ª passada · gerado em **2026-06-10** · Equipe: **[NOME DA EQUIPE]** · fontes: [business-rules-catalog.md](business-rules-catalog.md) (≈50 marcadores `<!-- mystery: -->`), [dependency-map.md](dependency-map.md) e [inventory.md](inventory.md). Números de linha aproximados (±2).

### Contagem por classificação

| Classificação | Severidade | Contagem |
| ------------- | ---------- | -------- |
| `resolved` — Respondido pelo código | — | 13 |
| `blocks-stage-2` — Bloqueia o Estágio 2 | **Critical** | 6 |
| `needs-investigation` — Investigar no Estágio 1 | **High** | 13 |
| `needs-facilitator` — Perguntar a mentor/gestor | **Medium** | 6 |
| `parked` — Documentar e seguir | **Low** | 7 |

### A. Mistérios RESOLVIDOS pela leitura do código

| ID | Mistério | Resolução (com fonte) |
| --- | --- | --- |
| MYS-001 | **Fator-K** | Existe em `CADPROG.NSN#L93-L95`: `#FATOR-K = 1.00 + (FATOR-REAJ × 0.347215)`, aplicado ao VLR-BASE na inclusão. Também é campo persistido `PROGRAMA-SOCIAL.FATOR-K` (DDM). ⚠️ Resta ambiguidade campo×calculado → MYS-026. |
| MYS-002 | **13º / dezembro / abono** | `CALCBENF.NSN#L240-L257`: mês=12 → tipo 'D', 13º = base×reg×idade; abono natalino 15% para tipo 'A'. |
| MYS-003 | **Exceção judicial ao teto 30%** | `CALCDSCT.NSN#L190-L195`: corte só roda se `#TIPO-DSCT NE 'J'`; judicial somado sem teto. |
| MYS-004 | **Bypass região 99** | `VALELEG.NSN#L108-L112`: região 99 → ELEGÍVEL imediato (ESCAPE), pula tudo. Incluído 2013. ⚠️ Vira risco de segurança → MYS-S1. |
| MYS-005 | **Ordenação × totalizadores** | `BATCHPGT.NSN`: lê `BY CPF` (não por nome como a doc dizia); comentário "DOWNSTREAM DEPENDEM". → risco em MYS-013. |
| MYS-007 | **Máx. dependentes** | `CADDEPEND.NSN#L61-L64`: limite real é **5** (não 3); doc desatualizada. DDM permite PE até 10. |
| MYS-008 | **Status 'E' / origem** | `BATCHCON.NSN#L211-L218`: 'E' vem do retorno bancário '02' na conciliação, não do BATCHPGT. |
| MYS-009 | **Conciliação BATCHCON** | `BATCHCON.NSN`: CNAB 240 BB, match por NUM-PAGTO+CPF+competência, tolerância R$0,01. |
| MYS-010 | **Dados bancários** | Estão no **PAGAMENTO (FNR 152)**: COD-BANCO/AGENCIA/CONTA/TIPO (DDM). São por pagamento, não por beneficiário. |
| MYS-011 | **Auditoria** | Gravada inline por `BATCHCON.NSN` no `AUDITORIA (FNR 153)`; `LOGAUDIT` citado na doc não existe no legado. |
| MYS-E1 | 🥚 **CPF 000 válido** | `VALBENEF.NSN#L196-L201`. |
| MYS-E2 | 🥚 **Prefixos de CPF anulam validação** | `VALDOCS.NSN#L180-L195`. |
| MYS-E3 | 🥚 **Auditoria oculta exclusões** | `RELAUDIT.NSN#L110-L114` + nota do `AUDITORIA.ddm`. |

### B. Mistérios EM ABERTO

| ID | Descrição | Fonte | Classificação | Severidade | Ação sugerida |
| --- | --- | --- | --- | --- | --- |
| **MYS-S1** | 🔴 **Backdoors de segurança** (região 99 + CPF 000 + 8 prefixos VALDOCS) concedem elegibilidade/validação sem checagem. Vetores de fraude plantados. | `VALELEG.NSN#L108`, `VALBENEF.NSN#L196`, `VALDOCS.NSN#L180` | `blocks-stage-2` | **Critical** | Decidir política na migração (preservar/restringir/auditar); validar com segurança/jurídico. Conferir registros reais. |
| **MYS-021** | 🔴 **Dupla aplicação de reajuste** — CADPROG ajusta VLR-BASE pelo Fator-K (usa FATOR-REAJUSTE) e CALCBENF/BATCHPGT reaplicam `×(1+FATOR-REAJ)`. | `CADPROG.NSN#L93`, `CALCBENF.NSN#L228` | `blocks-stage-2` | **Critical** | Confirmar se Fator-K e reajuste mensal são distintos ou duplicados. Impacto financeiro sistêmico. |
| **MYS-022** | 🔴 **Divergência de descontos batch×online** — BATCHPGT aplica só 3% simplificado; CALCDSCT aplica faixas/teto/judicial. Quando o desconto completo roda? | `BATCHPGT.NSN#L319`, `CALCDSCT.NSN` | `blocks-stage-2` | **Critical** | Mapear o passo que aplica CALCDSCT sobre pagamentos 'G'. |
| **MYS-023** | 🔴 **Duplicação de lógica de cálculo** — BATCHPGT reimplementa CALCBENF inline (tabelas copiadas), sem CALLNAT. Risco de divergência. | `BATCHPGT.NSN#L255-L316`; [dependency-map.md](dependency-map.md) | `blocks-stage-2` | **Critical** | Comparar tabelas/fórmulas byte-a-byte; unificar na migração. |
| **MYS-024** | 🔴 **Máquina de status do PAGAMENTO: DDM × código em conflito** — DDM define P=PEND/G/E=EMITIDO/C=CONFIRMADO/D/X/R; código usa G/P=pago/E=erro|estornado/C/D. | `PAGAMENTO.ddm` (SIT-PAGAMENTO) vs `BATCHCON/BATCHREL/RELPGT` | `blocks-stage-2` | **Critical** | Mapear máquina de estados real antes de migrar ~180 mi registros. |
| **MYS-025** | 🔴 **Colisão do status 'E'** — BATCHCON grava 'E'=erro; BATCHREL/RELPGT exibem 'E'=estornado. | `BATCHCON.NSN#L211`, `BATCHREL.NSN`, `RELPGT.NSN` | `blocks-stage-2` | **Critical** | Definir significado único de 'E'. |
| **MYS-013** | **Ordenação por CPF acoplada a downstream/dedup** — "SISTEMAS DOWNSTREAM DEPENDEM"; dedup `#CPF-ANT` quebra se a ordem mudar. | `BATCHPGT.NSN#L193-L210` | `needs-investigation` | **High** | Identificar qual sistema depende da ordem (CNAB? BATCHCON?). |
| **MYS-006** | **Pró-rata não implementado** — doc afirma cálculo proporcional; CALCBENF não tem. Comentário do 13º cita `MESES_ATIVOS/12` que não existe no código. | `CALCBENF.NSN#L237-L246` | `needs-investigation` | **High** | Confirmar se pró-rata existe em outro lugar ou foi removido. |
| **MYS-027** | **13º descarta fatores** — fórmula do 13º usa só reg+idade, omite fam e renda do benefício mensal. | `CALCBENF.NSN#L242` | `needs-investigation` | **High** | Confirmar intenção; impacto no valor do 13º. |
| **MYS-028** | **Teto de 30% dependente de ordem** — corte roda a cada iteração do PE; 'J' isento infla o acumulado. | `CALCDSCT.NSN#L190-L195` | `needs-investigation` | **High** | Verificar se a ordem do PE de descontos é determinística. |
| **MYS-029** | **Judicial pode ultrapassar 30%** do bruto (somado sem teto) e consumir o benefício. | `CALCDSCT.NSN#L128-L138` | `needs-investigation` | **High** | Confirmar se é intencional (ordem judicial prevalece). |
| **MYS-030** | **IPCA congelado** — tabela de correção só tem 2010-2012; anos fora ficam sem correção (índice 1.0) silenciosamente. | `CALCCORR.NSN#L70-L120` | `needs-investigation` | **High** | Validar política de correção e completar índices. |
| **MYS-031** | 🔴 **Bug de máscara de CPF (LGPD)** — quando CPF<11 dígitos, expõe os 3 primeiros; "NAO CORRIGIR SEM APROVACAO DA AUDITORIA". | `CONSBENF.NSN#L198-L210` | `needs-investigation` | **High** | Corrigir na migração com aprovação; tratar exposição. |
| **MYS-032** | **CPFs com tamanho variável** no arq.150 (zeros à esquerda) → dados inconsistentes. | `CONSBENF.NSN` (lógica da máscara) | `needs-investigation` | **High** | Normalizar CPFs para 11 dígitos e revalidar DV na migração. |
| **MYS-033** | 🔴 **Arredondamento divergente** — BATCHREL arredonda (+0.005) ao totalizar; cálculo trunca. Totais do relatório ≠ soma dos pagamentos. | `BATCHREL.NSN#L143-L147` | `needs-investigation` | **High** | Definir política única round/truncate. |
| **MYS-034** | **Status 'C' (cancelado) órfão na escrita** — relatórios contam 'C', mas nenhum programa grava 'C'. | `BATCHREL.NSN`, `RELPGT.NSN` | `needs-investigation` | **High** | Achar quem cancela pagamento (programa ausente?). |
| **MYS-035** | **Tipo 'T' (TERCEIRO) órfão** — RELPGT traduz 'T', mas nenhum cálculo grava 'T'. | `RELPGT.NSN#L137`; `PAGAMENTO.ddm` | `needs-investigation` | **High** | Investigar o que é pagamento "terceiro". |
| **MYS-036** | **VALBENEF/VALDOCS órfãos** — validadores nunca chamados (sem CALLNAT). Validação inline divergente em CADBENEF. | [dependency-map.md](dependency-map.md) | `needs-investigation` | **High** | Confirmar se algum MAP/menu os aciona; senão código morto. |
| **MYS-037** | **Validação de CPF triplicada e divergente** (CADBENEF/VALBENEF/VALDOCS) — regras diferentes por ponto de entrada. | `CADBENEF/VALBENEF/VALDOCS` | `needs-investigation` | **High** | Unificar numa regra única na migração. |
| **MYS-S1b** | **Cadastro não valida região/programa** — CADBENEF grava COD-REGIAO/COD-PROGRAMA sem checar existência (alimenta o bypass 99). | `CADBENEF.NSN` | `needs-investigation` | **High** | Adicionar validação de FK na migração. |
| **MYS-012** | **Elegibilidade detalhada por programa** — varia entre programas; exige entrevistas com gestores. | `REGRAS-NEGOCIO-2012.md` RN-015; `VALELEG.NSN` | `needs-facilitator` | **Medium** | Perguntar a gestores/SENARC. |
| **MYS-013d** | **CadÚnico** — cruzamento citado; programa não consta no legado. | `REGRAS-NEGOCIO-2012.md` RN-016 | `needs-facilitator` | **Medium** | Confirmar se fonte existe fora do repo. |
| **MYS-038** | **Status >75 anos vira 'S' (suspenso)** silenciosamente — contra-intuitivo para benefício. | `CADBENEF.NSN#L165-L167` | `needs-facilitator` | **Medium** | Validar regra com gestor (revisão obrigatória vs bug). |
| **MYS-039** | **Lógica de dependentes invertida (tipo 'A')** — renda alta COM dependentes passa. | `VALELEG.NSN#L167-L181` | `needs-facilitator` | **Medium** | Confirmar com gestor do programa assistencial. |
| **MYS-040** | **Constante 0.347215** sem origem documentada (coração do Fator-K). | `CADPROG.NSN#L93` | `needs-facilitator` | **Medium** | Investigar origem (índice econômico 2003?). |
| **MYS-041** | **Pensão 'P' sujeita ao teto** (diferente do judicial) — juridicamente costuma ter prioridade. | `CALCDSCT.NSN` | `needs-facilitator` | **Medium** | Validar com jurídico se 'P' deveria ser isenta. |
| **MYS-042** | **Sindical 1% hardcoded** ignora PCT do cadastro. | `CALCDSCT.NSN#L154-L157` | `parked` | **Low** | Documentar; parametrizar na migração. |
| **MYS-043** | **COD-ELEGIBILIDADE posições 3-5** não usadas (só 1='R', 2='D'). | `VALELEG.NSN` (VERIF-ELEG-ESPECIFICA) | `parked` | **Low** | Investigar valores reais no DDM/dados. |
| **MYS-044** | **Idade calculada só por ano** (ignora mês/dia) — bordas de faixa etária. | `CALCBENF.NSN`, `VALELEG.NSN` | `parked` | **Low** | Decidir regra de idade exata na migração. |
| **MYS-045** | **Fevereiro fixo em 29 dias** — aceita 29/02 em ano não bissexto. | `VALBENEF.NSN#L97` | `parked` | **Low** | Corrigir validação de data. |
| **MYS-046** | **Código morto** — Banco Real (BATCHCON), Plano Verão (CALCCORR), `#LOG-WORK` (BATCHPGT), MAP ausente (CONSBENF). | vários | `parked` | **Low** | Descartar na migração; confirmar que nada chega. |
| **MYS-047** | **Máscara de CPF inconsistente** entre programas (RELPGT expõe mais que CONSBENF). | `RELPGT.NSN` vs `CONSBENF.NSN` | `parked` | **Low** | Unificar máscara. |
| **MYS-048** | **CNAB 240 hardcoded só para BB**; outros bancos? | `BATCHCON.NSN` | `parked` | **Low** | Confirmar bancos pagadores ativos. |
| **MYS-016** | **Desvinculação de dependentes** não documentada. | `REGRAS-NEGOCIO-2012.md` §6 | `parked` | **Low** | Revisitar se CADDEPEND revelar lógica. |

### Discrepâncias de schema (DDM × código) — entram como riscos de migração

- **FNRs divergentes**: comentários citam 150/155/160/170; reais são **150/151/152/153** (DBID 57). → confiar nos DDMs (relacionado a MYS-024).
- **Dois Fatores-K** (MYS-026): campo persistido `PROGRAMA-SOCIAL.FATOR-K` (N5.4) vs `#FATOR-K` calculado em CADPROG (N5.6). Investigar qual é usado.
- **Tipos de desconto** código (C/I/J/S/P/A) × DDM (IR/JD/CS/PA/EM/TX/OU/EX) — tabela de-para necessária.
- **VIEWs reduzidas**: DDMs têm 34-52 campos; programas usam ~10-15 (biometria, email, SIAFI, hash nunca tocados).

### Observações para o Estágio 2

- **Os 4 bloqueadores críticos originais estão resolvidos** (MYS-001/002/003/004). Os novos Critical (MYS-S1, 021-025) são **financeiros e de segurança** descobertos no código — devem ser decididos antes das EARS.
- **Concentração de risco no PAGAMENTO (FNR 152)**: status ambíguo, dupla mutação, dados bancários, ~180 mi registros sem purge.
- **4 backdoors** (região 99, CPF 000, prefixos VALDOCS, encobrimento de exclusões) exigem decisão explícita de segurança/compliance.
- **Validação e cálculo duplicados** (CPF em 3 lugares; cálculo em CALCBENF+BATCHPGT) — oportunidade de unificação na arquitetura-alvo.

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="mysteries-checklist.md"><strong>mysteries-checklist.md</strong></a><br/>
<sub>Lista do que procurar.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="discovery-report.md"><strong>discovery-report.md</strong></a><br/>
<sub>Síntese final.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>


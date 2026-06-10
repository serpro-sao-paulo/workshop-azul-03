<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Relatório de Descoberta — Estágio 1: Arqueologia Digital

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **discovery-report**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Este documento consolida todas as descobertas do Estágio 1.
> Preencha cada seção com as conclusões do time. **Este é o input principal do Estágio 2** — sem ele, a especificação vira chute.

**Time**: [NOME DA EQUIPE]
**Data**: 10/06/2026
**Edição**: Estágio 1 — passada completa (15/15 programas + 4/4 DDMs)
**Participantes**: [Liste os membros e suas personas]

> Gerado por `/discovery-report`. Sintetiza [inventory.md](inventory.md), [business-rules-catalog.md](business-rules-catalog.md), [dependency-map.md](dependency-map.md), [mysteries-found.md](mysteries-found.md) e [glossary.md](glossary.md). **Documento de passagem para o Estágio 2.**

---

## 1. Sumário Executivo

O SIFAP é um sistema Natural/Adabas de ~29 anos (1997) que processa a folha mensal de benefícios sociais: **15 programas `.NSN`** sobre **4 arquivos Adabas** (DBID 57, FNR 150-153), com ~133 regras de negócio catalogadas (~80 confirmadas no código). A arquitetura real é de **programas independentes acoplados apenas pelos dados** — **não há um único `CALLNAT`/`INCLUDE`**; lógicas críticas (cálculo de benefício) estão **duplicadas inline** entre `CALCBENF` e `BATCHPGT`. O maior risco de entrada no Estágio 2 é a **máquina de status do PAGAMENTO em conflito entre DDM e código** ([MYS-024](mysteries-found.md)) sobre ~180 milhões de registros, somada a **4 backdoors de segurança** plantados (região 99, CPF 000, prefixos VALDOCS, encobrimento de exclusões na auditoria). Os 4 bloqueadores históricos foram **resolvidos pela leitura do código** (Fator-K, 13º, exceção judicial, região 99), mas surgiram 6 novos bloqueadores financeiros/segurança. **Confiança para modernização: MÉDIA** — domínio bem mapeado, porém com decisões críticas de negócio/segurança pendentes de validação humana.

---

## 2. Visão Geral do Sistema

### 2.1 Propósito do SIFAP

Sistema de Fiscalização e Administração de Pagamentos de **benefícios sociais**. Fluxo central: cadastra beneficiários e programas → valida elegibilidade → calcula o benefício mensal (com fatores regional/familiar/renda/idade, 13º e abono) → gera a folha em lote → remete ao banco (CNAB 240) → concilia o retorno bancário → audita e gera relatórios.

### 2.2 Arquitetura Legada

- **15 programas Natural** agrupados em: Cadastro (`CADBENEF`, `CADDEPEND`, `CADPROG`), Consulta (`CONSBENF`), Validação (`VALBENEF`, `VALDOCS`, `VALELEG`), Cálculo (`CALCBENF`, `CALCDSCT`, `CALCCORR`), Batch (`BATCHPGT`, `BATCHCON`, `BATCHREL`), Relatórios (`RELPGT`, `RELAUDIT`).
- **4 DDMs Adabas** (DBID 57): `BENEFICIARIO` (FNR 150, ~4,2 mi reg.), `PROGRAMA-SOCIAL` (FNR 151, ~45 reg.), `PAGAMENTO` (FNR 152, **~180 mi reg.**), `AUDITORIA` (FNR 153, ~25 mi reg., imutável).
- **Acoplamento:** 0 arestas programa→programa; **34 arestas programa→dados**. O `PAGAMENTO` é o hub (8 programas o tocam). Detalhe em [dependency-map.md](dependency-map.md).
- **Fluxo de vida do pagamento:** `CALCBENF/BATCHPGT (STORE 'G')` → `CALCDSCT (desconto)` → `BATCHCON ('P'/'D'/'E' + auditoria)` → `CALCCORR (correção)`.

### 2.3 Usuários e Perfis

Programas online via terminal 3270 (cadastro/consulta) operados por usuários internos; batch agendado por JCL (1º dia útil do mês). O DDM `AUDITORIA` registra perfis ADM/OPR/CON/AUD/SUP. Órgãos gestores das regras: SENARC/CGPB/MDS. Autorização nível 2 (perfil SUPERVISOR) citada para alteração de CPF (RN-009).

---

## 3. Principais Descobertas

### 3.1 Regras de Negócio Críticas

> As 5 mais importantes (confirmadas no código). Catálogo completo em [business-rules-catalog.md](business-rules-catalog.md).

1. **Cálculo do benefício mensal** = `VLR-BASE × fator_regional × fator_familiar × fator_renda × fator_idade × (1+reajuste)`, truncado a 2 casas (`CALCBENF.NSN#L224-L232`).
2. **Teto de descontos = 30% do bruto, exceto judicial (tipo 'J')** que é isento (`CALCDSCT.NSN#L190-L195`).
3. **13º + abono natalino em dezembro:** tipo 'D', 13º = base×reg×idade; abono 15% para programa tipo 'A' (`CALCBENF.NSN#L240-L257`).
4. **Elegibilidade por tipo de programa:** A (renda≤600/dependentes+docs), P (idade≥60), T (idade 16-65); **região 99 = elegível incondicional** (`VALELEG.NSN`).
5. **Validação de CPF por Módulo 11** com exceção de teste para CPF iniciado em '000' (`CADBENEF`/`VALBENEF`).

### 3.2 Dependências Complexas

- **Sem acoplamento de chamadas** — o risco não é cascata de chamadas, e sim **lógica duplicada divergente**: `BATCHPGT` reimplementa `CALCBENF` (tabelas copiadas) e aplica descontos diferentes de `CALCDSCT` ([MYS-022](mysteries-found.md), [MYS-023](mysteries-found.md)).
- **Hub de risco = `PAGAMENTO` (FNR 152):** mutado por 4 programas em momentos diferentes, sem orquestração, ~180 mi registros sem purge.
- **Órfãos:** `VALBENEF` e `VALDOCS` nunca são chamados ([MYS-036](mysteries-found.md)).

### 3.3 Dívida Técnica Identificada

- [ ] **Lógica de cálculo duplicada** (CALCBENF × BATCHPGT) e **validação de CPF triplicada** (CADBENEF/VALBENEF/VALDOCS) com regras divergentes.
- [ ] **Máquina de status do PAGAMENTO ambígua** — DDM e código discordam; status 'E' (erro vs estornado) e 'C' (órfão) sem fonte de escrita.
- [ ] **Magic numbers e tabelas hardcoded** — fatores regionais (27), faixas de renda, `0.347215` (Fator-K), IPCA congelado em 2010-2012, CNAB só do BB.
- [ ] **Código morto** — Banco Real, Plano Verão, `#LOG-WORK`, MAP ausente.
- [ ] **Qualidade de dados** — CPFs com tamanho variável (zeros à esquerda); fevereiro fixo em 29 dias.

### 3.4 Gaps de Documentação

A documentação de 2012 cobria ~25% das regras e estava **errada/desatualizada** em pontos-chave: máx. dependentes (dizia 3, é 5), ordenação do batch (dizia por nome, é por CPF), dados bancários (dizia no cadastro, estão no PAGAMENTO), Fator-K e 13º ("não documentados"). Os campos bancários, SIAFI, biometria e contato existem no DDM mas **nenhum programa os usa**. A verdade está no código `.NSN`, não nos docs.

---

## 4. Mistérios e Riscos

### 4.1 Mistérios Não Resolvidos

> Bloqueadores do Estágio 2 (Critical). Catálogo completo (45 mistérios) em [mysteries-found.md](mysteries-found.md).

| ID  | Descrição | Risco para Migração |
| --- | --------- | ------------------- |
| MYS-024 | Status do PAGAMENTO: DDM × código em conflito total (P/G/E/C/D/X/R) | Interpretar errado ~180 mi registros |
| MYS-025 | Colisão do status 'E' (erro vs estornado) | Conciliação/relatório incorretos |
| MYS-021 | Possível dupla aplicação de reajuste (Fator-K + reajuste mensal) | Valor do benefício errado, sistêmico |
| MYS-022 | Descontos batch (3%) ≠ descontos completos (CALCDSCT) | Pagamento sem descontos legais |
| MYS-023 | Lógica de cálculo duplicada (CALCBENF × BATCHPGT) | Divergência online × lote |
| MYS-S1 | 4 backdoors de segurança (região 99, CPF 000, prefixos VALDOCS, exclusões ocultas) | Fraude / LGPD / compliance (TCU) |

### 4.2 Riscos para o Estágio 2

1. **Não escrever EARS de cálculo/desconto** sem antes decidir: unificar CALCBENF×BATCHPGT, resolver a dupla aplicação do reajuste e a máquina de status do PAGAMENTO.
2. **Decisão de segurança/compliance obrigatória** sobre os 4 backdoors antes de qualquer spec — envolver jurídico/segurança/auditoria.
3. **Validar com gestores (SENARC/CGPB)** os itens `needs-facilitator`: origem do `0.347215`, status >75 anos, pensão 'P' vs teto, elegibilidade por programa, CadÚnico.

---

## 5. Recomendações

### 5.1 O que migrar primeiro

> Hipótese inicial (a confirmar com o Product Owner no Estágio 2).

| Prioridade | Funcionalidade | Justificativa |
| ---------- | -------------- | ------------- |
| 1 | Cadastro de beneficiário + validação (CADBENEF/VALBENEF/VALDOCS) | Base de tudo; unificar a validação de CPF triplicada. |
| 2 | Cálculo de benefício + descontos (CALCBENF/CALCDSCT) | Coração financeiro; resolver duplicação e dupla aplicação de reajuste. |
| 3 | Folha mensal em lote (BATCHPGT) | Fluxo crítico de produção; depende de 1 e 2 unificados. |

### 5.2 O que descartar

- **Código morto** (Banco Real em BATCHCON, Plano Verão em CALCCORR, `#LOG-WORK`): descontinuado, descartar.
- **Backdoors de teste** (CPF 000, prefixos VALDOCS): não reimplementar sem decisão explícita.
- **Campos de DDM nunca usados** (biometria, hash, alguns SIAFI): avaliar descarte na modelagem-alvo.

### 5.3 O que evoluir

> Funcionalidades que devem ser migradas E melhoradas:

- **Máquina de status do PAGAMENTO:** redesenhar com estados claros e única fonte de verdade.
- **Auditoria:** remover o encobrimento de exclusões (compliance TCU/IN-63).
- **Mascaramento de CPF:** corrigir o bug LGPD e unificar a máscara entre telas/relatórios.
- **Conciliação CNAB:** generalizar para múltiplos bancos (hoje só BB).
- **Tabelas hardcoded:** externalizar fatores/índices (IPCA, regional, faixas) como parâmetros.

- [Funcionalidade]: [Como melhorar]

---

## 6. Métricas do Estágio

| Métrica                       | Valor        |
| ----------------------------- | ------------ |
| Programas analisados          | 15 / 15  |
| DDMs mapeados                 | 4 / 4   |
| Regras de negócio encontradas | ~133 (~80 confirmadas) |
| Regras escondidas encontradas | 10 / 10  |
| Easter eggs encontrados       | 3 / 3 (+1 bônus)  |
| Termos no glossário           | 50       |
| Mistérios catalogados         | 45 (13 resolvidos, 32 em aberto) |
| Tempo total gasto             | \_\_\_ horas |

---

## 6.1 Hipóteses de Recorte (Bounded Contexts) — para o `@architect-agent`

> ⚠️ **São HIPÓTESES, não decisões.** Baseadas nos clusters de acesso a dados (não há clusters de chamada, pois não há CALLNAT). O architect avalia e decide no Estágio 2.

- **Hipótese 1 — Cadastro de Beneficiários** (`CADBENEF`, `CADDEPEND`, `VALBENEF`, `VALDOCS`, `CONSBENF`; DDM `BENEFICIARIO`): fronteira natural em torno do ciclo de vida cadastral e suas validações.
- **Hipótese 2 — Catálogo de Programas Sociais** (`CADPROG`; DDM `PROGRAMA-SOCIAL`): dados paramétricos estáveis (~45 registros), incluindo regras de elegibilidade e Fator-K.
- **Hipótese 3 — Elegibilidade** (`VALELEG`; lê `BENEFICIARIO`+`PROGRAMA-SOCIAL`): decisão de quem pode receber — candidato a serviço de domínio próprio (contém o backdoor região 99).
- **Hipótese 4 — Cálculo & Folha de Pagamento** (`CALCBENF`, `CALCDSCT`, `CALCCORR`, `BATCHPGT`; DDM `PAGAMENTO`): núcleo financeiro; aqui mora a duplicação de lógica a unificar.
- **Hipótese 5 — Conciliação, Auditoria & Relatórios** (`BATCHCON`, `BATCHREL`, `RELPGT`, `RELAUDIT`; DDMs `PAGAMENTO`+`AUDITORIA`): integração bancária, trilha de auditoria e saídas.

> Tensão conhecida: o `PAGAMENTO` é compartilhado pelas Hipóteses 4 e 5 — o architect precisará decidir a propriedade do dado (provavelmente Hipótese 4 é dona; 5 consome).

---

## 7. Notas para o Próximo Estágio

> Mensagens para o time no Estágio 2 (Especificação Moderna):

1. **Comece pelas decisões, não pelas EARS.** Os 6 bloqueadores Critical ([seção 4.1](#41-mistérios-não-resolvidos)) precisam de definição antes de especificar cálculo, status e segurança.
2. **Toda EARS deve carregar `source_legacy:`** apontando para o `business-rules-catalog.md` (formato `ARQUIVO.NSN#L<i>-L<f>`). As regras já estão rastreadas — reutilize.
3. **Use os FNRs corretos (150-153)**, não os números dos comentários de código (150/155/160/170), que estão errados.
4. **Backdoors = decisão de produto + segurança**, não bug a copiar. Documente a decisão (preservar/restringir/auditar) com justificativa.
5. **Itens `needs-facilitator`** exigem entrevista com gestores (SENARC/CGPB) — agende cedo.
6. **O glossário ([glossary.md](glossary.md), 50 termos)** é o vocabulário compartilhado — use os mesmos nomes na spec para evitar desencontro.

---

## Definição de Pronto deste relatório

- [x] Todas as seções acima preenchidas (sem placeholders, exceto nomes da equipe/tempo).
- [x] Pelo menos 5 regras críticas listadas em §3.1, cada uma referenciando o código-fonte.
- [x] Decisões de migrar/descartar/evoluir em §5.
- [x] Métricas de §6 conferem com os outros artefatos (glossary.md, business-rules-catalog.md, mysteries-found.md).

## Aprovação da Equipe

> Reviewed by: __________________  ·  Date: __________  ·  Confidence: [ ] high  [x] medium  [ ] low

— Paula


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="mysteries-found.md"><strong>mysteries-found.md</strong></a><br/>
<sub>Lista de mistérios.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../02-spec-moderna/GUIDE.md"><strong>Estágio 2 — Spec</strong></a><br/>
<sub>Próximo estágio: spec moderna.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>


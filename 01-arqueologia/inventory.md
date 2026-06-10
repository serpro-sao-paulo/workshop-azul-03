# Inventário Legado — [NOME DA EQUIPE]

> **Primeira passada (top-down)** — gerado em 2026-06-10 a partir de **nomes de arquivos e estrutura de pastas apenas**. Nenhum programa foi aberto ou lido.
> Este inventário será **revisado** conforme a equipe ler arquivos individuais e rastrear dependências reais (CALLNAT/FETCH).

Path de legado: `01-arqueologia/legado-sifap/`

---

## Estrutura de Pastas

```
legado-sifap/
├── adabas-ddms/        (Data Definition Modules)
├── legacy-docs/        (documentação histórica)
└── natural-programs/   (programas-fonte Natural)
```

- **Total de diretórios:** 4 (raiz + 3 subpastas)
- **Profundidade máxima:** 1 nível — estrutura plana, sem aninhamento profundo
- **Total de arquivos:** 30

---

## Contagem de Arquivos por Tipo

| Extensão | Contagem | Finalidade provável |
| -------- | -------- | ------------------- |
| `.NSN`   | 15 | Programa-fonte Natural |
| `.md`    | 8  | Documentação / READMEs (Markdown) |
| `.ddm`   | 4  | Data Definition Module (mapeamento de arquivo Adabas) |
| `.docx`  | 3  | Documentação histórica (Word) — pareada com versões `.md` |

> Observação: cada `.docx` em `legacy-docs/` possui um par `.md` de mesmo nome. Conteúdo a confirmar quando a documentação for lida.

---

## Padrões de Convenção de Nomes

Agrupamento por prefixo dos 15 programas `.NSN` (somente nomes — **hipóteses não verificadas**):

| Prefixo | Contagem | Arquivos | Hipótese |
| ------- | -------- | -------- | -------- |
| `BATCH` | 3 | BATCHCON, BATCHPGT, BATCHREL | Entry points batch (processamento em lote) — prováveis pontos de partida do fluxo |
| `CAD`   | 3 | CADBENEF, CADDEPEND, CADPROG | Programas de cadastro/manutenção (CRUD de entidades) |
| `CALC`  | 3 | CALCBENF, CALCCORR, CALCDSCT | Rotinas de cálculo (benefício, correção, desconto) — prováveis subprogramas CALLNAT |
| `VAL`   | 3 | VALBENEF, VALDOCS, VALELEG | Rotinas de validação (regras de elegibilidade/documentos) — prováveis subprogramas CALLNAT |
| `REL`   | 2 | RELAUDIT, RELPGT | Geração de relatórios |
| `CONS`  | 1 | CONSBENF | Consulta — `Padrão de ocorrência única, investigar no próximo passo` |

DDMs (4) seguem nomes de entidade de domínio: `AUDITORIA`, `BENEFICIARIO`, `PAGAMENTO`, `PROGRAMA-SOCIAL`.

> ⚠️ Hipóteses baseadas apenas em convenções genéricas Natural/Adabas. Confirme abrindo os programas em `/extract-business-rules` e `/map-dependencies`.

---

## Itens Incomuns (Top 3)

1. **`natural-programs/BATCHPGT.NSN` — maior programa (10.866 bytes)**
   - Incomum por: maior arquivo `.NSN` da pasta, ~30% maior que o segundo colocado.
   - Investigação sugerida: ler primeiro entre os batch — provavelmente o fluxo de pagamento mais complexo, com mais CALLNATs.

2. **`natural-programs/CONSBENF.NSN` — prefixo único `CONS`**
   - Incomum por: único arquivo com esse padrão de prefixo (todos os outros têm 2+ irmãos).
   - Investigação sugerida: verificar se é um programa de consulta online isolado ou se há programas `CONS*` faltando/perdidos no legado.

3. **`legacy-docs/*.docx` — único formato binário do repositório (3 arquivos)**
   - Incomum por: única extensão não-texto; cada `.docx` é duplicado por um `.md` de mesmo nome (ARQUITETURA-ORIGINAL-1997, MANUAL-TECNICO-SIFAP-2008, REGRAS-NEGOCIO-2012).
   - Investigação sugerida: confirmar se `.md` é uma conversão fiel do `.docx` ou se o `.docx` contém diagramas/conteúdo ausente no Markdown.

---

## Ordem de Leitura Proposta

> **Hipótese de leitura** — a ordem real mudará quando a equipe começar a rastrear dependências (CALLNAT/FETCH).

1. **Dados primeiro — DDMs (`adabas-ddms/`):** entender o modelo de dados antes do código.
   1. `PROGRAMA-SOCIAL.ddm`
   2. `BENEFICIARIO.ddm`
   3. `PAGAMENTO.ddm`
   4. `AUDITORIA.ddm`
2. **Entry points batch (`BATCH*`):** pontos de partida do fluxo de processamento.
   1. `BATCHPGT.NSN` (maior — fluxo de pagamento)
   2. `BATCHCON.NSN`
   3. `BATCHREL.NSN`
3. **Programas mais conectados (hipótese):** os subprogramas de cálculo (`CALC*`) e validação (`VAL*`) são prováveis alvos de CALLNAT a partir dos batch — confirmar ao mapear dependências.
4. **Cadastro e consulta:** `CAD*`, `CONSBENF` — fluxos de manutenção/consulta.
5. **Relatórios:** `REL*` — saídas do sistema.

> Justificativa: (a) DDMs antes do código para fixar o vocabulário de dados; (b) `BATCH*` como entry points por convenção de prefixo; (c) `CALC*`/`VAL*` por serem prováveis nós mais conectados (nomes de domínio que tendem a ser chamados por múltiplos fluxos). Tudo a ser validado em `/map-dependencies`.

---

## Próximos Passos

- Esta é a **primeira passada**. Atualize este inventário conforme arquivos forem lidos.
- Para extrair regras de negócio de um programa: `/extract-business-rules`
- Para rastrear relacionamentos CALLNAT/FETCH: `/map-dependencies`

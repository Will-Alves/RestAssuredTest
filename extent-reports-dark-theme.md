# Tema Dark — ExtentReports 5 (Spark)

Use este prompt ao ajustar o visual dos relatórios HTML gerados pelo ExtentReports 5.

---

## Paleta de cores

| Token          | Hex        | Uso                                |
|----------------|------------|------------------------------------|
| bg principal   | `#0d1117`  | fundo da página e content area     |
| bg sidebar     | `#0b0f16`  | sidebar e navbar superior          |
| bg card        | `#161b22`  | cards de teste                     |
| borda          | `#30363d`  | divisores, bordas                  |
| borda sutil    | `#21262d`  | separadores de linha de log        |
| ciano          | `#39d0c4`  | accent — logo, item ativo, `pre`   |
| verde          | `#3fb950`  | PASS                               |
| vermelho       | `#f85149`  | FAIL                               |
| azul           | `#58a6ff`  | INFO, links                        |
| amarelo        | `#d29922`  | WARN                               |
| texto principal| `#e6edf3`  | títulos, nomes de teste            |
| texto conteúdo | `#c9d1d9`  | corpo das linhas de log            |
| texto mudo     | `#6e7681`  | timestamps, metadados              |

---

## Arquivos de tema

- `src/test/resources/report/nav.css` — injetado via `spark.config().setCss(...)`
- `src/test/resources/report/branding.js` — injetado via `spark.config().setJs(...)`

O CSS e o JS são lidos durante a execução dos testes e injetados **inline** no HTML gerado. O relatório é autocontido — funciona ao abrir direto pelo caminho de arquivo, sem servidor.

---

## Seletores reais do HTML gerado (ExtentReports 5 Spark)

> **Atenção:** o `<body>` tem `class="spa -report dark"` — as classes reais são `spa`, `-report` e `dark`. Usar `body.spa-report` não funciona. Usar `body.dark` ou `.dark` funciona.

> **Estrutura da sidebar (invertida):** `<ul class="test-list-item">` contém `<li class="test-item">`. O atributo `status="pass"` fica no `<li>`, não no `<ul>`. Seletores no `<ul>` não enxergam o `status`.

| Elemento                        | Seletor real                                        |
|---------------------------------|-----------------------------------------------------|
| Corpo da página                 | `body.dark`                                         |
| Content area principal          | `.main-content`                                     |
| Sidebar                         | `.side-nav`, `.test-list-wrapper`                   |
| Navbar superior                 | `.header.navbar`                                    |
| Header da lista (label Testes)  | `.test-list-tools`                                  |
| Container de itens              | `<ul class="test-list-item">`                       |
| Item na sidebar                 | `.test-list-item .test-item` (o `<li>`)             |
| Item ativo                      | `.test-list-item .test-item.active`                 |
| Item por status                 | `.test-list-item .test-item[status="pass"]`         |
| Nome do teste na sidebar        | `.test-detail .name`                                |
| Hora/duração na sidebar         | `.test-detail .text-sm`                             |
| Badge PASS/FAIL na sidebar      | `.test-detail .text-sm .badge` (inline, antes do timestamp após Java patch) |
| Cabeçalho do detalhe            | `.detail-head`                                      |
| Título do teste no detalhe      | `h5.test-status`                                    |
| Linhas de log                   | `.event-row > td` (não `.ivent-row`)                |
| Coluna de timestamp             | `.timestamp-col`                                    |
| Blocos de código                | `pre`                                               |

---

## Pós-processamento Java (patchHtml)

Alguns elementos do Spark não podem ser sobrescritos via CSS por conta de `!important` ou renderização Angular. A classe `ExtentReportExtension.java` processa o HTML gerado com `html.replace()` / `html.replaceAll()` antes de salvar o arquivo final.

Patches ativos:
- Remove `<link rel="apple-touch-icon">` e `<link rel="shortcut icon">`
- Substitui `<span class="font-size-14">Tests</span>` → `<span style="font-size:17px!important;font-weight:600">Testes ClassName</span>`
- Reordena badge + timestamp na sidebar: move o badge (`pass-bg`/`fail-bg`) para ANTES do timestamp, retirando o `float-right`

```java
// Label "Testes ClassName" na sidebar
html = html.replace(
    "<span class=\"font-size-14\">Tests</span>",
    "<span style=\"font-size:17px!important;font-weight:600\">Testes " + className + "</span>");

// Badge antes do timestamp (sem float)
html = html.replaceAll(
    "(<span>\\d{2}:\\d{2}:\\d{2}</span> / <span>[^<]+</span>)\\s*(<span class=\"badge (?:pass|fail)-bg log) float-right\">([^<]+)</span>",
    "$2\">$3</span> $1");
```

---

## Padrões de estilo aplicados

### Tipografia
- Interface geral: `'Segoe UI', -apple-system, sans-serif`
- Blocos `pre`: `Consolas, 'Courier New', monospace`

### Textura de fundo
```css
.main-content {
  background-color: #0d1117 !important;
  background-image: radial-gradient(circle, rgba(255,255,255,0.025) 1px, transparent 1px) !important;
  background-size: 28px 28px !important;
}
```

### Sidebar — itens
```css
/* Separator entre itens */
.test-list-item .test-item {
  border-bottom: 1px solid #21262d !important;
}
/* Item ativo (classe adicionada pelo Angular ao clicar) */
.test-list-item .test-item.active {
  background-color: rgba(57,208,196,0.06) !important;
}
.test-list-item .test-item:hover {
  background-color: rgba(255,255,255,0.03) !important;
}
/* Sem border-left no item — causa retângulo visual indesejado */
```

### Título do teste (painel de detalhe)
```css
h5.test-status {
  font-size: 19px !important;
  font-weight: 600 !important;
  color: #e6edf3 !important;
}
h5.test-status.text-pass,
h5.test-status.text-fail {
  color: #e6edf3 !important;
}
```

### Badges de status no log
```css
.pass-bg { background: rgba(63,185,80,0.15);  color: #3fb950; border: 1px solid rgba(63,185,80,0.3);  }
.fail-bg  { background: rgba(248,81,73,0.15);  color: #f85149; border: 1px solid rgba(248,81,73,0.3);  }
.info-bg  { background: rgba(88,166,255,0.15); color: #58a6ff; border: 1px solid rgba(88,166,255,0.3); }
.warn-bg  { background: rgba(210,153,34,0.15); color: #d29922; border: 1px solid rgba(210,153,34,0.3); }
```

### Ícone (branding.js)
- Canvas `64×64`: fundo `#0d1117`, borda `#39d0c4`, texto `RA` em `#39d0c4`
- Logo no nav: `fa-flask` com `color: #39d0c4`

---

## O que NÃO fazer

- Não usar `body.spa-report.dark` — o body tem `class="spa -report dark"` com espaço, o seletor combinado não bate
- Não usar `.ivent-row` — a classe real é `.event-row`
- Não usar `.test-head h4` — o título real é `h5.test-status` dentro de `.detail-head`
- Não usar `.test-list-item .test-name` — o nome real é `.test-detail .name`
- Não aplicar `font-family: monospace` em `*` globalmente — ícones Font Awesome quebram
- Não colorir `h5.test-status` com a cor de status (verde/vermelho) — o título deve ser sempre branco `#e6edf3`
- Não usar `.test-list-item[status="pass"]` para colorir por status — `status` está no `<li class="test-item">`, não no `<ul class="test-list-item">`. Usar `.test-list-item .test-item[status="pass"]`
- Não adicionar `border-left` em `.test-list-item .test-item.active` — cria um retângulo visual alto indesejado na sidebar
- Para esconder um nó de texto (`&middot;`, texto livre) use `font-size: 0` no container e restaure nos filhos — CSS não seleciona nós de texto diretamente
- Sobrescrever `.float-right` via CSS não funciona quando o elemento tem a classe hardcoded no HTML — usar Java `patchHtml` com `replaceAll` para reordenar diretamente no HTML

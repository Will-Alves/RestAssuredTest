# Tema Dark — ExtentReports 5 (Spark)

Use este prompt ao ajustar o visual dos relatórios HTML gerados pelo ExtentReports 5.

---

## Paleta de cores (mesma da documentação)

| Variável       | Hex       | Uso                              |
|----------------|-----------|----------------------------------|
| bg principal   | `#0d1117` | fundo da página e content area   |
| bg sidebar     | `#080d12` | sidebar e navbar superior        |
| bg card        | `#161b22` | cards de teste                   |
| borda          | `#30363d` | divisores, bordas                |
| ciano          | `#39d0c4` | accent — logo, item ativo, `pre` |
| verde          | `#3fb950` | PASS                             |
| vermelho       | `#f85149` | FAIL                             |
| azul           | `#58a6ff` | INFO, links                      |
| amarelo        | `#d29922` | WARN                             |
| texto principal| `#e6edf3` | títulos, nomes de teste          |
| texto secundário| `#8b949e`| timestamps, metadados            |

---

## Arquivos de tema

- `src/test/resources/report/nav.css` — injetado via `spark.config().setCss(...)`
- `src/test/resources/report/branding.js` — injetado via `spark.config().setJs(...)`

Carregados em `ExtentReportExtension.java`:
```java
spark.config().setCss(Files.readString(Path.of(cssPath)));
spark.config().setJs(Files.readString(Path.of(jsPath)));
```

---

## Regras de estilo

### Tipografia
- Interface (sidebar, badges, cabeçalhos): `'Segoe UI', -apple-system, sans-serif`
- Nomes de teste e linhas de log: `Consolas, 'JetBrains Mono', monospace`
- Blocos `pre`: `Consolas, monospace`, borda esquerda ciano `#39d0c4`

### Títulos
- Título do teste no painel de detalhe: `18px bold #e6edf3`, borda inferior `#30363d`
- Nome na navbar: `15px bold #39d0c4`
- Cabeçalho "Tests" da sidebar: uppercase, `#39d0c4`, `1px #30363d` bottom

### Item na sidebar
- Texto do nome: `#e6edf3` (branco — NÃO verde, mesmo quando PASS)
- Status via borda esquerda colorida: `3px solid #3fb950` (PASS) ou `#f85149` (FAIL)
- Fundo sutil: `rgba(63,185,80,0.06)` para PASS

### Badges
```css
.pass-bg { background: rgba(63,185,80,0.18); color: #3fb950; border: 1px solid rgba(63,185,80,0.35); }
.fail-bg  { background: rgba(248,81,73,0.18); color: #f85149; border: 1px solid rgba(248,81,73,0.35); }
.info-bg  { background: rgba(88,166,255,0.18); color: #58a6ff; border: 1px solid rgba(88,166,255,0.35); }
.warn-bg  { background: rgba(210,153,34,0.18); color: #d29922; border: 1px solid rgba(210,153,34,0.35); }
```

### Ícone (branding.js)
- Canvas `64×64`: fundo `#0d1117`, borda `#39d0c4`, texto `RA` em `#39d0c4`
- Logo no nav: `fa-flask` com `color: #39d0c4`

---

## O que NÃO fazer

- Não aplicar monospace em `*` globalmente — deixa a interface com cara de terminal genérico
- Não usar `color: #3fb950` no nome do teste na sidebar — usa borda esquerda como indicador, texto fica branco
- Não remover Font Awesome da regra de tipografia — ícones quebram se o `font-family` global for monospace
- Não alterar o fundo padrão do ExtentReports no Java — fazer tudo via CSS injetado

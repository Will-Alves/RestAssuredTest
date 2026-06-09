# Documentação — Testes Automatizados SCC

## Visão Geral

Os testes automatizados do SCC são escritos em Java com **REST Assured** e **JUnit 5**, organizados em dois arquivos principais:

- `SCC_Teste.java` — contém os métodos de teste (`@Test`)
- `Metodos_Rest.java` — biblioteca de métodos utilitários de validação reutilizáveis

A ideia central é que cada `@Test` descreva **o que** está sendo validado, enquanto `Metodos_Rest` cuida do **como** validar cada tipo de dado.

---

## Estrutura dos Testes

```
SCC_Teste.java
├── test_PA_ListarTAGsPossiveis
├── test_GerarSenha
├── test_GerarSenhaComPrioridade
└── test_ChamarProximo
```

---

## Metodos_Rest — Biblioteca de Validações

### Validações de tipo básico

| Método | O que valida |
|---|---|
| `validarBoolean(map, campo, esperado)` | Campo existe, é Boolean e tem o valor esperado |
| `validarString(map, campo)` | Campo existe, é String e não está vazio |
| `validarInteiro(map, campo)` | Campo existe e é Integer |
| `validarInteiroPositivo(map, campo)` | Tudo de `validarInteiro` + valor > 0 |

### Validações de resposta HTTP

| Método | O que valida |
|---|---|
| `validarContentType(response)` | Header `Content-Type` contém `application/json` |
| `validarTempoResposta(response)` | Tempo de resposta abaixo de 2000ms |

### Validações de senha

Senhas no SCC são retornadas como `String` pela API, mas precisam ser tratadas como valores numéricos.

| Método | Formato esperado | Exemplo |
|---|---|---|
| `validarSenhaSemPrioridade(map, campo)` | Apenas dígitos | `"4892"` |
| `validarSenhaComPrioridade(map, campo)` | 1 letra + dígitos | `"T4147"` |

Ambos validam: tipo String, padrão de dígitos, parte numérica > 0, comprimento entre 1 e 10 caracteres.

### Validações de data e hora

| Método | Formato | Validações extras |
|---|---|---|
| `validarEmissao(map, campo)` | `MM/dd/yyyy HH:mm:ss` | Data válida, é hoje, é recente (últimos 10 min) |
| `validarDataHoraBR(map, campo)` | `dd/MM/yyyy HH:mm:ss` | Apenas formato e data válida |

`validarEmissao` é usado em endpoints de **geração** de senha (retorno imediato).
`validarDataHoraBR` é usado em endpoints que podem retornar **chamadas antigas**, como `Chamarproximo`.

### Validações de estrutura

| Método | O que faz |
|---|---|
| `validarCamposExatos(map, camposEsperados)` | Falha o teste se o `result` tiver campos a mais ou a menos |
| `validarCamposItem(map, camposEsperados, label)` | Valida campos obrigatórios presentes; registra extras como aviso (`⚠`) sem falhar |
| `validarQuantidade(lista, quantidade, label)` | Valida que `lista.size()` confere com o campo `Quantidade` retornado pela API |
| `validarReferenciaExiste(lista, campoChave, valor, label)` | Valida que um valor existe em outra lista (ex: `GrupoId` de uma TAG existe nos Grupos) |

### Validações específicas

| Método | O que valida |
|---|---|
| `validarCorHex(map, campo)` | String no formato hexadecimal de 6 dígitos (ex: `"FF5500"`) |

---

## Testes

### test_GerarSenha

**Endpoint:** `POST /aspect/rest/senha/gerarsenha`
**Cenário:** Gerar senha sem prioridade (sem campo `Prioridade` no body).

```json
Body:    { "Fila": "5", "Totem": "0", "IdUnidade": "2" }
Retorno: { "result": { "sucesso": true, "senha": "4892", "emissao": "05/26/2026 10:32:06" } }
```

**Validações:**
- Content-Type e tempo de resposta
- `sucesso` é `true`
- `senha` é String com apenas dígitos, tratada como Integer > 0
- `emissao` no formato `MM/dd/yyyy HH:mm:ss`, é hoje e recente (últimos 10 min)
- Campos exatos: `sucesso`, `senha`, `emissao`

---

### test_GerarSenhaComPrioridade

**Endpoint:** `POST /aspect/rest/senha/gerarsenha`
**Cenário:** Gerar senha com prioridade (campo `Prioridade: 1` no body). A senha retorna com letra prefixada.

```json
Body:    { "Fila": 1, "Totem": 0, "IdUnidade": 2, "Prioridade": 1 }
Retorno: { "result": { "sucesso": true, "senha": "T4147", "emissao": "05/26/2026 09:17:24" } }
```

**Validações:**
- Content-Type e tempo de resposta
- `sucesso` é `true`
- `senha` inicia com exatamente 1 letra seguida de dígitos; parte numérica > 0
- `emissao` no formato `MM/dd/yyyy HH:mm:ss`, é hoje e recente (últimos 10 min)
- Campos exatos: `sucesso`, `senha`, `emissao`

**Diferença chave em relação ao teste sem prioridade:** o padrão de senha muda de `\d+` para `[A-Za-z]\d+`, e a parte numérica é extraída com `senha.substring(1)`.

---

### test_ChamarProximo

**Endpoint:** `GET /aspect/rest/senha/Chamarproximo/{fila}/{unidade}`
**Cenário:** Chamar a próxima senha de uma fila. Path params: `fila=1`, `unidade=2`.

```json
Retorno: {
  "result": {
    "Sucesso": true,
    "Senha": "1533",
    "DataHora": "27/02/2026 09:18:31",
    "IdFila": 2,
    "NomeFila": "Prioridade"
  }
}
```

**Validações:**
- Content-Type e tempo de resposta
- `Sucesso` é `true`
- `Senha` é String numérica, tratada como Integer > 0
- `DataHora` no formato `dd/MM/yyyy HH:mm:ss` e é uma data válida — **sem** checagem de "hoje/recente", pois pode retornar chamadas antigas
- `IdFila` é Integer > 0
- `NomeFila` é String não vazia
- Campos exatos: `Sucesso`, `Senha`, `DataHora`, `IdFila`, `NomeFila`

---

### test_PA_ListarTAGsPossiveis

**Endpoint:** `GET /aspect/rest/PA/ListarTAGsPossiveis/{idUsuario}/{idUnidade}`
**Cenário:** Listar grupos de TAGs e TAGs disponíveis para um usuário e unidade.

**Validações:**
- `result.GruposDeTags.Quantidade` confere com o tamanho real da lista
- `result.TAGs.Quantidade` confere com o tamanho real da lista
- Cada Grupo contém os campos `ID` (Integer) e `Nome` (String)
- Cada TAG contém os campos `ID` (Integer), `Nome` (String), `GrupoId` (Integer) e `Cor` (String hex)
- `Cor` de cada TAG está no formato hexadecimal de 6 dígitos
- `GrupoId` de cada TAG existe na lista de Grupos retornada

---

## Decisões de Projeto

**Por que `validarCamposExatos` em vez de só `validarCamposItem`?**
`validarCamposExatos` falha o teste quando um campo novo aparece no response, alertando imediatamente sobre mudanças na API. `validarCamposItem` é usado nas listagens (TAGs, Grupos) onde campos extras são registrados como aviso sem quebrar o teste.

**Por que `senha` é String e não Integer?**
A API retorna `senha` sempre como String JSON (com aspas). Converter para Integer garante que o valor é numérico e positivo, sem depender de um contrato de tipo que a API não oferece.

**Por que `validarEmissao` e `validarDataHoraBR` são métodos separados?**
Os formatos de data diferem entre os endpoints (`MM/dd/yyyy` vs `dd/MM/yyyy`) e as regras de negócio também diferem: `emissao` deve ser gerada agora (recente), enquanto `DataHora` de `Chamarproximo` pode ser de qualquer momento passado.

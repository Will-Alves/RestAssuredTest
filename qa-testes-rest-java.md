# Prompt — Padrões de QA Sênior para Testes REST Automatizados

Use este prompt como contexto ao trabalhar neste projeto com Claude.

---

## Contexto do Projeto

Projeto de testes automatizados de API REST em Java 17, usando JUnit 5, REST Assured 5 e ExtentReports 5.
As classes que vão ao GitHub são apenas: `SCC_Teste.java`, `SicsDynaMetrica.java`, `SuiteInterno.java`,
`ExtentReportExtension.java`, `Metodos_Rest.java`, `Servico_Parametros.java`, `ConfigLoader.java`,
`pom.xml`, `.gitignore`, `README.md`.

Nunca suba ao GitHub: credenciais, IPs internos, nomes de cliente, nomes de sistema que identifiquem o cliente.
IPs internos e URLs de ambiente ficam somente em `config.properties` (gitignore).
O README não deve conter nomes de cliente, nomes de sistema interno ou dados que identifiquem o cliente.

---

## Arquitetura

### Fluxo de execução
```
mvn test → SuiteInterno → SCC_Teste, SicsDynaMetrica
  Para cada teste:
    ExtentReportExtension.beforeEach → captura System.out (tee), instala filtros REST Assured
    @Test → ConfigLoader lê config.properties → REST Assured dispara requisição → Metodos_Rest valida
    ExtentReportExtension.afterEach  → processa output, registra no relatório
  ExtentReportExtension.afterAll     → flush → gera HTML → abre Chrome
```

### Configuração externa
- `ConfigLoader.get("chave")` lê `config.properties` do classpath
- Lança exceção explícita se a chave não existir ou estiver vazia
- Chaves usadas: `interno.base1`, `interno.base2.pa`, `interno.base2.fila`

### Relatórios
- `ExtentReportExtension` gera um relatório HTML independente por classe
- Relatórios em `target/SCC_Teste.html` e `target/SicsDynaMetrica.html`
- A extensão usa `ConcurrentHashMap<String, ClassData>` — cada classe tem seu próprio `ExtentReports`
- Não usar `.log().all()` nas requisições — a extensão já captura via filtros globais (`replaceFiltersWith`)

---

## Padrões de Código

### Classe de teste
```java
@ExtendWith(ExtentReportExtension.class)
public class MinhaClasse {

    @BeforeAll
    public static void warmup() {
        RestAssured.config = RestAssured.config()
            .httpClient(HttpClientConfig.httpClientConfig().dontReuseHttpClientInstance());
        try { RestAssured.given().get(URL).then().extract().response(); } catch (Exception ignored) {}
    }

    @AfterAll
    public static void tearDown() {
        RestAssured.baseURI = RestAssured.DEFAULT_URI;
        RestAssured.port    = RestAssured.UNDEFINED_PORT;
        RestAssured.basePath = "";
        RestAssured.requestSpecification  = null;
        RestAssured.responseSpecification = null;
    }

    @Test
    public void test_NomeDescritivo() {
        String apiUrl = ConfigLoader.get("chave") + "/aspect/rest/endpoint";

        Response response = RestAssured.given()
                .when().get(apiUrl)
                .then().statusCode(200)
                .extract().response();

        Metodos_Rest.imprimirCabecalho("TESTE API endpoint", apiUrl);

        // validações com Metodos_Rest.*
        Map<String, Object> result = response.jsonPath().getMap("result");
        assertNotNull(result, "result não deve ser nulo");
        Metodos_Rest.validarString(result, "campo");
        Metodos_Rest.validarCamposExatos(result, Set.of("campo1", "campo2"));
    }
}
```

### Validações — usar Metodos_Rest
| Método | Uso |
|---|---|
| `validarBoolean(map, campo, esperado)` | Campo Boolean com valor esperado |
| `validarString(map, campo)` | String não vazia |
| `validarInteiro(map, campo)` | Integer não nulo |
| `validarInteiroPositivo(map, campo)` | Integer > 0 |
| `validarContentType(response)` | Content-Type é application/json |
| `validarTempoResposta(response)` | Tempo < 5000 ms |
| `validarSenhaSemPrioridade(map, campo)` | String numérica pura, > 0, 1–10 chars |
| `validarSenhaComPrioridade(map, campo)` | Letra + dígitos, > 0, 2–10 chars |
| `validarEmissao(map, campo)` | Data `MM/dd/yyyy HH:mm:ss` (formato americano da API) |
| `validarQuantidade(lista, qtd, label)` | Tamanho da lista confere com Quantidade da API |
| `validarCamposItem(map, esperados, label)` | Campos esperados presentes (extras = alerta, não falha) |
| `validarCamposExatos(map, esperados)` | Campos exatos — sem extras nem faltando |
| `validarCorHex(map, campo)` | Hex 6 dígitos |
| `validarReferenciaExiste(lista, chave, valor, label)` | Integridade referencial |
| `validarStringNumerica(map, campo)` | String só com dígitos |
| `validarHorario(map, campo)` | Formato `HH:mm:ss` |
| `validarDataHoraBR(map, campo)` | Formato `dd/MM/yyyy HH:mm:ss` (brasileiro) |
| `validarIP(map, campo)` | IPv4 `n.n.n.n` |
| `validarURL(valor, protocolo)` | URL com protocolo informado |
| `imprimirCabecalho(titulo, apiUrl)` | Cabeçalho de seção no relatório |
| `relatorioCampos(map, esperados)` | Campos novos detectados (alerta) |
| `relatorioCamposLista(lista, esperados, rotulo)` | Campos novos por item da lista |

### Assertions resilientes
- Preferir `greaterThanOrEqualTo(N)` em vez de `hasSize(N)` quando a API pode retornar mais itens
- Mensagens de asserção em português: usar `"nulo"` e não `"null"`, `"booleano"` e não `"Boolean"`

### Constantes de timeout
```java
private static final long RESPONSE_TIMEOUT_MS = 5000;
```

---

## Regras do Git

### O que vai ao GitHub
- `SCC_Teste.java`, `SicsDynaMetrica.java`, `SuiteInterno.java`
- `ExtentReportExtension.java`, `Metodos_Rest.java`, `Servico_Parametros.java`, `ConfigLoader.java`
- `pom.xml`, `.gitignore`, `README.md`

### O que NÃO vai ao GitHub (gitignore)
- `config.properties` — contém IPs internos e URLs de ambiente
- `BarrigaTest.java`, `BarrigaRest.java`, `AuthHelper.java`, `BaseTest.java`, `Constantes.java`, `Movimentacao.java`
- Todo o pacote `br/ce/wcaquino/rest/` (testes de estudo/refac)

### Maven Surefire
Configurado para incluir somente `**/SuiteInterno.java`. Não alterar isso sem intenção deliberada.

---

## README

O README deve conter:
- Tech stack com versões
- Estrutura de pastas (somente arquivos que vão ao git)
- Seção de configuração com aviso de gitignore
- Tabela de testes por classe (nome do teste, método HTTP, endpoint, o que valida)
- Informação sobre geração de relatório

O README **não deve conter**: nomes de cliente, nomes de sistemas internos, IPs, URLs reais, dados que identifiquem o cliente.

---

## Documentação em PDF

Para gerar a documentação em PDF:
1. Criar `Documentacao_Testes.html` com tema dark (fundo `#0d1117`, acentos ciano `#39d0c4`)
2. Converter via Chrome headless:
```powershell
& "C:\Program Files\Google\Chrome\Application\chrome.exe" `
  --headless --disable-gpu `
  --print-to-pdf="Documentacao_Testes.pdf" `
  --no-pdf-header-footer `
  "file:///caminho/para/Documentacao_Testes.html"
```

---

## O que NÃO fazer

- Não usar `.log().all()` nas requisições — a `ExtentReportExtension` já captura tudo
- Não usar `registered` (flag estático) para compartilhar `ExtentReports` entre classes
- Não fazer asserção `hasSize(N)` quando a API pode retornar N ou mais itens
- Não colocar IPs ou URLs no código — sempre via `ConfigLoader`
- Não commitar `config.properties`
- Não mencionar nome do cliente ou sistema no README ou qualquer arquivo versionado
- Não usar `assertj-core` — removido do projeto; usar apenas JUnit 5 assertions + Hamcrest

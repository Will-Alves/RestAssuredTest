# restassured-tests

Suite de testes automatizados de API REST. Valida contratos de API, presença de campos, tipos de dados e regras de negócio.

## Tech Stack

- **Java 17**
- **REST Assured 5.4.0**
- **JUnit 5**
- **Maven**
- **ExtentReports 5**

## Project Structure

```
src/test/java/
├── br/com/testes/
│   ├── BaseTest.java                  # Configuração base (URL, porta, Content-Type, timeout)
│   ├── Constantes.java                # Constantes compartilhadas
│   ├── ConfigLoader.java              # Carregador de configuração externa (JSON)
│   ├── Metodos_Rest.java              # Helpers de validação reutilizáveis
│   ├── ExtentReportExtension.java     # Extensão JUnit 5 para ExtentReports
│   ├── Servico_Parametros.java        # Validador de parâmetros de serviço
│   ├── [testes de API internos]       # Classes de teste por domínio
│   └── suite/
│       └── SuiteInterno.java          # Suite de serviços internos
│
└── br/ce/wcaquino/rest/
    ├── utils/
    │   └── DataUtils.java             # Utilitários de data
    └── tests/refac/
        ├── ContasTest.java            # Testes de contas
        ├── MovimentacaoTest.java      # Testes de movimentações
        ├── SaldoTest.java             # Testes de saldo
        ├── AuthTest.java              # Testes de autenticação
        └── suite/
            └── SuiteRefac.java        # Suite: contas + movimentações + saldo + auth
```

## Executando os Testes

Rodar uma suite completa:

```bash
mvn test -Dtest="br.ce.wcaquino.rest.tests.refac.suite.SuiteRefac" -Dsurefire.failIfNoSpecifiedTests=false
mvn test -Dtest="br.com.testes.suite.SuiteInterno" -Dsurefire.failIfNoSpecifiedTests=false
```

Rodar uma classe específica:

```bash
mvn test -Dtest=NomeDaClasse -Dsurefire.failIfNoSpecifiedTests=false
```

## Suites

| Suite | Descrição |
|-------|-----------|
| `SuiteRefac` | Testes de API pública com login e reset por classe |
| `SuiteInterno` | Testes de serviços internos |

## Relatório

O ExtentReport é gerado em `test-output/` após cada execução.

# restassured-tests

Suite de testes automatizados de API REST para validação de contratos, campos, tipos de dados e regras de negócio de serviços internos.

## Tech Stack

| Tecnologia | Versão |
|---|---|
| Java | 17 |
| REST Assured | 5.4.0 |
| JUnit 5 (Jupiter) | 5.x |
| ExtentReports | 5.x |
| Maven | 3.x |

## Estrutura do Projeto

```
src/test/java/br/com/testes/
├── suite/
│   └── SuiteInterno.java          # Ponto de entrada — agrupa SCC_Teste e SicsDynaMetrica
├── SCC_Teste.java                 # Testes de geração de senha e TAGs
├── SicsDynaMetrica.java                 # Testes de monitoramento de filas e parâmetros
├── ExtentReportExtension.java     # Extensão JUnit 5 para geração de relatório HTML
├── Metodos_Rest.java              # Validações reutilizáveis (campos, tipos, formatos)
├── Servico_Parametros.java        # Validador de grupos e parâmetros de serviço
└── ConfigLoader.java              # Leitura de configuração externa (config.properties)

src/test/resources/
└── config.properties              # ⚠ Não versionado — ver seção Configuração
```

## Configuração

Crie o arquivo `src/test/resources/config.properties` com as chaves necessárias para os testes. O arquivo **não é versionado** (está no `.gitignore`) pois contém URLs e endereços de ambiente interno.

```properties
interno.base1=<url-base-1>
interno.base2.pa=<url-base-2-pa>
interno.base2.fila=<url-base-2-fila>
```

## Executando os Testes

Rodar a suite completa:

```bash
mvn test
```

Rodar uma classe específica:

```bash
mvn test -Dtest=SCC_Teste -Dsurefire.failIfNoSpecifiedTests=false
mvn test -Dtest=SicsDynaMetrica  -Dsurefire.failIfNoSpecifiedTests=false
```

## Testes — SCC_Teste

| Teste | Método | Endpoint | O que valida |
|---|---|---|---|
| `test_PA_ListarTAGsPossiveis` | GET | `/PA/ListarTAGsPossiveis/{fila}/{unidade}` | Grupos e TAGs com integridade referencial |
| `test_GerarSenha` | POST | `/senha/gerarsenha` | Geração de senha sem prioridade |
| `test_GerarSenhaComPrioridade` | POST | `/senha/gerarsenha` | Geração de senha com prioridade |
| `test_ChamarProximo` | GET | `/senha/Chamarproximo/{fila}/{unidade}` | Chamada do próximo atendimento |

## Testes — SicsDynaMetrica

| Teste | Método | Endpoint | O que valida |
|---|---|---|---|
| `test_ValidarParametrosServico` | GET | `/Servico/Parametros/1` | Parâmetros de configuração do serviço |
| `test_ValidarListarTMEDia` | GET | `/fila/ListarTMEDia/` | TME do dia por fila |
| `test_ValidarMaiorEspera` | GET | `/fila/ObterMaiorEspera` | Senha com maior tempo de espera |
| `test_ValidarObterQtdEsperaV2` | GET | `/fila/ObterQtdEsperaV2` | Quantidade de senhas aguardando por fila |

## Relatório

Após a execução, os relatórios HTML são gerados em:

```
target/SCC_Teste.html
target/SicsDynaMetrica.html
```

O relatório é aberto automaticamente no Chrome ao fim de cada classe.

package br.com.testes;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.response.Response;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ExtentReportExtension.class)
public class SicsDynaMetrica {

    private static final String API_URL = ConfigLoader.get("interno.base1") + "/aspect/rest/Servico/Parametros/1";

    @BeforeAll
    public static void warmup() {
        RestAssured.config = RestAssured.config()
                .httpClient(HttpClientConfig.httpClientConfig().dontReuseHttpClientInstance());
        try {
            RestAssured.given().get(API_URL).then().extract().response();
        } catch (Exception ignored) {
        }
    }

    @AfterAll
    public static void tearDown() {
        RestAssured.baseURI = RestAssured.DEFAULT_URI;
        RestAssured.port = RestAssured.UNDEFINED_PORT;
        RestAssured.basePath = "";
        RestAssured.requestSpecification = null;
        RestAssured.responseSpecification = null;
    }

    @Test
    public void test_ValidarParametrosServico() {
        Response response = RestAssured.given()
                .log().ifValidationFails()
                .when()
                .get(API_URL)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract()
                .response();

        Metodos_Rest.imprimirCabecalho("TESTE API Servico/Parametros", API_URL);

        Map<String, Object> result = response.jsonPath().getMap("result");
        assertNotNull(result, "result não deve ser null");

        Metodos_Rest.validarString(result, "nomeServico");
        Metodos_Rest.validarIP(result, "enderecoIP");

        List<?> configuracoes = response.jsonPath().getList("result.configuracoes");
        assertNotNull(configuracoes, "configuracoes não deve ser null");
        assertFalse(configuracoes.isEmpty(), "configuracoes não deve ser vazia");
        System.out.println("  ✔ configuracoes.size(): " + configuracoes.size());

        Set<String> camposEsperadosGrupo = Set.of("grupo", "parametros");
        Set<String> camposEsperadosParametro = Set.of("chave", "valor");

        Servico_Parametros.validarConfiguracoes(configuracoes, camposEsperadosGrupo, camposEsperadosParametro);

        Metodos_Rest.relatorioCampos(result, Set.of("nomeServico", "enderecoIP", "configuracoes"));
    }

    @Test
    public void test_ValidarListarTMEDia() {
        String apiUrl = ConfigLoader.get("interno.base2.fila") + "/aspect/rest/fila/ListarTMEDia/";

        Response response = RestAssured.given()
                .log().ifValidationFails()
                .queryParam("Unidade", 1)
                .queryParam("Fila", "1,2,3,4,5")
                .when()
                .get(apiUrl)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract()
                .response();

        Metodos_Rest.imprimirCabecalho("TESTE API fila/ListarTMEDia", apiUrl);

        List<?> filas = response.jsonPath().getList("result.Filas");
        assertNotNull(filas, "result.Filas não deve ser null");
        System.out.println("  ✔ Filas.size(): " + filas.size());

        Set<String> camposEsperados = Set.of("ID", "Nome", "TMEDia");
        List<Map<?, ?>> filasMapeadas = new java.util.ArrayList<>();

        for (Object rawFila : filas) {
            if (!(rawFila instanceof Map<?, ?> fila))
                continue;
            filasMapeadas.add(fila);
            Metodos_Rest.validarCamposItem(fila, camposEsperados, "Fila");
            Metodos_Rest.validarString(fila, "ID");
            Metodos_Rest.validarString(fila, "Nome");
            Metodos_Rest.validarHorario(fila, "TMEDia");
            System.out.println("  ✔ Fila validada -> ID=" + fila.get("ID") + " Nome=" + fila.get("Nome") + " TMEDia="
                    + fila.get("TMEDia"));
        }

        Metodos_Rest.relatorioCamposLista(filasMapeadas, camposEsperados, "Filas");
    }

    @Test
    public void test_ValidarMaiorEspera() {
        String apiUrl = ConfigLoader.get("interno.base1") + "/aspect/rest/fila/ObterMaiorEspera";

        Response response = RestAssured.given()
                .log().ifValidationFails()
                .queryParam("Unidade", 1)
                .queryParam("Fila", "1,2,3,4,5")
                .when()
                .get(apiUrl)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract()
                .response();

        Metodos_Rest.imprimirCabecalho("TESTE API fila/ObterMaiorEspera", apiUrl);

        Map<String, Object> result = response.jsonPath().getMap("result");
        assertNotNull(result, "result não deve ser null");

        Map<String, Object> senha = response.jsonPath().getMap("result.Senha");
        assertNotNull(senha, "result.Senha não deve ser null");

        // Numero — String numérica
        Metodos_Rest.validarSenhaSemPrioridade(senha, "Numero");

        // Fila
        Metodos_Rest.validarString(senha, "Fila");
        System.out.println("  ✔ Fila: " + senha.get("Fila"));

        // DataEntradaFila — formato dd/MM/yyyy HH:mm:ss
        Metodos_Rest.validarDataHoraBR(senha, "DataEntradaFila");

        // TempoEspera — formato HH:mm:ss
        Metodos_Rest.validarHorario(senha, "TempoEspera");

        Metodos_Rest.relatorioCampos(senha, Set.of("Numero", "Fila", "DataEntradaFila", "TempoEspera"));
    }

    @Test
    public void test_ValidarObterQtdEsperaV2() {
        String apiUrl = ConfigLoader.get("interno.base1") + "/aspect/rest/fila/ObterQtdEsperaV2";

        Response response = RestAssured.given()
                .log().ifValidationFails()
                .queryParam("Unidade", 1)
                .queryParam("Fila", "1,2,3,4,5")
                .when()
                .get(apiUrl)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract()
                .response();

        Metodos_Rest.imprimirCabecalho("TESTE API fila/ObterQtdEsperaV2", apiUrl);

        List<?> filas = response.jsonPath().getList("result.Filas");
        assertNotNull(filas, "result.Filas não deve ser null");
        System.out.println("  ✔ Filas.size(): " + filas.size());

        Set<String> camposEsperados = Set.of("NomeFila", "QtdeEspera");
        List<Map<?, ?>> filasMapeadas = new java.util.ArrayList<>();

        for (Object rawFila : filas) {
            if (!(rawFila instanceof Map<?, ?> fila))
                continue;
            filasMapeadas.add(fila);
            Metodos_Rest.validarCamposItem(fila, camposEsperados, "Fila");
            Metodos_Rest.validarString(fila, "NomeFila");
            Metodos_Rest.validarStringNumerica(fila, "QtdeEspera");
            System.out.println("  ✔ " + fila.get("NomeFila") + ": " + fila.get("QtdeEspera") + " em espera");
        }

        Metodos_Rest.relatorioCamposLista(filasMapeadas, camposEsperados, "Filas");
    }
}

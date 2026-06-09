package br.com.testes;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.response.Response;

import static org.junit.jupiter.api.Assertions.*;

// import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ExtentReportExtension.class)
public class SCC_Teste {

    @BeforeAll
    public static void warmup() {
        RestAssured.config = RestAssured.config()
                .httpClient(HttpClientConfig.httpClientConfig().dontReuseHttpClientInstance());
        try { RestAssured.given().get("http://192.168.0.196:80/aspect/rest/Servico/Parametros/1").then().extract().response(); } catch (Exception ignored) {}
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
    public void test_PA_ListarTAGsPossiveis() {
        String apiUrl = "http://192.168.0.116:26616/aspect/rest/PA/ListarTAGsPossiveis/{param1}/{param2}";

        Response response = RestAssured.given()
                .pathParam("param1", 5)
                .pathParam("param2", 2)
                .when()
                .get(apiUrl)
                .then()
                .statusCode(200)
                .extract()
                .response();

        Metodos_Rest.imprimirCabecalho("TESTE API PA/ListarTAGsPossiveis",
                apiUrl.replace("{param1}", "5").replace("{param2}", "2"));

        Set<String> camposEsperadosGrupo = Set.of("ID", "Nome");
        Set<String> camposEsperadosTag = Set.of("ID", "Nome", "GrupoId", "Cor");

        Integer qtdGrupos = response.path("result.GruposDeTags.Quantidade");
        List<Map<String, Object>> grupos = response.path("result.GruposDeTags.Itens");
        Integer qtdTags = response.path("result.TAGs.Quantidade");
        List<Map<String, Object>> tags = response.path("result.TAGs.Itens");

        Metodos_Rest.validarQuantidade(grupos, qtdGrupos, "Grupos");
        Metodos_Rest.validarQuantidade(tags, qtdTags, "TAGs");

        System.out.println("\nGrupos:");
        for (Map<String, Object> grupo : grupos) {
            Metodos_Rest.validarCamposItem(grupo, camposEsperadosGrupo, "Grupo");
            Metodos_Rest.validarInteiro(grupo, "ID");
            Metodos_Rest.validarString(grupo, "Nome");
        }

        System.out.println("\nTAGs:");
        for (Map<String, Object> tag : tags) {
            Metodos_Rest.validarCamposItem(tag, camposEsperadosTag, "TAG");
            Metodos_Rest.validarInteiro(tag, "ID");
            Metodos_Rest.validarString(tag, "Nome");
            Metodos_Rest.validarInteiro(tag, "GrupoId");
            Metodos_Rest.validarCorHex(tag, "Cor");
            Metodos_Rest.validarReferenciaExiste(grupos, "ID", tag.get("GrupoId"), "GrupoId");
        }
    }


    @Test
    public void test_GerarSenha() {
        String apiUrl = "http://192.168.0.196:80/aspect/rest/senha/gerarsenha";

        String body = "{ \"Fila\": \"5\", \"Totem\": \"0\", \"IdUnidade\": \"2\" }";

        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(body)
                .when()
                .post(apiUrl)
                .then()
                .statusCode(200)
                .extract()
                .response();

        Metodos_Rest.imprimirCabecalho("TESTE API senha/gerarsenha", apiUrl);

        Metodos_Rest.validarContentType(response);
        Metodos_Rest.validarTempoResposta(response);

        Map<String, Object> result = response.jsonPath().getMap("result");
        assertNotNull(result, "result não deve ser null");

        Metodos_Rest.validarBoolean(result, "sucesso", true);
        Metodos_Rest.validarSenhaSemPrioridade(result, "senha");
        Metodos_Rest.validarEmissao(result, "emissao");
        Metodos_Rest.validarCamposExatos(result, Set.of("sucesso", "senha", "emissao"));
    }

    @Test
    public void test_GerarSenhaComPrioridade() {
        String apiUrl = "http://192.168.0.196:80/aspect/rest/senha/gerarsenha";

        String body = "{ \"Fila\": 1, \"Totem\": 0, \"IdUnidade\": 2, \"Prioridade\": 1 }";

        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(body)
                .when()
                .post(apiUrl)
                .then()
                .statusCode(200)
                .extract()
                .response();

        Metodos_Rest.imprimirCabecalho("TESTE API senha/gerarsenha (com prioridade)", apiUrl);

        Metodos_Rest.validarContentType(response);
        Metodos_Rest.validarTempoResposta(response);

        Map<String, Object> result = response.jsonPath().getMap("result");
        assertNotNull(result, "result não deve ser null");

        Metodos_Rest.validarBoolean(result, "sucesso", true);
        Metodos_Rest.validarSenhaComPrioridade(result, "senha");
        Metodos_Rest.validarEmissao(result, "emissao");
        Metodos_Rest.validarCamposExatos(result, Set.of("sucesso", "senha", "emissao"));
    }

    @Test
    public void test_ChamarProximo() {
        String apiUrl = "http://192.168.0.196:80/aspect/rest/senha/Chamarproximo/{fila}/{unidade}";

        Response response = RestAssured.given()
                .log().all()
                .pathParam("fila", 1)
                .pathParam("unidade", 2)
                .when()
                .get(apiUrl)
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .response();

        Metodos_Rest.imprimirCabecalho("TESTE API senha/Chamarproximo", apiUrl.replace("{fila}", "1").replace("{unidade}", "2"));

        Metodos_Rest.validarContentType(response);
        Metodos_Rest.validarTempoResposta(response);

        Map<String, Object> result = response.jsonPath().getMap("result");
        assertNotNull(result, "result não deve ser null");

        Metodos_Rest.validarBoolean(result, "Sucesso", true);
        Metodos_Rest.validarSenhaSemPrioridade(result, "Senha");
        Metodos_Rest.validarDataHoraBR(result, "DataHora");
        Metodos_Rest.validarInteiroPositivo(result, "IdFila");
        Metodos_Rest.validarString(result, "NomeFila");
        Metodos_Rest.validarCamposExatos(result, Set.of("Sucesso", "Senha", "DataHora", "IdFila", "NomeFila"));
    }

}

package br.ce.wcaquino.rest.tests.refac;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import br.com.testes.BaseTest;
import br.com.testes.ConfigLoader;

public class ContasTest extends BaseTest {

    private static String TOKEN;

    @BeforeAll
    public static void login() {
        RestAssured.requestSpecification = null;

        Map<String, String> login = new HashMap<>();
        login.put("email", ConfigLoader.get("barriga.email"));
        login.put("senha", ConfigLoader.get("barriga.senha"));

        TOKEN = given()
                .baseUri("https://barrigarest.wcaquino.me")
                .port(443)
                .contentType(ContentType.JSON)
                .body(login)
                .when()
                .post("/signin")
                .then()
                .statusCode(200)
                .extract().path("token");

        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addHeader("Authorization", "JWT " + TOKEN)
                .build();

        RestAssured.get("/reset");
    }

    public Integer getIdContaPeloNome(String nome) {
        return RestAssured.get("/contas?nome=" + nome).then().extract().path("id[0]");
    }

    @Test
    public void deveIncluirContaComSucesso() {
        given()
                .body("{ \"nome\": \"Conta inserida\" }")
                .when()
                .post("/contas")
                .then()
                .statusCode(201);
    }

    @Test
    public void naoDeveInserirContaMesmoNome() {
        given()
                .body("{ \"nome\": \"Conta mesmo nome\" }")
                .when()
                .post("/contas")
                .then()
                .statusCode(400)
                .body("error", is("Já existe uma conta com esse nome!"));
    }

    @Test
    public void deveAlterarContaComSucesso() {
        Integer CONTA_ID = getIdContaPeloNome("Conta para alterar");

        given()
                .body("{ \"nome\": \"Conta alterada\" }")
                .pathParam("id", CONTA_ID)
                .when()
                .put("/contas/{id}")
                .then()
                .statusCode(200)
                .body("nome", is("Conta alterada"));
    }
}

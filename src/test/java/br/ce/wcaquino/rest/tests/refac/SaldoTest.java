package br.ce.wcaquino.rest.tests.refac;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import br.com.testes.BaseTest;
import br.com.testes.ConfigLoader;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;

public class SaldoTest extends BaseTest {

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
    public void deveCalcularSaldoContas() {
        Integer CONTA_ID = getIdContaPeloNome("Conta para saldo");

        given()
                .when()
                .get("/saldo")
                .then()
                .statusCode(200)
                .body("find{it.conta_id == " + CONTA_ID + "}.saldo", is("534.00"));
    }

}

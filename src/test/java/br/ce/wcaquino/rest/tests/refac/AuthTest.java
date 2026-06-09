package br.ce.wcaquino.rest.tests.refac;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import br.com.testes.BaseTest;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.FilterableRequestSpecification;

public class AuthTest extends BaseTest {

    private static String TOKEN;

    @BeforeAll
    public static void login() {
        RestAssured.requestSpecification = null;

        Map<String, String> login = new HashMap<>();
        login.put("email", "willsouza@yahoo.com");
        login.put("senha", "123456");

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

    @Test
    public void naoDeveAcessarAPISemToken() {
        FilterableRequestSpecification req = (FilterableRequestSpecification) RestAssured.requestSpecification;
        req.removeHeader("Authorization");

        given()
                .when()
                .get("/contas")
                .then()
                .statusCode(401);
    }

}

package br.ce.wcaquino.rest.tests.refac;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Map;

import br.com.testes.ConfigLoader;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;

public class AuthHelper {

    public static String doLogin() {
        RestAssured.requestSpecification = null;

        Map<String, String> login = new HashMap<>();
        login.put("email", ConfigLoader.get("barriga.email"));
        login.put("senha", ConfigLoader.get("barriga.senha"));

        String token = given()
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
                .addHeader("Authorization", "JWT " + token)
                .build();

        RestAssured.get("/reset");
        return token;
    }
}

package br.com.testes;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import br.ce.wcaquino.rest.utils.DataUtils;
import io.restassured.RestAssured;
import io.restassured.specification.FilterableRequestSpecification;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ExtentReportExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BarrigaTest extends BaseTest {

    private static String TOKEN;
    private static String CONTA_NAME = "Conta " + System.nanoTime();
    private static Integer CONTA_ID;
    private static Integer MOV_ID;

    @BeforeAll
    public static void login() {
        RestAssured.requestSpecification = null;
        Map<String, String> login = new HashMap<>();
        login.put("email", ConfigLoader.get("barriga.email"));
        login.put("senha", ConfigLoader.get("barriga.senha"));

        TOKEN = given()
                .body(login)
                .when()
                .post("/signin")
                .then()
                .statusCode(200)
                .extract().path("token");

        RestAssured.requestSpecification.header("Authorization", "JWT " + TOKEN);
    }

    @Test
    @Order(1)
    public void deveIncluirContaComSucesso() {
        CONTA_ID = given()

                .body("{ \"nome\": \"" + CONTA_NAME + "\" }")
                .when()
                .post("/contas")
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    @Test
    @Order(2)
    public void deveAlterarContaComSucesso() {
        given()

                .body("{ \"nome\": \"" + CONTA_NAME + " alterada\" }")
                .pathParam("id", CONTA_ID)
                .when()
                .put("/contas/{id}")
                .then()
                .statusCode(200)
                .body("nome", is(CONTA_NAME + " alterada"));
    }

    @Test
    @Order(3)
    public void naoDeveInserirContaMesmoNome() {
        given()

                .body("{ \"nome\": \"" + CONTA_NAME + " alterada\" }")
                .when()
                .post("/contas")
                .then()
                .statusCode(400)
                .body("error", is("Já existe uma conta com esse nome!"));
    }

    @Test
    @Order(6)
    public void deveInserirMovimentacaoSucesso() {
        Movimentacao mov = getMovimentacaoValida();

        MOV_ID = given()

                .body(mov)
                .when()
                .post("/transacoes")
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    @Test
    @Order(4)
    public void deveValidarCamposObrigatoriosMovimentacao() {
        given()

                .body("{}")
                .when()
                .post("/transacoes")
                .then()
                .statusCode(400)
                .body("$", hasSize(8))
                .body("msg", hasItems(
                        "Data da Movimentação é obrigatório",
                        "Data do pagamento é obrigatório",
                        "Descrição é obrigatório",
                        "Interessado é obrigatório",
                        "Valor é obrigatório",
                        "Valor deve ser um número",
                        "Conta é obrigatório",
                        "Situação é obrigatório"));
    }

    @Test
    @Order(5)
    public void naoDeveInserirMovimentacaoComDataFutura() {
        Movimentacao mov = getMovimentacaoValida();
        mov.setData_transacao(DataUtils.getDataDiferencaDias(2));

        given()

                .body(mov)
                .when()
                .post("/transacoes")
                .then()
                .statusCode(400)
                .body("$", hasSize(1))
                .body("msg", hasItems("Data da Movimentação deve ser menor ou igual à data atual"));
    }

    @Test
    @Order(7)
    public void naoDeveRemoverContaComMovimentacao() {
        given()

                .pathParam("id", CONTA_ID)
                .when()
                .delete("/contas/{id}")
                .then()
                .statusCode(500)
                .body("constraint", is("transacoes_conta_id_foreign"));
    }

    @Test
    @Order(8)
    public void deveCalcularSaldoContas() {
        given()

                .when()
                .get("/saldo")
                .then()
                .statusCode(200)
                .body("find{it.conta_id == " + CONTA_ID + "}.saldo", is("100.00"));
    }

    @Test
    @Order(9)
    public void deveRemoverMovimentacao() {
        given()

                .pathParam("id", MOV_ID)
                .when()
                .delete("/transacoes/{id}")
                .then()
                .statusCode(204);
    }

    // @Test
    // @Order(8)
    // public void deveApagarContaCriada() {
    //     given()
    //             .pathParam("id", CONTA_ID)
    //             .when()
    //             .delete("/contas/{id}")
    //             .then()
    //             .statusCode(204);
    // }

    @Test
    @Order(10)
    public void naoDeveAcessarAPISemToken() {

        FilterableRequestSpecification req = (FilterableRequestSpecification) RestAssured.requestSpecification;
        req.removeHeader("Authorization");

        given()
                .when()
                .get("/contas")
                .then()
                .statusCode(401);

        RestAssured.requestSpecification.header("Authorization", "JWT " + TOKEN);
    }

    private Movimentacao getMovimentacaoValida() {
        Movimentacao mov = new Movimentacao();
        mov.setConta_id(CONTA_ID);
        // mov.setUsuario_id(usuario_id);
        mov.setDescricao("Descricao da movimentacao");
        mov.setEnvolvido("Envolvido na mov");
        mov.setTipo("REC");
        mov.setData_transacao(DataUtils.getDataDiferencaDias(-1));
        mov.setData_pagamento(DataUtils.getDataDiferencaDias(5));
        mov.setValor(100f);
        mov.setStatus(true);
        return mov;
    }
}

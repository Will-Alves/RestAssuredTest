package br.ce.wcaquino.rest.tests.refac;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import br.ce.wcaquino.rest.utils.DataUtils;
import br.com.testes.BaseTest;
import br.com.testes.Movimentacao;
import io.restassured.RestAssured;

public class MovimentacaoTest extends BaseTest {

    private static Integer USUARIO_ID;

    @BeforeAll
    public static void login() {
        AuthHelper.doLogin();
        USUARIO_ID = RestAssured.get("/contas").then().extract().path("usuario_id[0]");
    }

    private Integer getIdMovPelaDescricao(String desc) {
        return RestAssured.get("/transacoes?descricao=" + desc).then().extract().path("id[0]");
    }

    private Integer getIdContaPeloNome(String nome) {
        return RestAssured.get("/contas?nome=" + nome).then().extract().path("id[0]");
    }

    private Movimentacao getMovimentacaoValida() {
        Movimentacao mov = new Movimentacao();
        mov.setConta_id(getIdContaPeloNome("Conta para movimentacoes"));
        mov.setUsuario_id(USUARIO_ID);
        mov.setDescricao("Descricao da movimentacao");
        mov.setEnvolvido("Envolvido na mov");
        mov.setTipo("REC");
        mov.setData_transacao(DataUtils.getDataDiferencaDias(-1));
        mov.setData_pagamento(DataUtils.getDataDiferencaDias(5));
        mov.setValor(new java.math.BigDecimal("100.00"));
        mov.setStatus(true);
        return mov;
    }

    @Test
    public void deveInserirMovimentacaoSucesso() {
        given()
                .body(getMovimentacaoValida())
                .when()
                .post("/transacoes")
                .then()
                .statusCode(201);
    }

    @Test
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
                .body("msg", hasItem("Data da Movimentação deve ser menor ou igual à data atual"));
    }

    @Test
    public void naoDeveRemoverContaComMovimentacao() {
        Integer CONTA_ID = getIdContaPeloNome("Conta com movimentacao");

        given()
                .pathParam("id", CONTA_ID)
                .when()
                .delete("/contas/{id}")
                .then()
                .statusCode(500)
                .body("constraint", is("transacoes_conta_id_foreign"));
    }

    @Test
    public void deveRemoverMovimentacao() {
        Integer MOV_ID = getIdMovPelaDescricao("Movimentacao para exclusao");

        given()
                .pathParam("id", MOV_ID)
                .when()
                .delete("/transacoes/{id}")
                .then()
                .statusCode(204);
    }
}

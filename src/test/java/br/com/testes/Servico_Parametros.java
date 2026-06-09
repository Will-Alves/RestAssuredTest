package br.com.testes;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Servico_Parametros {

    public static void validarConfiguracoes(
            List<?> configuracoes,
            Set<String> camposEsperadosGrupo,
            Set<String> camposEsperadosParametro) {

        for (Object rawConfig : configuracoes) {
            if (!(rawConfig instanceof Map<?, ?> config)) continue;
            Metodos_Rest.validarCamposItem(config, camposEsperadosGrupo, "Configuração");
            Metodos_Rest.validarString(config, "grupo");
            String grupo = config.get("grupo").toString();
            System.out.println("\n  Grupo: " + grupo);

            Object rawParametros = config.get("parametros");
            assertNotNull(rawParametros, "[" + grupo + "] parametros não deve ser null");
            assertTrue(rawParametros instanceof List<?>, "[" + grupo + "] parametros deve ser uma lista");
            List<?> parametros = (List<?>) rawParametros;
            assertFalse(parametros.isEmpty(), "[" + grupo + "] parametros não deve ser vazio");
            System.out.println("  ✔ parametros.size(): " + parametros.size());

            for (Object rawParam : parametros) {
                if (!(rawParam instanceof Map<?, ?> param)) continue;
                String chave = param.get("chave") != null ? param.get("chave").toString() : "null";
                String valor = param.get("valor") != null ? param.get("valor").toString() : "null";
                Metodos_Rest.validarCamposItem(param, camposEsperadosParametro, "Parâmetro");
                Metodos_Rest.validarString(param, "chave");
                assertFalse(valor.isBlank(), "[" + grupo + "] " + chave + " - valor não deve ser vazio");

                if (chave.equals("EndPointDynatrace")) {
                    Metodos_Rest.validarURL(valor, "https://");
                } else if (chave.equals("EndPointSccListarUnidade")) {
                    Metodos_Rest.validarURL(valor, "http://");
                } else if (chave.equals("ApiKeyDynatrace")) {
                    assertFalse(valor.isBlank(), "[" + grupo + "] ApiKeyDynatrace não deve ser vazio");
                    System.out.println("    ✔ ApiKeyDynatrace presente e não vazio");
                } else {
                    System.out.println("    ✔ " + chave + ": " + valor);
                }
            }
        }
    }
}

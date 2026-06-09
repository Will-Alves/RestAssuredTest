package br.com.testes;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    private static final Properties props = new Properties();

    static {
        try (InputStream input = ConfigLoader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties não encontrado. Crie o arquivo baseado em config.properties.example");
            }
            props.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar config.properties: " + e.getMessage(), e);
        }
    }

    public static String get(String chave) {
        String valor = props.getProperty(chave);
        if (valor == null || valor.isBlank()) {
            throw new RuntimeException("Propriedade não encontrada ou vazia: " + chave);
        }
        return valor;
    }
}

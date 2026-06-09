package br.com.testes;

import static org.junit.jupiter.api.Assertions.*;

import io.restassured.response.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Metodos_Rest {

    public static final String BOLD = "[1m";
    public static final String RESET = "[0m";

    public static void validarBoolean(Map<?, ?> map, String campo, boolean esperado) {
        assertNotNull(map.get(campo), campo + " não deve ser null");
        assertInstanceOf(Boolean.class, map.get(campo), campo + " deve ser Boolean");
        assertEquals(esperado, map.get(campo), campo + " deve ser " + esperado);
        System.out.println("  ✔ " + campo + " é Boolean " + esperado + ": " + map.get(campo));
    }

    public static void validarString(Map<?, ?> map, String campo) {
        assertNotNull(map.get(campo), campo + " não deve ser null");
        assertInstanceOf(String.class, map.get(campo), campo + " deve ser String");
        assertFalse(map.get(campo).toString().isBlank(), campo + " não deve ser vazio");
        System.out.println("  ✔ " + campo + " é String não vazia: " + map.get(campo));
    }

    public static void validarInteiro(Map<?, ?> map, String campo) {
        assertNotNull(map.get(campo), campo + " não deve ser null");
        assertInstanceOf(Integer.class, map.get(campo), campo + " deve ser Integer");
        System.out.println("  ✔ " + campo + " é Integer: " + map.get(campo));
    }

    public static void validarInteiroPositivo(Map<?, ?> map, String campo) {
        validarInteiro(map, campo);
        int valor = (Integer) map.get(campo);
        assertTrue(valor > 0, campo + " deve ser > 0: " + valor);
        System.out.println("  ✔ " + campo + " é Integer positivo: " + valor);
    }

    public static void validarContentType(Response response) {
        String contentType = response.getContentType();
        assertTrue(contentType != null && contentType.contains("application/json"),
                "Content-Type deve ser application/json: " + contentType);
        System.out.println("  ✔ Content-Type: " + contentType);
    }

    public static void validarTempoResposta(Response response) {
        long responseTime = response.getTime();
        assertTrue(responseTime < 2000, "Tempo de resposta deve ser < 2000ms: " + responseTime + "ms");
        System.out.println("  ✔ Tempo de resposta: " + responseTime + "ms");
    }

    // senha sem prioridade: API retorna como String, mas o valor é numérico - valida e trata como Integer
    public static void validarSenhaSemPrioridade(Map<?, ?> map, String campo) {
        validarString(map, campo);
        String senhaStr = (String) map.get(campo);
        assertTrue(senhaStr.matches("\\d+"), campo + " deve conter apenas dígitos: " + senhaStr);
        int senhaNum = Integer.parseInt(senhaStr);
        assertTrue(senhaNum > 0, campo + " deve ser > 0: " + senhaNum);
        assertTrue(senhaStr.length() >= 1 && senhaStr.length() <= 10,
                campo + " deve ter entre 1 e 10 caracteres: " + senhaStr);
        System.out.println("  ✔ " + campo + " (String → Integer) válida, > 0, tamanho OK: " + senhaStr + " -> " + senhaNum);
    }

    // senha com prioridade: retorna com 1 letra prefixada (ex: "T4147") - trata parte numérica como Integer
    public static void validarSenhaComPrioridade(Map<?, ?> map, String campo) {
        validarString(map, campo);
        String senha = (String) map.get(campo);
        assertTrue(senha.matches("[A-Za-z]\\d+"),
                campo + " com prioridade deve iniciar com 1 letra seguida de dígitos: " + senha);
        assertTrue(senha.length() >= 2 && senha.length() <= 10,
                campo + " deve ter entre 2 e 10 caracteres: " + senha);
        int senhaNum = Integer.parseInt(senha.substring(1));
        assertTrue(senhaNum > 0, "parte numérica de " + campo + " deve ser > 0: " + senhaNum);
        System.out.println("  ✔ " + campo + " com prioridade válida (letra + dígitos, numérico > 0, tamanho OK): " + senha);
    }

    public static void validarEmissao(Map<?, ?> map, String campo) {
        validarString(map, campo);
        String emissao = (String) map.get(campo);
        assertTrue(emissao.matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}"),
                campo + " fora do formato MM/dd/yyyy HH:mm:ss: " + emissao);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
        LocalDateTime emissaoDate = assertDoesNotThrow(
                () -> LocalDateTime.parse(emissao, formatter),
                campo + " não é uma data válida: " + emissao);
        assertEquals(LocalDate.now(), emissaoDate.toLocalDate(),
                campo + " deve ser de hoje: " + emissao);
        assertFalse(emissaoDate.isAfter(LocalDateTime.now().plusMinutes(1)),
                campo + " não deve estar no futuro: " + emissao);
        assertFalse(emissaoDate.isBefore(LocalDateTime.now().minusMinutes(10)),
                campo + " deve ser recente (últimos 10 min): " + emissao);
        System.out.println("  ✔ " + campo + " válida, hoje, recente: " + emissao);
    }

    public static void validarQuantidade(List<?> lista, Integer quantidade, String label) {
        assertEquals(lista.size(), quantidade.intValue(),
                label + ": tamanho da lista (" + lista.size() + ") não confere com Quantidade (" + quantidade + ")");
        System.out.println("  ✔ Quantidade de " + label + " confere: " + quantidade);
    }

    public static void validarCamposItem(Map<?, ?> map, Set<String> camposEsperados, String label) {
        Set<String> ausentes = new HashSet<>(camposEsperados);
        ausentes.removeAll(map.keySet());
        assertTrue(ausentes.isEmpty(),
                label + " — campos ausentes na resposta: " + ausentes);
        var extras = new HashSet<>(map.keySet());
        extras.removeAll(camposEsperados);
        if (!extras.isEmpty()) {
            System.out.println("  ⚠ " + label + " contém campos novos: " + extras);
        }
    }

    public static void validarCorHex(Map<?, ?> map, String campo) {
        validarString(map, campo);
        String cor = (String) map.get(campo);
        assertTrue(cor.matches("^[0-9A-Fa-f]{6}$"), campo + " inválido (esperado hex 6 dígitos): " + cor);
        System.out.println("  ✔ " + campo + " em formato hex válido: " + cor);
    }

    public static void validarReferenciaExiste(List<Map<String, Object>> lista, String campoChave, Object valor, String label) {
        boolean existe = lista.stream().anyMatch(item -> item.get(campoChave).equals(valor));
        assertTrue(existe, label + " " + valor + " não existe na lista de referência");
        System.out.println("  ✔ " + label + " " + valor + " existe na lista de referência");
    }

    public static void validarCamposExatos(Map<?, ?> map, Set<String> camposEsperados) {
        relatorioCampos(map, camposEsperados);
        assertEquals(camposEsperados, new HashSet<>(map.keySet()),
                "result deve conter exatamente os campos esperados");
    }

    public static void validarStringNumerica(Map<?, ?> map, String campo) {
        validarString(map, campo);
        String valor = map.get(campo).toString();
        assertTrue(valor.matches("\\d+"), campo + " deve ser numérico: " + valor);
        System.out.println("  ✔ " + campo + " numérico válido: " + valor);
    }

    public static void validarHorario(Map<?, ?> map, String campo) {
        validarString(map, campo);
        String horario = map.get(campo).toString();
        assertTrue(horario.matches("\\d+:\\d{2}:\\d{2}"),
                campo + " fora do formato HH:mm:ss: " + horario);
        System.out.println("  ✔ " + campo + " (HH:mm:ss): " + horario);
    }

    public static void validarDataHoraBR(Map<?, ?> map, String campo) {
        validarString(map, campo);
        String data = map.get(campo).toString();
        assertTrue(data.matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}"),
                campo + " fora do formato dd/MM/yyyy HH:mm:ss: " + data);
        System.out.println("  ✔ " + campo + " (dd/MM/yyyy HH:mm:ss): " + data);
    }

    public static void validarIP(Map<?, ?> map, String campo) {
        validarString(map, campo);
        String ip = map.get(campo).toString();
        assertTrue(ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"),
                campo + " fora do formato IP: " + ip);
        System.out.println("  ✔ " + campo + " em formato IP válido: " + ip);
    }

    public static void validarURL(String valor, String protocolo) {
        assertFalse(valor.isBlank(), "URL não deve ser vazia");
        assertTrue(valor.startsWith(protocolo),
                "URL deve iniciar com " + protocolo + ": " + valor);
        System.out.println("  ✔ URL válida (" + protocolo + "): " + valor);
    }

    public static void relatorioCamposLista(List<Map<?, ?>> lista, Set<String> camposEsperados, String rotulo) {
        System.out.println("\n" + BOLD + "📋 Relatório de campos — " + rotulo + RESET);
        boolean houveExtras = false;
        for (Map<?, ?> item : lista) {
            var extras = new HashSet<>(item.keySet());
            extras.removeAll(camposEsperados);
            if (!extras.isEmpty()) {
                houveExtras = true;
                System.out.println("  ⚠ " + rotulo + " contém campos novos: " + extras);
            }
        }
        if (!houveExtras) {
            System.out.println("  ✔ Nenhum campo novo detectado em " + rotulo + ".");
        }
    }

    public static void validarChavesNovas(Set<String> chavesDetectadas, Set<String> chavesConhecidas, String rotulo) {
        Set<String> novas = new HashSet<>(chavesDetectadas);
        novas.removeAll(chavesConhecidas);
        System.out.println("\n" + BOLD + "📋 Relatório de chaves — " + rotulo + RESET);
        if (!novas.isEmpty()) {
            System.out.println("  ⚠ Chaves novas detectadas: " + novas);
        } else {
            System.out.println("  ✔ Nenhuma chave nova detectada.");
        }
    }

    public static void validarChavesNovas(Set<String> chavesDetectadas, java.nio.file.Path baseline, String rotulo) {
        try {
            Set<String> chavesConhecidas = new HashSet<>(java.nio.file.Files.readAllLines(baseline, java.nio.charset.StandardCharsets.UTF_8));
            chavesConhecidas.removeIf(String::isBlank);

            Set<String> novas = new HashSet<>(chavesDetectadas);
            novas.removeAll(chavesConhecidas);

            System.out.println("\n" + BOLD + "📋 Relatório de chaves — " + rotulo + RESET);
            if (!novas.isEmpty()) {
                chavesConhecidas.addAll(novas);
                java.util.List<String> linhas = new java.util.ArrayList<>(chavesConhecidas);
                java.util.Collections.sort(linhas);
                java.nio.file.Files.write(baseline, linhas, java.nio.charset.StandardCharsets.UTF_8);
                System.out.println("  ⚠ Chaves novas adicionadas ao baseline: " + novas);
            } else {
                System.out.println("  ✔ Nenhuma chave nova detectada.");
            }
        } catch (java.io.IOException e) {
            System.out.println("  ❌ Erro ao ler/escrever baseline: " + e.getMessage());
        }
    }

    public static void imprimirCabecalho(String titulo, String apiUrl) {
        System.out.println(BOLD + "\n=== " + titulo + " ===" + RESET);
        System.out.println(BOLD + "🔗 API testada: " + apiUrl + RESET);
        System.out.println("\n" + BOLD + "✅ Validações\n" + RESET);
    }

    public static void relatorioCampos(Map<?, ?> map, Set<String> camposEsperados) {
        System.out.println("\n" + BOLD + "📋 Relatório de mudanças" + RESET);
        var extras = new HashSet<>(map.keySet());
        extras.removeAll(camposEsperados);
        if (extras.isEmpty()) {
            System.out.println("✔ Nenhum campo novo detectado.");
        } else {
            System.out.println("⚠ Campos novos detectados: " + extras);
        }
    }
}

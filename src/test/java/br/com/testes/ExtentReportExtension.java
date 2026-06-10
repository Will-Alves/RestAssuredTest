package br.com.testes;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import org.junit.jupiter.api.extension.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

public class ExtentReportExtension
        implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

    private static final String FAVICON_SVG = buildFaviconDataUri();
    private static final String NAV_CSS     = loadResource("nav.css");
    private static final String BRANDING_JS = loadResource("branding.js");

    private static String loadResource(String name) {
        try (var in = ExtentReportExtension.class.getResourceAsStream("/report/" + name)) {
            if (in == null) return "";
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private static String buildFaviconDataUri() {
        String svg = loadResource("favicon.svg");
        if (svg.isBlank()) return "";
        String encoded = java.net.URLEncoder.encode(svg.strip(), StandardCharsets.UTF_8)
            .replace("+", "%20");
        return "data:image/svg+xml," + encoded;
    }

    private static final Pattern ANSI_ESCAPE = Pattern.compile("\\[[0-9;]*[a-zA-Z]");
    private static final Pattern NOISY_REQUEST_LINE = Pattern.compile(
        "(Proxy|Request params|Form params|Path params|Cookies|Multiparts|Body):[\\s\\t]*(<none>)?\\s*");

    private static final Map<String, ExtentTest> testMap    = new ConcurrentHashMap<>();
    private static final Map<String, ClassData>  reportMap  = new ConcurrentHashMap<>();

    private static final ThreadLocal<ByteArrayOutputStream> captureBuffer = new ThreadLocal<>();
    private static final ThreadLocal<PrintStream>           originalOut   = new ThreadLocal<>();
    private static final ThreadLocal<Long>                  startTime     = new ThreadLocal<>();

    // ── Per-class state ──────────────────────────────────────────────────────

    private static class ClassData {
        final String       className;
        final String       tempPath;
        ExtentReports      extent;
        final AtomicInteger totalTests      = new AtomicInteger(0);
        final AtomicInteger passedTests     = new AtomicInteger(0);
        final AtomicInteger failedTests     = new AtomicInteger(0);
        final AtomicInteger totalAssertions = new AtomicInteger(0);
        final AtomicLong    totalDuration   = new AtomicLong(0);
        volatile boolean    linkPrinted     = false;

        ClassData(String className) {
            this.className = className;
            this.tempPath  = "target/extent-" + className + "-tmp.html";

            ExtentSparkReporter spark = new ExtentSparkReporter(tempPath);
            spark.config().setTheme(Theme.DARK);
            spark.config().setDocumentTitle("Testes — " + className);
            spark.config().setReportName("Testes " + className);
            spark.config().setEncoding("UTF-8");
            spark.config().enableOfflineMode(true);
            spark.config().setCss(NAV_CSS);
            spark.config().setJs(BRANDING_JS);

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Projeto", "RestAssured Tests");
            extent.setSystemInfo("Ambiente", "Local");
        }

        synchronized void flush() {
            if (extent == null) return;
            int  total = totalTests.get();
            long avg   = total > 0 ? totalDuration.get() / total : 0;
            extent.setSystemInfo("Total de Testes",       String.valueOf(total));
            extent.setSystemInfo("Passaram",              String.valueOf(passedTests.get()));
            extent.setSystemInfo("Falharam",              String.valueOf(failedTests.get()));
            extent.setSystemInfo("Total de Assertions",   String.valueOf(totalAssertions.get()));
            extent.setSystemInfo("Duração Total",         String.format("%.3fs", totalDuration.get() / 1000.0));
            extent.setSystemInfo("Tempo Médio por Teste", String.format("%.3fs", avg / 1000.0));
            extent.flush();

            String finalPath = "target/" + className + ".html";
            patchHtml(tempPath, finalPath, className);

            if (!linkPrinted) {
                linkPrinted = true;
                String absPath = new File(finalPath).getAbsolutePath();
                String fileUrl = "file:///" + absPath.replace("\\", "/");
                System.out.println("\n  Relatório gerado: " + absPath + "\n");
                try { new ProcessBuilder("cmd", "/c", "start", "chrome", fileUrl).start(); }
                catch (Exception ignored) {}
            }
        }
    }

    private static ClassData classData(ExtensionContext ctx) {
        String name = ctx.getTestClass().map(Class::getSimpleName).orElse("Testes");
        return reportMap.computeIfAbsent(name, ClassData::new);
    }

    // ── Extension callbacks ───────────────────────────────────────────────────

    @Override
    public void beforeAll(ExtensionContext context) {
        classData(context); // ensures ClassData is created
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        startTime.set(System.currentTimeMillis());
        ClassData cd = classData(context);

        PrintStream original = System.out;
        originalOut.set(original);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        captureBuffer.set(baos);

        PrintStream tee = new PrintStream(baos, true, StandardCharsets.UTF_8) {
            @Override public void write(byte[] b, int off, int len) { super.write(b, off, len); original.write(b, off, len); }
            @Override public void write(int b)                       { super.write(b);           original.write(b);          }
        };
        System.setOut(tee);

        PrintStream dynamicOut = new PrintStream(new java.io.OutputStream() {
            public void write(byte[] b, int off, int len) { System.out.write(b, off, len); }
            public void write(int b) { System.out.write(b); }
            public void flush() { System.out.flush(); }
        }, true);

        io.restassured.config.RestAssuredConfig baseConfig = io.restassured.RestAssured.config != null
                ? io.restassured.RestAssured.config
                : io.restassured.config.RestAssuredConfig.config();
        io.restassured.RestAssured.config = baseConfig
            .logConfig(new io.restassured.config.LogConfig(dynamicOut, true));
        io.restassured.RestAssured.replaceFiltersWith(
            new io.restassured.filter.log.RequestLoggingFilter(dynamicOut),
            new io.restassured.filter.log.ResponseLoggingFilter(dynamicOut),
            (reqSpec, resSpec, ctx) -> {
                long t = System.currentTimeMillis();
                io.restassured.response.Response r = ctx.next(reqSpec, resSpec);
                dynamicOut.println("Response-Time-Ms: " + (System.currentTimeMillis() - t));
                return r;
            }
        );

        testMap.put(key(context), cd.extent.createTest(context.getDisplayName()));
        cd.totalTests.incrementAndGet();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        PrintStream original = originalOut.get();
        if (original != null) {
            System.setOut(original);
            original.println("\n===========================================\n");
        }

        long duration = System.currentTimeMillis() - startTime.get();
        ClassData cd = classData(context);
        cd.totalDuration.addAndGet(duration);

        ExtentTest test = testMap.get(key(context));
        if (test == null) return;

        boolean testPassed = !context.getExecutionException().isPresent();

        ByteArrayOutputStream baos = captureBuffer.get();
        if (baos != null) processOutput(test, baos.toString(StandardCharsets.UTF_8), testPassed, cd);

        if (!testPassed) {
            Throwable ex = context.getExecutionException().get();
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            test.log(Status.FAIL, "<b>" + esc(ex.getMessage()) + "</b>");
            test.log(Status.FAIL, "<pre>" + esc(sw.toString()) + "</pre>");
            cd.failedTests.incrementAndGet();
        } else {
            cd.passedTests.incrementAndGet();
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String inicio = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime.get()), ZoneId.systemDefault()).format(fmt);
        String fim    = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime.get() + duration), ZoneId.systemDefault()).format(fmt);
        test.log(Status.INFO, String.format(
            "<b>🕐 Início: %s &nbsp;|&nbsp; 🏁 Fim: %s &nbsp;|&nbsp; ⏱ Duração do teste: %.3fs</b>",
            inicio, fim, duration / 1000.0));
    }

    @Override
    public void afterAll(ExtensionContext context) {
        classData(context).flush();
    }

    // ── Output processing ─────────────────────────────────────────────────────

    private void processOutput(ExtentTest test, String rawIn, boolean testPassed, ClassData cd) {
        String raw      = ANSI_ESCAPE.matcher(rawIn).replaceAll("").replace("", "");
        int    splitIdx = raw.indexOf("\n===");
        String httpPart = splitIdx >= 0 ? raw.substring(0, splitIdx) : raw;
        String valPart  = splitIdx >= 0 ? raw.substring(splitIdx + 1) : "";
        processHttpBlock(test, httpPart, testPassed);
        processValidationBlock(test, valPart, cd);
    }

    private void processHttpBlock(ExtentTest test, String httpPart, boolean testPassed) {
        StringBuilder reqBlock   = new StringBuilder();
        StringBuilder resBody    = new StringBuilder();
        String        statusLine = null;
        long          responseMs = -1;
        boolean       inReqBody  = false;
        boolean       inResBody  = false;

        for (String rawLine : httpPart.split("\n")) {
            String trimmed = rawLine.trim();
            if (trimmed.startsWith("Request method:")) {
                inReqBody = true; inResBody = false;
            } else if (trimmed.matches("HTTP/\\d\\.\\d \\d{3}.*")) {
                if (reqBlock.length() > 0) {
                    test.log(Status.INFO, "<details><summary>📤 <b>Request</b></summary><pre style='font-size:12px'>"
                        + esc(reqBlock.toString()) + "</pre></details>");
                    reqBlock = new StringBuilder();
                }
                statusLine = trimmed;
                inReqBody = false; inResBody = false;
                continue;
            } else if (statusLine != null && !inResBody && trimmed.isEmpty()) {
                inResBody = true;
                continue;
            }

            if (trimmed.startsWith("Response-Time-Ms:")) {
                try { responseMs = Long.parseLong(trimmed.split(":")[1].trim()); } catch (Exception ignored) {}
            } else if (inReqBody && !trimmed.startsWith("HTTP/")) {
                if (!NOISY_REQUEST_LINE.matcher(trimmed).matches()) reqBlock.append(trimmed).append("\n");
            } else if (inResBody && !trimmed.isEmpty()) {
                resBody.append(rawLine.stripTrailing()).append("\n");
            }
        }

        if (reqBlock.length() > 0)
            test.log(Status.INFO, "<details><summary>📤 <b>Request</b></summary><pre style='font-size:12px'>"
                + esc(reqBlock.toString()) + "</pre></details>");
        if (statusLine != null) {
            int code = -1;
            Status httpStatus;
            try {
                code = Integer.parseInt(statusLine.split(" ")[1]);
                if (code >= 200 && code < 300)      httpStatus = Status.PASS;
                else if (code >= 300 && code < 400) httpStatus = Status.WARNING;
                else                                httpStatus = testPassed ? Status.PASS : Status.FAIL;
            } catch (Exception ignored) {
                httpStatus = testPassed ? Status.PASS : Status.FAIL;
            }
            String color   = (code >= 200 && code < 300) ? "#6bbf47"
                           : (code >= 300 && code < 400) ? "#e8a83a" : "#e8463c";
            String colored = "<span style='color:" + color + ";font-weight:bold'>" + esc(statusLine) + "</span>";
            test.log(httpStatus, "📥 " + colored);
        }
        if (resBody.length() > 0)
            test.log(Status.INFO, "<details><summary>📋 <b>Response Body</b></summary>"
                + "<pre style='font-size:12px;max-height:300px;overflow-y:auto;background:#0d1117;border:1px solid #30363d;"
                + "border-radius:6px;padding:12px 14px;line-height:1.7;color:#e6edf3;font-family:Consolas,monospace'>"
                + highlightJson(resBody.toString()) + "</pre></details>");
        if (responseMs >= 0) {
            if (responseMs > 1000)
                test.log(Status.WARNING, "<b>⚠ Tempo de resposta: " + responseMs + "ms (acima de 1000ms)</b>");
            else
                test.log(Status.INFO, "<b>⏱ Tempo de resposta: " + responseMs + "ms</b>");
        }
    }

    private void processValidationBlock(ExtentTest test, String valPart, ClassData cd) {
        for (String rawLine : valPart.split("\n")) {
            String trimmed = rawLine.trim();
            if (!trimmed.isEmpty()) addValidationLine(test, trimmed, cd);
        }
    }

    private void addValidationLine(ExtentTest test, String line, ClassData cd) {
        if (line.startsWith("===") || line.startsWith("🔗")) {
            test.log(Status.INFO, "<b>" + esc(line) + "</b>");
        } else if (line.contains("✔") || line.contains("✅")) {
            int colon = line.lastIndexOf(": ");
            String formatted = colon >= 0
                ? "<b>" + esc(line.substring(0, colon + 1)) + "</b>" + esc(line.substring(colon + 1))
                : "<b>" + esc(line) + "</b>";
            test.log(Status.PASS, formatted);
            cd.totalAssertions.incrementAndGet();
        } else if (line.contains("❌")) {
            test.log(Status.FAIL, esc(line));
        } else if (line.contains("⚠")) {
            test.log(Status.WARNING, esc(line));
        } else if (line.contains("📋")) {
            test.log(Status.INFO, "<b>" + esc(line) + "</b>");
        } else {
            test.log(Status.INFO, esc(line));
        }
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static void patchHtml(String sourcePath, String targetPath, String className) {
        try {
            java.nio.file.Path src  = java.nio.file.Paths.get(sourcePath);
            String             html = new String(java.nio.file.Files.readAllBytes(src), StandardCharsets.UTF_8);
            html = html.replaceAll("<link rel=\"apple-touch-icon\"[^>]*>", "");
            html = html.replaceAll("<link rel=\"shortcut icon\"[^>]*>", "");
            html = html.replace(
                "<span class=\"font-size-14\">Tests</span>",
                "<span style=\"font-size:17px!important;font-weight:600\">Testes " + className + "</span>");
            html = html.replaceAll(
                "(<span>\\d{2}:\\d{2}:\\d{2}</span> / <span>[^<]+</span>)\\s*(<span class=\"badge (?:pass|fail)-bg log) float-right\">([^<]+)</span>",
                "$2\">$3</span> $1");
            html = html.replace("</head>",
                "<link rel=\"icon\" type=\"image/svg+xml\" href=\"" + FAVICON_SVG + "\">\n</head>");
            java.nio.file.Path dest = java.nio.file.Paths.get(targetPath);
            java.nio.file.Files.write(dest, html.getBytes(StandardCharsets.UTF_8));
            java.nio.file.Files.deleteIfExists(src);
        } catch (Exception ignored) {}
    }

    private static String highlightJson(String json) {
        return esc(json)
            .replaceAll("(&quot;[^&]+&quot;)\\s*:", "<span style='color:#79c0ff'>$1</span>:")
            .replaceAll(":\\s*(&quot;[^&]*&quot;)", ": <span style='color:#a5d6a7'>$1</span>")
            .replaceAll(":\\s*(-?\\d+\\.?\\d*)", ": <span style='color:#f0883e'>$1</span>")
            .replaceAll(":\\s*(true|false)", ": <span style='color:#ff7b72'>$1</span>")
            .replaceAll(":\\s*(null)", ": <span style='color:#8b949e'>$1</span>");
    }

    private static String key(ExtensionContext ctx) { return ctx.getUniqueId(); }
}

package com.videep.chatlog;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/** Small dependency-free backend that serves the UI and chat-analysis API. */
public final class WebServer {
    private static final int DEFAULT_PORT = 8080;

    private WebServer() { }

    public static void main(String[] args) throws IOException {
        int port = parsePort(args);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", exchange -> serveAsset(exchange, "index.html", "text/html; charset=utf-8"));
        server.createContext("/styles.css", exchange -> serveAsset(exchange, "styles.css", "text/css; charset=utf-8"));
        server.createContext("/app.js", exchange -> serveAsset(exchange, "app.js", "application/javascript; charset=utf-8"));
        server.createContext("/api/analyze", WebServer::analyze);
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("Chat Log Analyzer is running at http://localhost:" + port);
    }

    private static int parsePort(String[] args) {
        if (args.length == 0) return DEFAULT_PORT;
        try {
            int port = Integer.parseInt(args[0]);
            if (port < 1 || port > 65535) throw new NumberFormatException();
            return port;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Port must be a number from 1 to 65535.");
        }
    }

    private static void analyze(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Allow", "POST");
            send(exchange, 405, "application/json", "{\"error\":\"Use POST\"}");
            return;
        }
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        ChatLogAnalyzer analyzer = new ChatLogAnalyzer();
        List<String> output = new ArrayList<>();
        for (String line : ChatLogNormalizer.normalize(body)) {
            output.add(analyzer.process(line).toJson());
        }
        send(exchange, 200, "application/json; charset=utf-8", "[" + String.join(",", output) + "]");
    }

    private static void serveAsset(HttpExchange exchange, String name, String contentType) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "text/plain; charset=utf-8", "Method not allowed");
            return;
        }
        try (InputStream asset = WebServer.class.getResourceAsStream("/static/" + name)) {
            if (asset == null) {
                send(exchange, 404, "text/plain; charset=utf-8", "Not found");
                return;
            }
            send(exchange, 200, contentType, new String(asset.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private static void send(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}

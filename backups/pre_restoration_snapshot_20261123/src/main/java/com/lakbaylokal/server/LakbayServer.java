package com.lakbaylokal.server;

import com.lakbaylokal.config.AppConfig;
import com.lakbaylokal.controller.ApiController;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Embedded HTTP server for Lakbay Lokal desktop application.
 * Serves the modern HTML5/JS/CSS frontend and dispatches REST API requests to ApiController.
 */
public class LakbayServer {

    private HttpServer server;
    private final ApiController apiController = new ApiController();

    public void start() throws IOException {
        int port = AppConfig.getServerPort();
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));

        // API Handler
        server.createContext("/api", new ApiHandler());

        // Static Files Handler
        server.createContext("/", new StaticFileHandler());

        server.start();
        System.out.println("[LakbayServer] Server running at http://localhost:" + port + "/");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("[LakbayServer] Server stopped.");
        }
    }

    public int getPort() {
        return server != null ? server.getAddress().getPort() : AppConfig.getServerPort();
    }

    private class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Auth-Token");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            URI uri = exchange.getRequestURI();
            String path = uri.getPath();
            String method = exchange.getRequestMethod();
            Map<String, String> queryParams = parseQuery(uri.getRawQuery());

            // Read Body
            String body = "";
            try (InputStream is = exchange.getRequestBody();
                 ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = is.read(buf)) != -1) {
                    os.write(buf, 0, len);
                }
                body = os.toString(StandardCharsets.UTF_8);
            }

            // Extract Auth Token
            String token = exchange.getRequestHeaders().getFirst("X-Auth-Token");
            if (token == null || token.isBlank()) {
                String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7).trim();
                }
            }

            String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();

            String response = apiController.handleRequest(path, method, queryParams, body, token, clientIp);

            int statusCode = 200;
            if (response.contains("\"success\":false") && response.contains("\"code\":")) {
                try {
                    int codeIdx = response.indexOf("\"code\":");
                    int commaIdx = response.indexOf(",", codeIdx);
                    int braceIdx = response.indexOf("}", codeIdx);
                    int endIdx = (commaIdx != -1 && commaIdx < braceIdx) ? commaIdx : braceIdx;
                    if (endIdx != -1) {
                        statusCode = Integer.parseInt(response.substring(codeIdx + 7, endIdx).trim());
                    }
                } catch (Exception ignored) {}
            }

            byte[] respBytes = response.getBytes(StandardCharsets.UTF_8);
            headers.add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(statusCode, respBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(respBytes);
            }
            exchange.close();
        }
    }

    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            URI uri = exchange.getRequestURI();
            String path = uri.getPath();

            if (path.equals("/") || path.isBlank()) {
                path = "/index.html";
            }

            // Look in file system resources first
            File webDir = new File("src/main/resources/web");
            File requestedFile = new File(webDir, path);

            byte[] content = null;
            if (requestedFile.exists() && requestedFile.isFile() && requestedFile.getCanonicalPath().startsWith(webDir.getCanonicalPath())) {
                content = Files.readAllBytes(requestedFile.toPath());
            } else {
                // Check if file exists in bin/web
                File binWebDir = new File("bin/web");
                File binFile = new File(binWebDir, path);
                if (binFile.exists() && binFile.isFile()) {
                    content = Files.readAllBytes(binFile.toPath());
                }

                if (content == null) {
                    // Fallback to classpath
                    String resourcePath = "web" + path;
                    try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                        if (is != null) {
                            content = is.readAllBytes();
                        }
                    }
                }
            }

            if (content != null) {
                String mime = getMimeType(path);
                exchange.getResponseHeaders().add("Content-Type", mime);
                exchange.sendResponseHeaders(200, content.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(content);
                }
            } else {
                String notFound = "<h1>404 Not Found</h1><p>Resource: " + path + "</p>";
                byte[] bytes = notFound.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
                exchange.sendResponseHeaders(404, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
            exchange.close();
        }
    }

    private String getMimeType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".css")) return "text/css; charset=utf-8";
        if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (path.endsWith(".json")) return "application/json; charset=utf-8";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isBlank()) return map;
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length > 0) {
                String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                String val = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
                map.put(key, val);
            }
        }
        return map;
    }
}

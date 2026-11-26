package com.lakbaylokal;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpApiTest {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        System.out.println("--- TESTING HTTP ENDPOINT http://localhost:8080/api/auth/login ---");

        // 1. Valid tourist_ana login
        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"tourist_ana\",\"password\":\"password123\"}"))
                .build();
        HttpResponse<String> res1 = client.send(req1, HttpResponse.BodyHandlers.ofString());
        System.out.println("HTTP Test 1 (Valid Login) Status: " + res1.statusCode());
        System.out.println("HTTP Test 1 Body: " + res1.body());

        // 2. Bad password
        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"tourist_ana\",\"password\":\"wrongpassword\"}"))
                .build();
        HttpResponse<String> res2 = client.send(req2, HttpResponse.BodyHandlers.ofString());
        System.out.println("\nHTTP Test 2 (Bad Password) Status: " + res2.statusCode());
        System.out.println("HTTP Test 2 Body: " + res2.body());

        // 3. Nonexistent user
        HttpRequest req3 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"unknown_user\",\"password\":\"password123\"}"))
                .build();
        HttpResponse<String> res3 = client.send(req3, HttpResponse.BodyHandlers.ofString());
        System.out.println("\nHTTP Test 3 (Nonexistent User) Status: " + res3.statusCode());
        System.out.println("HTTP Test 3 Body: " + res3.body());

        // 4. Test Web Assets Serving
        HttpRequest reqRoot = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/")).GET().build();
        HttpResponse<String> resRoot = client.send(reqRoot, HttpResponse.BodyHandlers.ofString());
        System.out.println("\nHTTP Web Root Status: " + resRoot.statusCode() + " (HTML length: " + resRoot.body().length() + ")");

        HttpRequest reqJs = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/js/app.js")).GET().build();
        HttpResponse<String> resJs = client.send(reqJs, HttpResponse.BodyHandlers.ofString());
        System.out.println("HTTP JS Asset Status: " + resJs.statusCode() + " (JS length: " + resJs.body().length() + ")");

        // 5. Test All 6 Roles via HTTP Login
        System.out.println("\n--- TESTING ALL 6 ROLES OVER HTTP ---");
        String[][] testRoles = {
            {"admin", "SYSTEM_ADMIN"},
            {"tourismoffice", "TOURISM_OFFICE"},
            {"lgu_naval", "LGU_OFFICER"},
            {"brgy_olog", "BARANGAY_OFFICIAL"},
            {"guide_pedro", "STAKEHOLDER"},
            {"tourist_ana", "TOURIST"}
        };
        boolean allHttpRolesOk = true;
        for (String[] r : testRoles) {
            HttpRequest rRole = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(String.format("{\"username\":\"%s\",\"password\":\"password123\"}", r[0])))
                    .build();
            HttpResponse<String> respRole = client.send(rRole, HttpResponse.BodyHandlers.ofString());
            boolean ok = respRole.body().contains("\"roleCode\":\"" + r[1] + "\"");
            System.out.println("HTTP Role " + r[0] + " -> " + r[1] + ": " + (ok ? "OK (200)" : "FAILED"));
            if (!ok) allHttpRolesOk = false;
        }

        if (res1.body().contains("\"success\":true") &&
            res1.body().contains("\"roleCode\":\"TOURIST\"") &&
            res2.body().contains("\"success\":false") &&
            res3.body().contains("\"success\":false") &&
            resRoot.statusCode() == 200 &&
            resJs.statusCode() == 200 &&
            allHttpRolesOk) {
            System.out.println("\n[SUCCESS] All HTTP API and static asset integration tests passed!");
        } else {
            System.err.println("\n[FAILURE] HTTP API integration tests failed.");
            System.exit(1);
        }
    }
}

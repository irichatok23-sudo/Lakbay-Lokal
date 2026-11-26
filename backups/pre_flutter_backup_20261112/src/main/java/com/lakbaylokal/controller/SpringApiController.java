package com.lakbaylokal.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Boot REST Controller for Lakbay Lokal.
 * Exposes all REST API endpoints for authentication, destinations, packages,
 * bookings, stakeholder services, verifications, analytics, and admin operations.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SpringApiController {

    private final ApiController apiController = new ApiController();

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> dispatch(
            HttpServletRequest request,
            @RequestBody(required = false) String body
    ) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Extract Query Parameters
        Map<String, String> queryParams = new HashMap<>();
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isBlank()) {
            for (String param : queryString.split("&")) {
                String[] pair = param.split("=", 2);
                if (pair.length > 0) {
                    String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                    String val = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
                    queryParams.put(key, val);
                }
            }
        }

        // Extract Auth Token
        String token = request.getHeader("X-Auth-Token");
        if (token == null || token.isBlank()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7).trim();
            }
        }

        String clientIp = request.getRemoteAddr();
        String requestBody = body != null ? body : "";

        // Dispatch to business controller
        String responseJson = apiController.handleRequest(path, method, queryParams, requestBody, token, clientIp);

        // Determine HTTP Status code based on response JSON if error
        HttpStatus status = HttpStatus.OK;
        if (responseJson.contains("\"success\":false") && responseJson.contains("Unauthorized")) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (responseJson.contains("\"success\":false") && responseJson.contains("required")) {
            status = HttpStatus.FORBIDDEN;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(responseJson, headers, status);
    }
}

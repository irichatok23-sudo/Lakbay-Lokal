package com.lakbaylokal.util;

import com.lakbaylokal.model.User;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe session management for authenticated users and RBAC tokens.
 */
public class SessionManager {

    public static class SessionData {
        private final String token;
        private final User user;
        private final long createdAt;
        private long lastAccessedAt;

        public SessionData(String token, User user) {
            this.token = token;
            this.user = user;
            this.createdAt = System.currentTimeMillis();
            this.lastAccessedAt = this.createdAt;
        }

        public String getToken() { return token; }
        public User getUser() { return user; }
        public long getCreatedAt() { return createdAt; }
        public long getLastAccessedAt() { return lastAccessedAt; }
        public void touch() { this.lastAccessedAt = System.currentTimeMillis(); }
    }

    private static final Map<String, SessionData> activeSessions = new ConcurrentHashMap<>();
    private static final long SESSION_TIMEOUT_MS = 24 * 60 * 60 * 1000; // 24 hours

    public static String createSession(User user) {
        String token = UUID.randomUUID().toString();
        activeSessions.put(token, new SessionData(token, user));
        return token;
    }

    public static User getUser(String token) {
        if (token == null || token.isBlank()) return null;
        SessionData session = activeSessions.get(token);
        if (session == null) return null;

        if (System.currentTimeMillis() - session.getLastAccessedAt() > SESSION_TIMEOUT_MS) {
            activeSessions.remove(token);
            return null;
        }
        session.touch();
        return session.getUser();
    }

    public static boolean invalidate(String token) {
        if (token != null) {
            return activeSessions.remove(token) != null;
        }
        return false;
    }

    public static boolean hasRole(String token, String roleCode) {
        User user = getUser(token);
        if (user == null) return false;
        if ("SYSTEM_ADMIN".equalsIgnoreCase(user.getRoleCode())) {
            return true; // Admin has superuser authority across all roles
        }
        return roleCode.equalsIgnoreCase(user.getRoleCode());
    }
}

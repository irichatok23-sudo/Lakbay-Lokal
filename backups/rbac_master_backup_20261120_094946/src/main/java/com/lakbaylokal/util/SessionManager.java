
package com.lakbaylokal.util;

import com.lakbaylokal.dao.UserDAO;
import com.lakbaylokal.dao.impl.UserDAOImpl;
import com.lakbaylokal.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe session management for authenticated users and RBAC tokens,
 * with disk-backed session recovery across backend restarts.
 */
public class SessionManager {

    public static class SessionData {
        private final String token;
        private final User user;
        private final long createdAt;
        private long lastAccessedAt;

        public SessionData(String token, User user) {
            this(token, user, System.currentTimeMillis());
        }

        public SessionData(String token, User user, long lastAccessedAt) {
            this.token = token;
            this.user = user;
            this.createdAt = System.currentTimeMillis();
            this.lastAccessedAt = lastAccessedAt;
        }

        public String getToken() { return token; }
        public User getUser() { return user; }
        public long getCreatedAt() { return createdAt; }
        public long getLastAccessedAt() { return lastAccessedAt; }
        public void touch() { this.lastAccessedAt = System.currentTimeMillis(); }
    }

    private static final Map<String, SessionData> activeSessions = new ConcurrentHashMap<>();
    private static final long SESSION_TIMEOUT_MS = 24 * 60 * 60 * 1000; // 24 hours
    private static final File SESSION_STORE_FILE = new File("backups/.active_sessions.properties");
    private static final UserDAO userDAO = new UserDAOImpl();

    public static String createSession(User user) {
        String token = UUID.randomUUID().toString();
        SessionData data = new SessionData(token, user);
        activeSessions.put(token, data);
        saveSessionStore();
        return token;
    }

    public static User getUser(String token) {
        if (token == null || token.isBlank()) return null;
        SessionData session = activeSessions.get(token);
        if (session == null) {
            session = tryRecoverSession(token);
        }
        if (session == null) return null;

        if (System.currentTimeMillis() - session.getLastAccessedAt() > SESSION_TIMEOUT_MS) {
            activeSessions.remove(token);
            saveSessionStore();
            return null;
        }
        session.touch();
        return session.getUser();
    }

    public static boolean invalidate(String token) {
        if (token != null) {
            boolean removed = activeSessions.remove(token) != null;
            saveSessionStore();
            return removed;
        }
        return false;
    }

    public static void invalidateUserSessions(int userId) {
        activeSessions.entrySet().removeIf(e -> e.getValue().getUser() != null && e.getValue().getUser().getUserId() == userId);
        saveSessionStore();
    }

    public static boolean hasRole(String token, String roleCode) {
        User user = getUser(token);
        if (user == null) return false;
        if ("SYSTEM_ADMIN".equalsIgnoreCase(user.getRoleCode())) {
            return true; // Admin has superuser authority across all roles
        }
        return roleCode.equalsIgnoreCase(user.getRoleCode());
    }

    private static synchronized SessionData tryRecoverSession(String token) {
        if (!SESSION_STORE_FILE.exists()) return null;
        try (FileInputStream fis = new FileInputStream(SESSION_STORE_FILE)) {
            Properties props = new Properties();
            props.load(fis);
            String val = props.getProperty(token);
            if (val != null && val.contains(":")) {
                String[] parts = val.split(":");
                int userId = Integer.parseInt(parts[0]);
                long lastAccess = Long.parseLong(parts[1]);
                if (System.currentTimeMillis() - lastAccess <= SESSION_TIMEOUT_MS) {
                    User u = userDAO.findById(userId);
                    if (u != null && "ACTIVE".equalsIgnoreCase(u.getStatus())) {
                        SessionData recovered = new SessionData(token, u, lastAccess);
                        activeSessions.put(token, recovered);
                        return recovered;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static synchronized void saveSessionStore() {
        try {
            File parent = SESSION_STORE_FILE.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            Properties props = new Properties();
            for (Map.Entry<String, SessionData> entry : activeSessions.entrySet()) {
                SessionData sd = entry.getValue();
                if (sd.getUser() != null) {
                    props.setProperty(entry.getKey(), sd.getUser().getUserId() + ":" + sd.getLastAccessedAt());
                }
            }
            try (FileOutputStream fos = new FileOutputStream(SESSION_STORE_FILE)) {
                props.store(fos, "Lakbay Lokal Active Sessions");
            }
        } catch (Exception ignored) {
        }
    }
}

package lakbaylokal.authentication;

import lakbaylokal.model.User;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static User currentUser;
    private static final Map<String, User> sessionTokens = new ConcurrentHashMap<>();

    public static synchronized void setCurrentUser(User user) {
        currentUser = user;
    }

    public static synchronized User getCurrentUser() {
        return currentUser;
    }

    public static String createSession(User user) {
        setCurrentUser(user);
        String token = UUID.randomUUID().toString();
        sessionTokens.put(token, user);
        return token;
    }

    public static User getUser(String token) {
        if (token == null) return currentUser;
        return sessionTokens.getOrDefault(token, currentUser);
    }

    public static void clearSession() {
        currentUser = null;
        sessionTokens.clear();
    }

    public static void logout() {
        clearSession();
    }

    public static boolean hasRole(String roleCode) {
        if (currentUser == null) return false;
        if ("SYSTEM_ADMIN".equalsIgnoreCase(currentUser.getRoleCode())) return true;
        return roleCode.equalsIgnoreCase(currentUser.getRoleCode());
    }
}

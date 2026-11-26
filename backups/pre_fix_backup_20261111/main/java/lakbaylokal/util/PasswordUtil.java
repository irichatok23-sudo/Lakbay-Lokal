package lakbaylokal.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateSalt() {
        byte[] saltBytes = new byte[16];
        RANDOM.nextBytes(saltBytes);
        return bytesToHex(saltBytes);
    }

    public static String hashPassword(String password, String salt) {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("Password and salt must not be null");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String combined = password + salt;
            byte[] hashBytes = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in JVM", e);
        }
    }

    public static boolean verifyPassword(String rawPassword, String salt, String expectedHash) {
        if (rawPassword == null || salt == null || expectedHash == null) {
            return false;
        }
        String normalizedSalt = salt.trim();
        String normalizedExpected = expectedHash.trim().toLowerCase();

        String calculated = hashPassword(rawPassword, normalizedSalt).toLowerCase();
        if (MessageDigest.isEqual(
                calculated.getBytes(StandardCharsets.UTF_8),
                normalizedExpected.getBytes(StandardCharsets.UTF_8)
        )) {
            return true;
        }

        String trimmedPassword = rawPassword.trim();
        if (!trimmedPassword.equals(rawPassword)) {
            String trimmedCalculated = hashPassword(trimmedPassword, normalizedSalt).toLowerCase();
            return MessageDigest.isEqual(
                    trimmedCalculated.getBytes(StandardCharsets.UTF_8),
                    normalizedExpected.getBytes(StandardCharsets.UTF_8)
            );
        }
        return false;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

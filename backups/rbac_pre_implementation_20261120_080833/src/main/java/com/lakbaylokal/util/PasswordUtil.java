package com.lakbaylokal.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

/**
 * Utility for secure salted password hashing.
 * Supports adaptive PBKDF2-HMAC-SHA256 (65,536 iterations) for strong key stretching,
 * while maintaining backward compatibility and automatic migration for legacy SHA-256 hashes.
 */
public class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PBKDF2_PREFIX = "pbkdf2$";
    private static final int PBKDF2_ITERATIONS = 65536;
    private static final int PBKDF2_KEY_BITS = 256;

    /**
     * Generates a 16-byte random hexadecimal salt.
     */
    public static String generateSalt() {
        byte[] saltBytes = new byte[16];
        RANDOM.nextBytes(saltBytes);
        return bytesToHex(saltBytes);
    }

    /**
     * Computes the legacy SHA-256 hash of password + salt (retained for seed verification compatibility).
     */
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

    /**
     * Computes an adaptive PBKDF2WithHmacSHA256 password hash (format: pbkdf2$iterations$hex).
     */
    public static String hashPasswordAdaptive(String password, String salt) {
        return hashPasswordPbkdf2(password, salt, PBKDF2_ITERATIONS);
    }

    private static String hashPasswordPbkdf2(String password, String salt, int iterations) {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("Password and salt must not be null");
        }
        try {
            KeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    salt.trim().getBytes(StandardCharsets.UTF_8),
                    iterations,
                    PBKDF2_KEY_BITS
            );
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hashBytes = factory.generateSecret(spec).getEncoded();
            return PBKDF2_PREFIX + iterations + "$" + bytesToHex(hashBytes);
        } catch (Exception e) {
            return hashPassword(password, salt);
        }
    }

    /**
     * Returns true if a stored password hash is still in legacy single-pass SHA-256 format
     * and should be transparently upgraded to PBKDF2-HMAC-SHA256 upon successful login.
     */
    public static boolean needsUpgrade(String storedHash) {
        return storedHash == null || !storedHash.trim().toLowerCase().startsWith(PBKDF2_PREFIX);
    }

    /**
     * Constant-time comparison to verify a provided password against a known salt and hash
     * (supports both PBKDF2-HMAC-SHA256 and legacy SHA-256 hashes).
     */
    public static boolean verifyPassword(String rawPassword, String salt, String expectedHash) {
        if (rawPassword == null || salt == null || expectedHash == null) {
            return false;
        }
        String normalizedSalt = salt.trim();
        String normalizedExpected = expectedHash.trim().toLowerCase();

        if (verifySingle(rawPassword, normalizedSalt, normalizedExpected)) {
            return true;
        }

        String trimmedPassword = rawPassword.trim();
        if (!trimmedPassword.equals(rawPassword)) {
            return verifySingle(trimmedPassword, normalizedSalt, normalizedExpected);
        }
        return false;
    }

    private static boolean verifySingle(String password, String normalizedSalt, String normalizedExpected) {
        if (normalizedExpected.startsWith(PBKDF2_PREFIX)) {
            String[] parts = normalizedExpected.split("\\$");
            if (parts.length == 3) {
                try {
                    int iterations = Integer.parseInt(parts[1]);
                    String computed = hashPasswordPbkdf2(password, normalizedSalt, iterations).toLowerCase();
                    return MessageDigest.isEqual(
                            computed.getBytes(StandardCharsets.UTF_8),
                            normalizedExpected.getBytes(StandardCharsets.UTF_8)
                    );
                } catch (NumberFormatException ignored) {
                    return false;
                }
            }
            return false;
        }

        String calculated = hashPassword(password, normalizedSalt).toLowerCase();
        return MessageDigest.isEqual(
                calculated.getBytes(StandardCharsets.UTF_8),
                normalizedExpected.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

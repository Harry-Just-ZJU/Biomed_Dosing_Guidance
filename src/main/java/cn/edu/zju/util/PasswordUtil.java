package cn.edu.zju.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Simple password hashing utility using SHA-256 + random salt.
 * Format stored: "SALT$HASH" (both Base64-encoded).
 *
 * We avoid adding a BCrypt dependency to keep pom.xml minimal.
 * SHA-256 with a 32-byte random salt is sufficient for this use case.
 */
public class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SALT_BYTES = 32;

    /** Hash a plain-text password. Returns "SALT_B64$HASH_B64". */
    public static String hash(String plain) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = sha256(salt, plain);
        return Base64.getEncoder().encodeToString(salt)
                + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /** Verify plain-text password against a stored hash string. */
    public static boolean verify(String plain, String stored) {
        if (plain == null || stored == null) return false;
        String[] parts = stored.split("\\$", 2);
        if (parts.length != 2) return false;
        try {
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expected = Base64.getDecoder().decode(parts[1]);
            byte[] actual   = sha256(salt, plain);
            // Constant-time comparison to prevent timing attacks
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] sha256(byte[] salt, String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            md.update(plain.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}

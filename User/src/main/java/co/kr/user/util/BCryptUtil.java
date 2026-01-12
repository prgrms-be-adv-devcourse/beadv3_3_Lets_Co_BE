package co.kr.user.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

/**
 * The BCryptUtil class provides methods for securely encrypting passwords using
 * Base64 encoding combined with BCrypt hashing and verifying passwords.
 */
@Component
public class BCryptUtil {

    @Autowired
    Base64Util base64Util;

    /**
     * Encrypts the given password using Base64 encoding and BCrypt hashing.
     *
     * This method first encodes the password using Base64, ensuring an additional layer
     * of obfuscation, and then hashes it using BCrypt to store securely in the database.
     *
     * @param password The plain text password to be encrypted.
     * @return The encrypted password as a BCrypt hash.
     */
    public String setPassword(String password) {
        // Base64 encoding of the password
        String basePW = base64Util.encode(password);
        // BCrypt hashing
        return BCrypt.hashpw(basePW, BCrypt.gensalt());
    }

    /**
     * Verifies if the provided plain text password matches the hashed password stored in the database.
     *
     * This method ensures that the password provided by the user is valid by applying
     * the same Base64 encoding and comparing it against the stored BCrypt hash.
     *
     * @param password The plain text password to be verified.
     * @param dbPassword The hashed password stored in the database.
     * @return true if the provided password matches the stored password, false otherwise.
     */
    public boolean checkPassword(String password, String dbPassword) {
        // Base64 encoding of the password
        String basePW = base64Util.encode(password);
        // BCrypt password verification
        return BCrypt.checkpw(basePW, dbPassword);
    }
}
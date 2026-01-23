package se.sven.nhldataservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.function.Function;

/**
 * JWT token generation and validation utilities.
  * Configuration:
 * - jwt.secret: Set via JWT_SECRET environment variable (minimum 32 characters required)
 * - jwt.expiration: Token expiration time in milliseconds (default: 1 hour)
 * Security notes:
 * - Never commit secrets to version control
 * - Use .env file for local development
 * - Use GitHub Secrets for CI/CD
 * - Use AWS Secrets Manager or similar for production deployment
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:3600000}")
    private long jwtExpiration;

    /**
     * Validates that the JWT secret key meets security requirements.
     * Application will fail to start if secret is too short.
     */
    @PostConstruct
    public void validateSecretKey() {
        if (secretKey == null || secretKey.length() < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 32 characters. " +
                            "Set jwt.secret in application.properties or JWT_SECRET environment variable");
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * Generates a JWT token for the specified username.
     *
     * @param username the username to include in the token
     * @return JWT token as a String
     * @throws IllegalArgumentException if username is null or empty
     */
    public String generateToken(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(Instant.now()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the username from a JWT token.
     *
     * @param token the JWT token
     * @return the username stored in the token
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from a JWT token.
     *
     * @param token the JWT token
     * @param claimsResolver function to extract the desired claim
     * @param <T> the type of the claim
     * @return the extracted claim value
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Validates a JWT token against the provided username.
     * Checks both token signature/format and expiration.
     *
     * @param token the JWT token to validate
     * @param username the username to validate against
     * @return true if token is valid and matches username, false otherwise
     */
    public boolean validateToken(String token, String username) {
        if (token == null || username == null) {
            return false;
        }

        try {
            final String tokenUsername = getUsernameFromToken(token);
            return (tokenUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            // Invalid token format, expired, or tampered
            return false;
        }
    }
}
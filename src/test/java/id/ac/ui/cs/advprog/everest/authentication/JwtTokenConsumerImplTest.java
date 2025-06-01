package id.ac.ui.cs.advprog.everest.authentication;

import id.ac.ui.cs.advprog.everest.authentication.exception.AuthenticationException;
import io.jsonwebtoken.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Lenient tests
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtTokenConsumerImplTest {

    @Mock
    private PublicKeyProvider publicKeyProvider;

    @Mock
    private Clock clock;

    private JwtTokenConsumerImpl jwtTokenConsumer;
    private Key privateKey;
    private Instant fixedInstant;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        // Generate RSA key pair for testing
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        Key publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();

        // Fixed time for consistent testing
        fixedInstant = Instant.parse("2024-01-01T12:00:00Z");

        when(publicKeyProvider.getPublicKey()).thenReturn(publicKey);
        when(clock.instant()).thenReturn(fixedInstant);

        jwtTokenConsumer = new JwtTokenConsumerImpl(publicKeyProvider, clock);
    }

    @Test
    void constructor_WithValidParameters_ShouldCreateInstance() {
        assertNotNull(jwtTokenConsumer);
    }

    @Test
    void constructor_WithNullPublicKeyProvider_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new JwtTokenConsumerImpl(null, clock));
    }

    @Test
    void constructor_WithNullClock_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new JwtTokenConsumerImpl(publicKeyProvider, null));
    }

    @Test
    void getEmailFromToken_WithValidToken_ShouldReturnEmail() {
        String email = "test@example.com";
        String token = createValidToken(email, UUID.randomUUID(), "access");

        String result = jwtTokenConsumer.getEmailFromToken(token);

        assertEquals(email, result);
    }

    @Test
    void getEmailFromToken_WithInvalidToken_ShouldThrowAuthenticationException() {
        String invalidToken = "invalid.token.here";

        assertThrows(AuthenticationException.class, () ->
                jwtTokenConsumer.getEmailFromToken(invalidToken));
    }

    @Test
    void getEmailFromToken_WithExpiredToken_ShouldThrowAuthenticationException() {
        String email = "test@example.com";
        String expiredToken = createExpiredToken(email, UUID.randomUUID());

        assertThrows(AuthenticationException.class, () ->
                jwtTokenConsumer.getEmailFromToken(expiredToken));
    }

    @Test
    void getUserIdFromToken_WithValidToken_ShouldReturnUserId() {
        String email = "test@example.com";
        UUID userId = UUID.randomUUID();
        String token = createValidToken(email, userId, "access");

        UUID result = jwtTokenConsumer.getUserIdFromToken(token);

        assertEquals(userId, result);
    }

    @Test
    void getUserIdFromToken_WithInvalidUuidFormat_ShouldThrowAuthenticationException() {
        String token = createTokenWithInvalidUserId();

        AuthenticationException exception = assertThrows(AuthenticationException.class, () ->
                jwtTokenConsumer.getUserIdFromToken(token));

        assertEquals("Invalid user ID format in token", exception.getMessage());
        assertNotNull(exception.getCause());
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    void getTokenType_WithValidToken_ShouldReturnTokenType() {
        String email = "test@example.com";
        UUID userId = UUID.randomUUID();
        String tokenType = "refresh";
        String token = createValidToken(email, userId, tokenType);

        String result = jwtTokenConsumer.getTokenType(token);

        assertEquals(tokenType, result);
    }

    @Test
    void getClaimFromToken_WithValidClaimNameAndType_ShouldReturnClaim() {
        String email = "test@example.com";
        UUID userId = UUID.randomUUID();
        String token = createValidToken(email, userId, "access");

        String result = jwtTokenConsumer.getClaimFromToken(token, "type", String.class);

        assertEquals("access", result);
    }

    @Test
    void getClaimFromToken_WithValidClaimNameAndType_ForUserId_ShouldReturnUserId() {
        String email = "test@example.com";
        UUID userId = UUID.randomUUID();
        String token = createValidToken(email, userId, "access");

        String result = jwtTokenConsumer.getClaimFromToken(token, "userId", String.class);

        assertEquals(userId.toString(), result);
    }

    @Test
    void getClaimFromToken_WithClaimsResolver_ShouldReturnResolvedValue() {
        String email = "test@example.com";
        UUID userId = UUID.randomUUID();
        String token = createValidToken(email, userId, "access");

        Function<Claims, String> resolver = Claims::getSubject;
        String result = jwtTokenConsumer.getClaimFromToken(token, resolver);

        assertEquals(email, result);
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        String token = createValidToken("test@example.com", UUID.randomUUID(), "access");

        boolean result = jwtTokenConsumer.validateToken(token);

        assertTrue(result);
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        String expiredToken = createExpiredToken("test@example.com", UUID.randomUUID());

        boolean result = jwtTokenConsumer.validateToken(expiredToken);

        assertFalse(result);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        String invalidToken = "invalid.token.here";

        boolean result = jwtTokenConsumer.validateToken(invalidToken);

        assertFalse(result);
    }

    @Test
    void validateToken_WithNullToken_ShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                jwtTokenConsumer.validateToken(null));
    }

    @Test
    void validateToken_WithEmptyToken_ShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                jwtTokenConsumer.validateToken(""));
    }

    @Test
    void validateToken_WithBlankToken_ShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                jwtTokenConsumer.validateToken("   "));
    }

    @Test
    void getExpirationDateFromToken_WithValidToken_ShouldReturnExpirationDate() {
        String token = createValidToken("test@example.com", UUID.randomUUID(), "access");

        Date result = jwtTokenConsumer.getExpirationDateFromToken(token);

        assertNotNull(result);
        assertTrue(result.after(Date.from(fixedInstant)));
    }

    @Test
    void getExpirationDateFromToken_WithInvalidToken_ShouldThrowAuthenticationException() {
        String invalidToken = "invalid.token.here";

        assertThrows(AuthenticationException.class, () ->
                jwtTokenConsumer.getExpirationDateFromToken(invalidToken));
    }

    @Test
    void getAllClaimsFromToken_WithNullToken_ShouldThrowIllegalArgumentException() {
        // This tests the private method indirectly through public methods
        assertThrows(IllegalArgumentException.class, () ->
                jwtTokenConsumer.getEmailFromToken(null));
    }

    @Test
    void getAllClaimsFromToken_WithEmptyToken_ShouldThrowIllegalArgumentException() {
        // This tests the private method indirectly through public methods
        assertThrows(IllegalArgumentException.class, () ->
                jwtTokenConsumer.getEmailFromToken(""));
    }

    @Test
    void getAllClaimsFromToken_WithJwtException_ShouldThrowAuthenticationException() {
        // Create a token with wrong signature
        String malformedToken = createTokenWithWrongSignature(UUID.randomUUID());

        AuthenticationException exception = assertThrows(AuthenticationException.class, () ->
                jwtTokenConsumer.getEmailFromToken(malformedToken));

        assertEquals("Invalid token", exception.getMessage());
        assertNotNull(exception.getCause());
        assertInstanceOf(JwtException.class, exception.getCause());
    }

    @Test
    void isTokenExpired_WithNullExpiration_ShouldReturnFalse() {
        // Create token without expiration
        String tokenWithoutExpiration = createTokenWithoutExpiration(UUID.randomUUID());

        boolean result = jwtTokenConsumer.validateToken(tokenWithoutExpiration);

        assertTrue(result); // Token is valid (no expiration means not expired)
    }

    // Helper methods for creating test tokens

    private String createValidToken(String email, UUID userId, String tokenType) {
        Date expiration = Date.from(fixedInstant.plusSeconds(3600)); // 1 hour from now

        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId.toString())
                .claim("type", tokenType)
                .setExpiration(expiration)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    private String createExpiredToken(String email, UUID userId) {
        Date expiration = Date.from(fixedInstant.minusSeconds(3600)); // 1 hour ago

        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId.toString())
                .claim("type", "access")
                .setExpiration(expiration)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    private String createTokenWithInvalidUserId() {
        Date expiration = Date.from(fixedInstant.plusSeconds(3600));

        return Jwts.builder()
                .setSubject("test@example.com")
                .claim("userId", "invalid-uuid")
                .claim("type", "access")
                .setExpiration(expiration)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    private String createTokenWithWrongSignature(UUID userId) {
        Date expiration = Date.from(fixedInstant.plusSeconds(3600));

        // Create a different key pair for wrong signature
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair wrongKeyPair = keyGen.generateKeyPair();

            return Jwts.builder()
                    .setSubject("test@example.com")
                    .claim("userId", userId.toString())
                    .claim("type", "access")
                    .setExpiration(expiration)
                    .signWith(wrongKeyPair.getPrivate(), SignatureAlgorithm.RS256)
                    .compact();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String createTokenWithoutExpiration(UUID userId) {
        return Jwts.builder()
                .setSubject("test@example.com")
                .claim("userId", userId.toString())
                .claim("type", "access")
                // No expiration set
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }
}
package id.ac.ui.cs.advprog.everest.authentication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("PublicKeyProviderImpl Tests")
class PublicKeyProviderImplTest {

    @Mock
    private JwtProperties jwtProperties;

    private String validBase64PublicKey;
    private String validPemPublicKey;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Generate a real RSA key pair for testing
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();

        // Create valid Base64 encoded public key
        byte[] publicKeyBytes = publicKey.getEncoded();
        validBase64PublicKey = Base64.getEncoder().encodeToString(publicKeyBytes);

        // Create valid PEM formatted public key (Base64 encoded PEM)
        String pemContent = "-----BEGIN PUBLIC KEY-----\n" +
                validBase64PublicKey + "\n" +
                "-----END PUBLIC KEY-----";
        validPemPublicKey = Base64.getEncoder().encodeToString(pemContent.getBytes());
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should successfully create instance with valid Base64 public key")
        void shouldCreateInstanceWithValidBase64Key() {
            // Given
            when(jwtProperties.getPublicKey()).thenReturn(validBase64PublicKey);

            // When
            PublicKeyProviderImpl provider = new PublicKeyProviderImpl(jwtProperties);

            // Then
            assertNotNull(provider);
            Key key = provider.getPublicKey();
            assertNotNull(key);
            assertInstanceOf(RSAPublicKey.class, key);
        }

        @Test
        @DisplayName("Should successfully create instance with valid PEM formatted public key")
        void shouldCreateInstanceWithValidPemKey() {
            // Given
            when(jwtProperties.getPublicKey()).thenReturn(validPemPublicKey);

            // When
            PublicKeyProviderImpl provider = new PublicKeyProviderImpl(jwtProperties);

            // Then
            assertNotNull(provider);
            Key key = provider.getPublicKey();
            assertNotNull(key);
            assertInstanceOf(RSAPublicKey.class, key);
        }

        @Test
        @DisplayName("Should throw NullPointerException when JwtProperties is null")
        void shouldThrowExceptionWhenJwtPropertiesIsNull() {
            // When & Then
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> new PublicKeyProviderImpl(null)
            );
            assertEquals("JwtProperties must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NullPointerException when public key string is null")
        void shouldThrowExceptionWhenPublicKeyIsNull() {
            // Given
            when(jwtProperties.getPublicKey()).thenReturn(null);

            // When & Then
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> new PublicKeyProviderImpl(jwtProperties)
            );
            assertEquals("JWT publicKey must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when public key string is empty")
        void shouldThrowExceptionWhenPublicKeyIsEmpty() {
            // Given
            when(jwtProperties.getPublicKey()).thenReturn("");

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new PublicKeyProviderImpl(jwtProperties)
            );
            assertTrue(exception.getMessage().contains("JWT publicKey must not be empty"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when public key string is whitespace only")
        void shouldThrowExceptionWhenPublicKeyIsWhitespaceOnly() {
            // Given
            when(jwtProperties.getPublicKey()).thenReturn("   \t\n   ");

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new PublicKeyProviderImpl(jwtProperties)
            );
            assertTrue(exception.getMessage().contains("JWT publicKey must not be empty"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when public key is invalid Base64")
        void shouldThrowExceptionWhenPublicKeyIsInvalidBase64() {
            // Given
            when(jwtProperties.getPublicKey()).thenReturn("invalid-base64-string!");

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new PublicKeyProviderImpl(jwtProperties)
            );
            assertTrue(exception.getMessage().startsWith("Invalid JWT public key:"));
            assertNotNull(exception.getCause());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when decoded content is not valid key")
        void shouldThrowExceptionWhenDecodedContentIsNotValidKey() {
            // Given - Valid Base64 but invalid key content
            String invalidKeyContent = Base64.getEncoder().encodeToString("not a valid key".getBytes());
            when(jwtProperties.getPublicKey()).thenReturn(invalidKeyContent);

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new PublicKeyProviderImpl(jwtProperties)
            );
            assertTrue(exception.getMessage().startsWith("Invalid JWT public key:"));
            assertNotNull(exception.getCause());
        }

        @Test
        @DisplayName("Should handle PEM format with extra whitespace")
        void shouldHandlePemFormatWithExtraWhitespace() {
            // Given
            String pemWithWhitespace = "-----BEGIN PUBLIC KEY-----\n  " +
                    validBase64PublicKey + "  \n\t" +
                    "-----END PUBLIC KEY-----  \n";
            String base64PemWithWhitespace = Base64.getEncoder().encodeToString(pemWithWhitespace.getBytes());
            when(jwtProperties.getPublicKey()).thenReturn(base64PemWithWhitespace);

            // When
            PublicKeyProviderImpl provider = new PublicKeyProviderImpl(jwtProperties);

            // Then
            assertNotNull(provider);
            Key key = provider.getPublicKey();
            assertNotNull(key);
            assertInstanceOf(RSAPublicKey.class, key);
        }

        @Test
        @DisplayName("Should handle PEM format with different line endings")
        void shouldHandlePemFormatWithDifferentLineEndings() {
            // Given
            String pemWithCRLF = "-----BEGIN PUBLIC KEY-----\r\n" +
                    validBase64PublicKey + "\r\n" +
                    "-----END PUBLIC KEY-----";
            String base64PemWithCRLF = Base64.getEncoder().encodeToString(pemWithCRLF.getBytes());
            when(jwtProperties.getPublicKey()).thenReturn(base64PemWithCRLF);

            // When
            PublicKeyProviderImpl provider = new PublicKeyProviderImpl(jwtProperties);

            // Then
            assertNotNull(provider);
            Key key = provider.getPublicKey();
            assertNotNull(key);
            assertInstanceOf(RSAPublicKey.class, key);
        }

        @Test
        @DisplayName("Should throw exception when PEM content is malformed after Base64 decode")
        void shouldThrowExceptionWhenPemContentIsMalformed() {
            // Given - Base64 encoded malformed PEM
            String malformedPem = "-----BEGIN PUBLIC KEY-----\ninvalid-key-content\n-----END PUBLIC KEY-----";
            String base64MalformedPem = Base64.getEncoder().encodeToString(malformedPem.getBytes());
            when(jwtProperties.getPublicKey()).thenReturn(base64MalformedPem);

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new PublicKeyProviderImpl(jwtProperties)
            );
            assertTrue(exception.getMessage().startsWith("Invalid JWT public key:"));
            assertNotNull(exception.getCause());
        }

        @Test
        @DisplayName("Should handle edge case where input looks like PEM but isn't valid")
        void shouldHandleEdgeCaseInvalidPemLikeInput() {
            // Given - Contains PEM markers but invalid structure
            String fakePem = "-----BEGIN PUBLIC KEY-----invalid-----END PUBLIC KEY-----";
            String base64FakePem = Base64.getEncoder().encodeToString(fakePem.getBytes());
            when(jwtProperties.getPublicKey()).thenReturn(base64FakePem);

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new PublicKeyProviderImpl(jwtProperties)
            );
            assertTrue(exception.getMessage().startsWith("Invalid JWT public key:"));
        }
    }

    @Nested
    @DisplayName("getPublicKey Method Tests")
    class GetPublicKeyMethodTests {

        @Test
        @DisplayName("Should return the same public key instance on multiple calls")
        void shouldReturnSamePublicKeyInstanceOnMultipleCalls() {
            // Given
            when(jwtProperties.getPublicKey()).thenReturn(validBase64PublicKey);
            PublicKeyProviderImpl provider = new PublicKeyProviderImpl(jwtProperties);

            // When
            Key key1 = provider.getPublicKey();
            Key key2 = provider.getPublicKey();

            // Then
            assertSame(key1, key2);
            assertNotNull(key1);
            assertInstanceOf(RSAPublicKey.class, key1);
        }

        @Test
        @DisplayName("Should return RSA public key with correct algorithm")
        void shouldReturnRsaPublicKeyWithCorrectAlgorithm() {
            // Given
            when(jwtProperties.getPublicKey()).thenReturn(validBase64PublicKey);
            PublicKeyProviderImpl provider = new PublicKeyProviderImpl(jwtProperties);

            // When
            Key key = provider.getPublicKey();

            // Then
            assertNotNull(key);
            assertEquals("RSA", key.getAlgorithm());
            assertInstanceOf(RSAPublicKey.class, key);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should work end-to-end with real RSA key pair")
        void shouldWorkEndToEndWithRealRsaKeyPair() throws Exception {
            // Given - Generate a fresh key pair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024); // Smaller key for faster test
            KeyPair keyPair = keyGen.generateKeyPair();
            PublicKey originalPublicKey = keyPair.getPublic();

            String base64Key = Base64.getEncoder().encodeToString(originalPublicKey.getEncoded());
            when(jwtProperties.getPublicKey()).thenReturn(base64Key);

            // When
            PublicKeyProviderImpl provider = new PublicKeyProviderImpl(jwtProperties);
            Key retrievedKey = provider.getPublicKey();

            // Then
            assertNotNull(retrievedKey);
            assertEquals("RSA", retrievedKey.getAlgorithm());
            assertArrayEquals(originalPublicKey.getEncoded(), retrievedKey.getEncoded());
        }

        @Test
        @DisplayName("Should handle minimum valid RSA key size")
        void shouldHandleMinimumValidRsaKeySize() throws Exception {
            // Given - 512-bit RSA key (minimum for some implementations)
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(512);
            KeyPair keyPair = keyGen.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();

            String base64Key = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            when(jwtProperties.getPublicKey()).thenReturn(base64Key);

            // When
            PublicKeyProviderImpl provider = new PublicKeyProviderImpl(jwtProperties);

            // Then
            assertNotNull(provider.getPublicKey());
            assertEquals("RSA", provider.getPublicKey().getAlgorithm());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very long Base64 string")
        void shouldHandleVeryLongBase64String() throws Exception {
            // Given - 4096-bit RSA key (very large)
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(4096);
            KeyPair keyPair = keyGen.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();

            String base64Key = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            when(jwtProperties.getPublicKey()).thenReturn(base64Key);

            // When
            PublicKeyProviderImpl provider = new PublicKeyProviderImpl(jwtProperties);

            // Then
            assertNotNull(provider.getPublicKey());
            assertEquals("RSA", provider.getPublicKey().getAlgorithm());
        }

        @Test
        @DisplayName("Should handle PEM with only BEGIN marker")
        void shouldHandlePemWithOnlyBeginMarker() {
            // Given
            String incompletePem = "-----BEGIN PUBLIC KEY-----\n" + validBase64PublicKey;
            String base64IncompletePem = Base64.getEncoder().encodeToString(incompletePem.getBytes());
            when(jwtProperties.getPublicKey()).thenReturn(base64IncompletePem);

            // When
            PublicKeyProviderImpl provider = new PublicKeyProviderImpl(jwtProperties);

            // Then - Should still work as it processes the key content
            assertNotNull(provider.getPublicKey());
        }

        @Test
        @DisplayName("Should handle multiple PEM markers in content")
        void shouldHandleMultiplePemMarkersInContent() {
            // Given
            String multiMarkerPem = "-----BEGIN PUBLIC KEY-----\n" +
                    validBase64PublicKey + "\n" +
                    "-----END PUBLIC KEY-----\n" +
                    "-----BEGIN PUBLIC KEY-----"; // Extra marker
            String base64MultiMarkerPem = Base64.getEncoder().encodeToString(multiMarkerPem.getBytes());
            when(jwtProperties.getPublicKey()).thenReturn(base64MultiMarkerPem);

            // When
            PublicKeyProviderImpl provider = new PublicKeyProviderImpl(jwtProperties);

            // Then
            assertNotNull(provider.getPublicKey());
            assertEquals("RSA", provider.getPublicKey().getAlgorithm());
        }
    }
}
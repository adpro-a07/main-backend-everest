package id.ac.ui.cs.advprog.everest.authentication;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

/**
 * Loads and provides an RSA public key for token verification from Base64 or PEM encoded strings.
 */
@Component
public class PublicKeyProviderImpl implements PublicKeyProvider {

    private final PublicKey publicKey;

    /**
     * Initializes PublicKeyProviderImpl using the public key from configuration
     * @param properties JwtProperties or any config class that provides the public key string
     */
    public PublicKeyProviderImpl(JwtProperties properties) {
        Objects.requireNonNull(properties, "JwtProperties must not be null");
        String publicKeyStr = properties.getPublicKey();
        Objects.requireNonNull(publicKeyStr, "JWT publicKey must not be null");
        Assert.hasText(publicKeyStr, "JWT publicKey must not be empty");

        try {
            this.publicKey = loadPublicKey(publicKeyStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT public key: " + e.getMessage(), e);
        }
    }

    private PublicKey loadPublicKey(String base64Pem) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String pem = new String(Base64.getDecoder().decode(base64Pem));

        String publicKeyPEM;
        if (pem.contains("-----BEGIN PUBLIC KEY-----")) {
            publicKeyPEM = pem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
        } else {
            // fallback: treat the raw input as already Base64 (e.g., no PEM wrapper)
            publicKeyPEM = base64Pem;
        }

        byte[] keyBytes = Base64.getDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(keySpec);
    }

    /**
     * Returns the RSA public key to verify signatures
     */
    @Override
    public Key getPublicKey() {
        return publicKey;
    }
}

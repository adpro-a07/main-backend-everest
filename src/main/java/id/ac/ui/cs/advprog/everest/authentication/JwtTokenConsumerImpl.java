package id.ac.ui.cs.advprog.everest.authentication;

import id.ac.ui.cs.advprog.everest.authentication.exception.AuthenticationException;
import id.ac.ui.cs.advprog.everest.authentication.interfaces.JwtTokenParser;
import id.ac.ui.cs.advprog.everest.authentication.interfaces.JwtTokenValidator;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Clock;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtTokenConsumerImpl implements JwtTokenParser, JwtTokenValidator {
    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String USER_ID_CLAIM = "userId";

    private final PublicKeyProvider publicKeyProvider;
    private final Clock clock;

    public JwtTokenConsumerImpl(PublicKeyProvider publicKeyProvider, Clock clock) {
        this.publicKeyProvider = Objects.requireNonNull(publicKeyProvider, "PublicKeyProvider must not be null");
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    @Override
    public String getEmailFromToken(String token) {
        validateToken(token);
        return getAllClaimsFromToken(token).getSubject();
    }

    @Override
    public UUID getUserIdFromToken(String token) {
        validateToken(token);
        try {
            String userId = getAllClaimsFromToken(token).get(USER_ID_CLAIM, String.class);
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new AuthenticationException("Invalid user ID format in token", e);
        }
    }

    @Override
    public String getTokenType(String token) {
        validateToken(token);
        return getAllClaimsFromToken(token).get(TOKEN_TYPE_CLAIM, String.class);
    }

    @Override
    public <T> T getClaimFromToken(String token, String claimName, Class<T> claimType) {
        validateToken(token);
        return getAllClaimsFromToken(token).get(claimName, claimType);
    }

    @Override
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        validateToken(token);
        return claimsResolver.apply(getAllClaimsFromToken(token));
    }

    @Override
    public boolean validateToken(String token) {
        Assert.hasText(token, "Token must not be null or empty");
        try {
            Claims claims = getAllClaimsFromToken(token);
            return !isTokenExpired(claims);
        } catch (JwtException | IllegalArgumentException | AuthenticationException e) {
            return false;
        }
    }

    @Override
    public Date getExpirationDateFromToken(String token) {
        validateToken(token);
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private Claims getAllClaimsFromToken(String token) {
        Assert.hasText(token, "Token must not be null or empty");

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(publicKeyProvider.getPublicKey()) // use RS256 verification
                    .setClock(() -> Date.from(clock.instant()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new AuthenticationException("Invalid token", e);
        }
    }

    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration != null && expiration.before(Date.from(clock.instant()));
    }
}

package id.ac.ui.cs.advprog.everest.authentication.interfaces;

public interface JwtTokenValidator {
    boolean validateToken(String token);
}


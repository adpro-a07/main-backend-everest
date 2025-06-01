package id.ac.ui.cs.advprog.everest.authentication;

import java.security.Key;

public interface PublicKeyProvider {
    Key getPublicKey();
}
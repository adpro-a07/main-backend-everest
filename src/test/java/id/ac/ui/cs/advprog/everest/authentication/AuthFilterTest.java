package id.ac.ui.cs.advprog.everest.authentication;

import id.ac.ui.cs.advprog.everest.authentication.exception.AuthServiceException;
import id.ac.ui.cs.advprog.everest.authentication.exception.InvalidTokenException;
import id.ac.ui.cs.advprog.everest.service.AuthServiceGrpcClient;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class AuthFilterTest {

    @Mock
    private AuthServiceGrpcClient authServiceGrpcClient;

    @Mock
    private PublicRouteMatcher publicRouteMatcher;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private AuthFilter authFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        UserContext.clear(); // Ensure clean state
    }

    @Test
    void testPublicRoute_skipsAuthentication() throws Exception {
        when(request.getRequestURI()).thenReturn("/public");
        when(publicRouteMatcher.isPublic("/public")).thenReturn(true);

        authFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(authServiceGrpcClient, never()).validateToken(any());
        verify(response, never()).sendError(anyInt(), anyString());
        assertNull(UserContext.getUser());
    }

    @Test
    void testMissingAuthorizationHeader_returns401() throws Exception {
        when(request.getRequestURI()).thenReturn("/secure");
        when(publicRouteMatcher.isPublic("/secure")).thenReturn(false);
        when(request.getHeader("Authorization")).thenReturn(null);

        authFilter.doFilterInternal(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void testInvalidAuthorizationHeader_returns401() throws Exception {
        when(request.getRequestURI()).thenReturn("/secure");
        when(publicRouteMatcher.isPublic("/secure")).thenReturn(false);
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

        authFilter.doFilterInternal(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void testValidToken_setsUserAndProceeds() throws Exception {
        String token = "valid.jwt.token";
        AuthenticatedUser mockUser = new AuthenticatedUser(
                UUID.randomUUID(),
                "test@example.com",
                "Test User",
                UserRole.CUSTOMER,
                "123456789",
                Instant.now(),
                Instant.now(),
                "Some Address",
                "Some Experience",
                5,
                10000L
        );

        when(request.getRequestURI()).thenReturn("/secure");
        when(publicRouteMatcher.isPublic("/secure")).thenReturn(false);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(authServiceGrpcClient.validateToken(token)).thenReturn(mockUser);

        authFilter.doFilterInternal(request, response, filterChain);

        verify(authServiceGrpcClient).validateToken(token);
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());

        // Ensure the user was set and cleared
        assertNull(UserContext.getUser());
    }

    @Test
    void testInvalidToken_returns401() throws Exception {
        when(request.getRequestURI()).thenReturn("/secure");
        when(publicRouteMatcher.isPublic("/secure")).thenReturn(false);
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(authServiceGrpcClient.validateToken("invalid-token"))
                .thenThrow(new InvalidTokenException("Invalid token"));

        authFilter.doFilterInternal(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void testAuthServiceException_returns503() throws Exception {
        when(request.getRequestURI()).thenReturn("/secure");
        when(publicRouteMatcher.isPublic("/secure")).thenReturn(false);
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(authServiceGrpcClient.validateToken("token"))
                .thenThrow(new AuthServiceException("Service down", new RuntimeException()));

        authFilter.doFilterInternal(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Authentication service unavailable");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void testUnexpectedException_returns500() throws Exception {
        when(request.getRequestURI()).thenReturn("/secure");
        when(publicRouteMatcher.isPublic("/secure")).thenReturn(false);
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(authServiceGrpcClient.validateToken("token"))
                .thenThrow(new RuntimeException("Unexpected"));

        authFilter.doFilterInternal(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error during authentication");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void testUserContextAlwaysCleared_afterSuccess() throws Exception {
        AuthenticatedUser mockUser = new AuthenticatedUser(
                UUID.randomUUID(),
                "test@example.com",
                "Test User",
                UserRole.CUSTOMER,
                "123456789",
                Instant.now(),
                Instant.now(),
                "Some Address",
                "Some Experience",
                5,
                10000L
        );

        when(request.getRequestURI()).thenReturn("/secure");
        when(publicRouteMatcher.isPublic("/secure")).thenReturn(false);
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(authServiceGrpcClient.validateToken("token"))
                .thenReturn(mockUser);

        authFilter.doFilterInternal(request, response, filterChain);

        assertNull(UserContext.getUser());
    }

    @Test
    void testUserContextAlwaysCleared_afterException() throws Exception {
        when(request.getRequestURI()).thenReturn("/secure");
        when(publicRouteMatcher.isPublic("/secure")).thenReturn(false);
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(authServiceGrpcClient.validateToken("token"))
                .thenThrow(new RuntimeException("error"));

        authFilter.doFilterInternal(request, response, filterChain);

        assertNull(UserContext.getUser());
    }
}


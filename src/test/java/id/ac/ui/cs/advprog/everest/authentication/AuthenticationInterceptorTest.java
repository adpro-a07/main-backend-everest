package id.ac.ui.cs.advprog.everest.authentication;

import id.ac.ui.cs.advprog.everest.authentication.exception.AuthServiceException;
import id.ac.ui.cs.advprog.everest.authentication.exception.InvalidTokenException;
import id.ac.ui.cs.advprog.everest.common.service.AuthServiceGrpcClient;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationInterceptorTest {

    @Mock
    AuthServiceGrpcClient authServiceGrpcClient;

    @InjectMocks
    AuthenticationInterceptor interceptor;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    HandlerMethod handlerMethod;

    Method dummyMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        MockitoAnnotations.openMocks(this);
        dummyMethod = DummyController.class.getMethod("securedMethod", AuthenticatedUser.class);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
        UserContext.clear();
    }

    static class DummyController {
        @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
        public void securedMethod(@CurrentUser AuthenticatedUser user) {}
        public void openMethod() {}
    }

    @Test
    void testPreHandle_skipsIfHandlerIsNotHandlerMethod() throws Exception {
        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);
    }

    @Test
    void testPreHandle_skipsIfNoAuthAnnotations() throws Exception {
        when(handlerMethod.getMethod()).thenReturn(DummyController.class.getMethod("openMethod"));
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        assertTrue(result);
    }

    @Test
    void testPreHandle_rejectsMissingAuthorizationHeader() throws Exception {
        when(handlerMethod.getMethod()).thenReturn(dummyMethod);
        when(request.getHeader("Authorization")).thenReturn(null);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
        assertFalse(result);
    }

    @Test
    void testPreHandle_acceptsValidToken() throws Exception {
        when(handlerMethod.getMethod()).thenReturn(dummyMethod);
        when(request.getHeader("Authorization")).thenReturn("Bearer VALID_TOKEN");

        AuthenticatedUser user = new AuthenticatedUser(
                UUID.randomUUID(),
                "a@a.com",
                "Customer Fullname",
                UserRole.CUSTOMER,
                "001122334455",
                Instant.now(),
                Instant.now(),
                "Depok",
                null,
                null,
                null
        );
        when(authServiceGrpcClient.validateToken("VALID_TOKEN")).thenReturn(user);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
        assertEquals(user, UserContext.getUser());
        assertEquals("ROLE_CUSTOMER", SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void testPreHandle_rejectsInvalidToken() throws Exception {
        when(handlerMethod.getMethod()).thenReturn(dummyMethod);
        when(request.getHeader("Authorization")).thenReturn("Bearer INVALID_TOKEN");

        when(authServiceGrpcClient.validateToken("INVALID_TOKEN")).thenThrow(new InvalidTokenException("Invalid"));

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        assertFalse(result);
    }

    @Test
    void testPreHandle_rejectsOnAuthServiceError() throws Exception {
        when(handlerMethod.getMethod()).thenReturn(dummyMethod);
        when(request.getHeader("Authorization")).thenReturn("Bearer TOKEN");

        when(authServiceGrpcClient.validateToken("TOKEN")).thenThrow(
                new AuthServiceException("Service unavailable", new RuntimeException())
        );

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        verify(response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Authentication service unavailable");
        assertFalse(result);
    }

    @Test
    void testPreHandle_rejectsOnUnexpectedException() throws Exception {
        when(handlerMethod.getMethod()).thenReturn(dummyMethod);
        when(request.getHeader("Authorization")).thenReturn("Bearer TOKEN");

        when(authServiceGrpcClient.validateToken("TOKEN")).thenThrow(new RuntimeException("Unexpected"));

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error during authentication");
        assertFalse(result);
    }

    @Test
    void testAfterCompletion_clearsUserContext() {
        UserContext.setUser(new AuthenticatedUser(
                UUID.randomUUID(),
                "a@a.com",
                "Admin Fullname",
                UserRole.ADMIN,
                "001122334455",
                Instant.now(),
                Instant.now(),
                null,
                null,
                null,
                null
        ));
        interceptor.afterCompletion(request, response, handlerMethod, null);
        assertNull(UserContext.getUser());
    }
}

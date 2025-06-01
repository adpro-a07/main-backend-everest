package id.ac.ui.cs.advprog.everest.authentication;

import id.ac.ui.cs.advprog.everest.authentication.exception.AuthServiceException;
import id.ac.ui.cs.advprog.everest.authentication.exception.AuthenticationException;
import id.ac.ui.cs.advprog.everest.common.service.AuthServiceGrpcClient;
import id.ac.ui.cs.advprog.everest.common.utils.TimestampUtil;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;

import com.google.protobuf.Timestamp;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationInterceptorTest {

    @Mock
    private JwtTokenConsumerImpl jwtTokenConsumer;

    @Mock
    private AuthServiceGrpcClient authServiceGrpcClient;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HandlerMethod handlerMethod;

    @Mock
    private Method method;

    @Mock
    private Parameter parameter;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthenticationInterceptor authenticationInterceptor;

    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String BEARER_TOKEN = "Bearer " + VALID_TOKEN;
    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_FULL_NAME = "Test User";
    private static final String TEST_PHONE = "1234567890";
    private static final String TEST_ADDRESS = "Test Address";
    private static final String TEST_WORK_EXP = "5 years in software development";
    private static final Integer TEST_JOBS_DONE = 10;
    private static final Long TEST_INCOME = 50000L;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testPreHandle_NotHandlerMethod_ReturnsTrue() throws Exception {
        // Given
        Object handler = new Object(); // Not a HandlerMethod

        // When
        boolean result = authenticationInterceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verifyNoInteractions(jwtTokenConsumer, authServiceGrpcClient);
    }

    @Test
    void testPreHandle_NoAnnotations_ReturnsTrue() throws Exception {
        // Given
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.isAnnotationPresent(PreAuthorize.class)).thenReturn(false);
        when(method.getParameters()).thenReturn(new Parameter[0]);

        // When
        boolean result = authenticationInterceptor.preHandle(request, response, handlerMethod);

        // Then
        assertTrue(result);
        verifyNoInteractions(jwtTokenConsumer, authServiceGrpcClient);
    }

    @Test
    void testPreHandle_HasPreAuthorizeOnly_NoCurrentUser_Success() throws Exception {
        // Given
        setupMethodWithPreAuthorizeOnly();
        setupValidTokenScenario();
        mockJwtClaimsExtraction();

        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class)) {
            // When
            boolean result = authenticationInterceptor.preHandle(request, response, handlerMethod);

            // Then
            assertTrue(result);
            verifyTokenValidation();
            verifyUserContextSet(userContextMock);
            verifySecurityContextSet();
        }
    }

    @Test
    void testPreHandle_HasCurrentUserOnly_Success() throws Exception {
        // Given
        setupMethodWithCurrentUserOnly();
        setupValidTokenScenario();
        mockCompleteUserDataFetch();

        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class)) {
            // When
            boolean result = authenticationInterceptor.preHandle(request, response, handlerMethod);

            // Then
            assertTrue(result);
            verifyTokenValidation();
            verifyCompleteUserDataFetch();
            verifyUserContextSet(userContextMock);
            verifySecurityContextSet();
        }
    }

    @Test
    void testPreHandle_HasBothAnnotations_Success() throws Exception {
        // Given
        setupMethodWithBothAnnotations();
        setupValidTokenScenario();
        mockCompleteUserDataFetch();

        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class)) {
            // When
            boolean result = authenticationInterceptor.preHandle(request, response, handlerMethod);

            // Then
            assertTrue(result);
            verifyTokenValidation();
            verifyCompleteUserDataFetch();
            verifyUserContextSet(userContextMock);
            verifySecurityContextSet();
        }
    }

    @Test
    void testPreHandle_MissingAuthorizationHeader_ReturnsUnauthorized() throws Exception {
        // Given
        setupMethodWithPreAuthorizeOnly();
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        boolean result = authenticationInterceptor.preHandle(request, response, handlerMethod);

        // Then
        assertFalse(result);
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
    }

    @Test
    void testPreHandle_InvalidAuthorizationHeader_ReturnsUnauthorized() throws Exception {
        // Given
        setupMethodWithPreAuthorizeOnly();
        when(request.getHeader("Authorization")).thenReturn("Invalid header");

        // When
        boolean result = authenticationInterceptor.preHandle(request, response, handlerMethod);

        // Then
        assertFalse(result);
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
    }

    @Test
    void testPreHandle_TokenValidationFails_ReturnsUnauthorized() throws Exception {
        // Given
        setupMethodWithPreAuthorizeOnly();
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(jwtTokenConsumer.validateToken(VALID_TOKEN)).thenReturn(false);

        // When
        boolean result = authenticationInterceptor.preHandle(request, response, handlerMethod);

        // Then
        assertFalse(result);
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
    }

    @Test
    void testPreHandle_AuthServiceException_ReturnsServiceUnavailable() throws Exception {
        // Given
        setupMethodWithCurrentUserOnly();
        setupValidTokenScenario();
        when(jwtTokenConsumer.getUserIdFromToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);
        when(authServiceGrpcClient.lookupUserById(TEST_USER_ID.toString()))
                .thenThrow(new AuthServiceException("Service unavailable"));

        // When
        boolean result = authenticationInterceptor.preHandle(request, response, handlerMethod);

        // Then
        assertFalse(result);
        verify(response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Authentication service unavailable");
    }

    @Test
    void testPreHandle_AuthenticationException_ReturnsUnauthorized() throws Exception {
        // Given
        setupMethodWithPreAuthorizeOnly();
        setupValidTokenScenario();
        when(jwtTokenConsumer.getEmailFromToken(VALID_TOKEN))
                .thenThrow(new AuthenticationException("Invalid token"));

        // When
        boolean result = authenticationInterceptor.preHandle(request, response, handlerMethod);

        // Then
        assertFalse(result);
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
    }

    @Test
    void testPreHandle_UnexpectedException_ReturnsInternalServerError() throws Exception {
        // Given
        setupMethodWithPreAuthorizeOnly();
        setupValidTokenScenario();
        when(jwtTokenConsumer.getEmailFromToken(VALID_TOKEN))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When
        boolean result = authenticationInterceptor.preHandle(request, response, handlerMethod);

        // Then
        assertFalse(result);
        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error during authentication");
    }

    @Test
    void testFetchCompleteUserData_Success() throws Exception {
        // Given
        setupMethodWithCurrentUserOnly();
        setupValidTokenScenario();
        mockCompleteUserDataFetchWithTimestamps();

        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class);
             MockedStatic<TimestampUtil> timestampUtilMock = mockStatic(TimestampUtil.class)) {

            Instant now = Instant.now();
            timestampUtilMock.when(() -> TimestampUtil.toInstant(any(Timestamp.class))).thenReturn(now);

            // When
            boolean result = authenticationInterceptor.preHandle(request, response, handlerMethod);

            // Then
            assertTrue(result);
            verifyCompleteUserDataFetch();
        }
    }

    @Test
    void testFetchCompleteUserData_UserNotFound() throws Exception {
        // Given
        setupMethodWithCurrentUserOnly();
        setupValidTokenScenario();
        when(jwtTokenConsumer.getUserIdFromToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);

        UserLookupResponse response = UserLookupResponse.newBuilder()
                .build();
        when(authServiceGrpcClient.lookupUserById(TEST_USER_ID.toString())).thenReturn(response);

        // When
        boolean result = authenticationInterceptor.preHandle(request, this.response, handlerMethod);

        // Then
        assertFalse(result);
        // Based on the actual error, it's being caught as AuthServiceException, returning 503
        verify(this.response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Authentication service unavailable");
    }

    @Test
    void testPreHandle_DirectAuthenticationException_ReturnsUnauthorized() throws Exception {
        // Given
        setupMethodWithCurrentUserOnly();
        setupValidTokenScenario();
        when(jwtTokenConsumer.getUserIdFromToken(VALID_TOKEN))
                .thenThrow(new AuthenticationException("Token parsing failed"));

        // When
        boolean result = authenticationInterceptor.preHandle(request, response, handlerMethod);

        // Then
        assertFalse(result);
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
    }

    @Test
    void testFetchCompleteUserData_UnexpectedError() throws Exception {
        // Given
        setupMethodWithCurrentUserOnly();
        setupValidTokenScenario();
        when(jwtTokenConsumer.getUserIdFromToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);
        when(authServiceGrpcClient.lookupUserById(TEST_USER_ID.toString()))
                .thenThrow(new RuntimeException("Database error"));

        // When
        boolean result = authenticationInterceptor.preHandle(request, response, handlerMethod);

        // Then
        assertFalse(result);
        verify(response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Authentication service unavailable");
    }

    @Test
    void testCreateUserFromJwtClaims_AllClaimsPresent() throws Exception {
        // Given
        setupMethodWithPreAuthorizeOnly();
        setupValidTokenScenario();
        mockAllJwtClaims();

        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class)) {
            // When
            boolean result = authenticationInterceptor.preHandle(request, response, handlerMethod);

            // Then
            assertTrue(result);
            verifyAllClaimsExtracted();
        }
    }

    @Test
    void testCreateUserFromJwtClaims_MissingRoleClaim() throws Exception {
        // Given
        setupMethodWithPreAuthorizeOnly();
        setupValidTokenScenario();
        mockJwtClaimsWithMissingRole();

        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class)) {
            // When
            boolean result = authenticationInterceptor.preHandle(request, response, handlerMethod);

            // Then
            assertTrue(result);
            verifyDefaultRoleUsed();
        }
    }

    @Test
    void testCreateUserFromJwtClaims_InvalidRoleClaim() throws Exception {
        // Given
        setupMethodWithPreAuthorizeOnly();
        setupValidTokenScenario();
        mockJwtClaimsWithInvalidRole();

        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class)) {
            // When
            boolean result = authenticationInterceptor.preHandle(request, response, handlerMethod);

            // Then
            assertTrue(result);
            verifyCustomerRoleDefault();
        }
    }

    @Test
    void testCreateUserFromJwtClaims_MissingOptionalClaims() throws Exception {
        // Given
        setupMethodWithPreAuthorizeOnly();
        setupValidTokenScenario();
        mockJwtClaimsWithMissingOptionals();

        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class)) {
            // When
            boolean result = authenticationInterceptor.preHandle(request, response, handlerMethod);

            // Then
            assertTrue(result);
            verifyOptionalClaimsHandled();
        }
    }

    @Test
    void testAfterCompletion_ClearsUserContext() {
        // Given
        Exception exception = new RuntimeException("Test exception");

        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class)) {
            // When
            authenticationInterceptor.afterCompletion(request, response, handlerMethod, exception);

            // Then
            userContextMock.verify(UserContext::clear);
        }
    }

    @Test
    void testAfterCompletion_WithNullException() {
        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class)) {
            // When
            authenticationInterceptor.afterCompletion(request, response, handlerMethod, null);

            // Then
            userContextMock.verify(UserContext::clear);
        }
    }

    // Helper methods for test setup
    private void setupMethodWithPreAuthorizeOnly() {
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.isAnnotationPresent(PreAuthorize.class)).thenReturn(true);
        when(method.getParameters()).thenReturn(new Parameter[0]);
    }

    private void setupMethodWithCurrentUserOnly() {
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.isAnnotationPresent(PreAuthorize.class)).thenReturn(false);
        when(parameter.isAnnotationPresent(CurrentUser.class)).thenReturn(true);
        when(method.getParameters()).thenReturn(new Parameter[]{parameter});
    }

    private void setupMethodWithBothAnnotations() {
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.isAnnotationPresent(PreAuthorize.class)).thenReturn(true);
        when(parameter.isAnnotationPresent(CurrentUser.class)).thenReturn(true);
        when(method.getParameters()).thenReturn(new Parameter[]{parameter});
    }

    private void setupValidTokenScenario() {
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(jwtTokenConsumer.validateToken(VALID_TOKEN)).thenReturn(true);
    }

    private void mockJwtClaimsExtraction() {
        when(jwtTokenConsumer.getEmailFromToken(VALID_TOKEN)).thenReturn(TEST_EMAIL);
        when(jwtTokenConsumer.getUserIdFromToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);
        when(jwtTokenConsumer.getClaimFromToken(VALID_TOKEN, "role", String.class)).thenReturn("ADMIN");
        when(jwtTokenConsumer.getClaimFromToken(VALID_TOKEN, "fullName", String.class)).thenReturn(TEST_FULL_NAME);
        when(jwtTokenConsumer.getClaimFromToken(VALID_TOKEN, "createdAt", Long.class)).thenReturn(System.currentTimeMillis() / 1000);
    }

    private void mockAllJwtClaims() {
        when(jwtTokenConsumer.getEmailFromToken(VALID_TOKEN)).thenReturn(TEST_EMAIL);
        when(jwtTokenConsumer.getUserIdFromToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);
        when(jwtTokenConsumer.getClaimFromToken(VALID_TOKEN, "role", String.class)).thenReturn("ADMIN");
        when(jwtTokenConsumer.getClaimFromToken(VALID_TOKEN, "fullName", String.class)).thenReturn(TEST_FULL_NAME);
        when(jwtTokenConsumer.getClaimFromToken(VALID_TOKEN, "createdAt", Long.class)).thenReturn(1640995200L);
    }

    private void mockJwtClaimsWithMissingRole() {
        when(jwtTokenConsumer.getEmailFromToken(VALID_TOKEN)).thenReturn(TEST_EMAIL);
        when(jwtTokenConsumer.getUserIdFromToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);
        when(jwtTokenConsumer.getClaimFromToken(VALID_TOKEN, "role", String.class)).thenReturn(null);
        when(jwtTokenConsumer.getClaimFromToken(VALID_TOKEN, "fullName", String.class)).thenReturn(TEST_FULL_NAME);
        when(jwtTokenConsumer.getClaimFromToken(VALID_TOKEN, "createdAt", Long.class)).thenReturn(1640995200L);
    }

    private void mockJwtClaimsWithInvalidRole() {
        when(jwtTokenConsumer.getEmailFromToken(VALID_TOKEN)).thenReturn(TEST_EMAIL);
        when(jwtTokenConsumer.getUserIdFromToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);
        when(jwtTokenConsumer.getClaimFromToken(VALID_TOKEN, "role", String.class)).thenReturn("INVALID_ROLE");
        when(jwtTokenConsumer.getClaimFromToken(VALID_TOKEN, "fullName", String.class)).thenReturn(TEST_FULL_NAME);
        when(jwtTokenConsumer.getClaimFromToken(VALID_TOKEN, "createdAt", Long.class)).thenReturn(1640995200L);
    }

    private void mockJwtClaimsWithMissingOptionals() {
        when(jwtTokenConsumer.getEmailFromToken(VALID_TOKEN)).thenReturn(TEST_EMAIL);
        when(jwtTokenConsumer.getUserIdFromToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);
        when(jwtTokenConsumer.getClaimFromToken(VALID_TOKEN, "role", String.class)).thenReturn("USER");
        when(jwtTokenConsumer.getClaimFromToken(VALID_TOKEN, "fullName", String.class))
                .thenThrow(new RuntimeException("Claim not found"));
        when(jwtTokenConsumer.getClaimFromToken(VALID_TOKEN, "createdAt", Long.class))
                .thenThrow(new RuntimeException("Claim not found"));
    }

    private void mockCompleteUserDataFetch() {
        when(jwtTokenConsumer.getUserIdFromToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);

        UserIdentity userIdentity = UserIdentity.newBuilder()
                .setId(TEST_USER_ID.toString())
                .setEmail(TEST_EMAIL)
                .setFullName(TEST_FULL_NAME)
                .setRole(UserRole.ADMIN)
                .setPhoneNumber(TEST_PHONE)
                .build();

        UserProfile userProfile = UserProfile.newBuilder()
                .setAddress(TEST_ADDRESS)
                .setWorkExperience(TEST_WORK_EXP)
                .setTotalJobsDone(TEST_JOBS_DONE)
                .setTotalIncome(TEST_INCOME)
                .build();

        UserData userData = UserData.newBuilder()
                .setIdentity(userIdentity)
                .setProfile(userProfile)
                .build();

        UserLookupResponse lookupResponse = UserLookupResponse.newBuilder()
                .setUserData(userData)
                .build();

        when(authServiceGrpcClient.lookupUserById(TEST_USER_ID.toString())).thenReturn(lookupResponse);
    }

    private void mockCompleteUserDataFetchWithTimestamps() {
        when(jwtTokenConsumer.getUserIdFromToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);

        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(System.currentTimeMillis() / 1000)
                .build();

        UserIdentity userIdentity = UserIdentity.newBuilder()
                .setId(TEST_USER_ID.toString())
                .setEmail(TEST_EMAIL)
                .setFullName(TEST_FULL_NAME)
                .setRole(UserRole.ADMIN)
                .setPhoneNumber(TEST_PHONE)
                .setCreatedAt(timestamp)
                .setUpdatedAt(timestamp)
                .build();

        UserProfile userProfile = UserProfile.newBuilder()
                .setAddress(TEST_ADDRESS)
                .setWorkExperience(TEST_WORK_EXP)
                .setTotalJobsDone(TEST_JOBS_DONE)
                .setTotalIncome(TEST_INCOME)
                .build();

        UserData userData = UserData.newBuilder()
                .setIdentity(userIdentity)
                .setProfile(userProfile)
                .build();

        UserLookupResponse lookupResponse = UserLookupResponse.newBuilder()
                .setUserData(userData)
                .build();

        when(authServiceGrpcClient.lookupUserById(TEST_USER_ID.toString())).thenReturn(lookupResponse);
    }

    // Verification helper methods
    private void verifyTokenValidation() {
        verify(jwtTokenConsumer).validateToken(VALID_TOKEN);
    }

    private void verifyUserContextSet(MockedStatic<UserContext> userContextMock) {
        userContextMock.verify(() -> UserContext.setUser(any(AuthenticatedUser.class)));
    }

    private void verifySecurityContextSet() {
        ArgumentCaptor<Authentication> authCaptor = ArgumentCaptor.forClass(Authentication.class);
        verify(securityContext).setAuthentication(authCaptor.capture());

        Authentication auth = authCaptor.getValue();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
                auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")) ||
                auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
    }

    private void verifyCompleteUserDataFetch() {
        verify(authServiceGrpcClient).lookupUserById(TEST_USER_ID.toString());
    }

    private void verifyAllClaimsExtracted() {
        verify(jwtTokenConsumer).getClaimFromToken(VALID_TOKEN, "role", String.class);
        verify(jwtTokenConsumer).getClaimFromToken(VALID_TOKEN, "fullName", String.class);
        verify(jwtTokenConsumer).getClaimFromToken(VALID_TOKEN, "createdAt", Long.class);
    }

    private void verifyDefaultRoleUsed() {
        verify(jwtTokenConsumer).getClaimFromToken(VALID_TOKEN, "role", String.class);
    }

    private void verifyCustomerRoleDefault() {
        verify(jwtTokenConsumer).getClaimFromToken(VALID_TOKEN, "role", String.class);
    }

    private void verifyOptionalClaimsHandled() {
        verify(jwtTokenConsumer).getClaimFromToken(VALID_TOKEN, "fullName", String.class);
        verify(jwtTokenConsumer).getClaimFromToken(VALID_TOKEN, "createdAt", Long.class);
    }
}
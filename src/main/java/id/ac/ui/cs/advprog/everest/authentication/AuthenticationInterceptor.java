package id.ac.ui.cs.advprog.everest.authentication;

import id.ac.ui.cs.advprog.everest.authentication.exception.AuthServiceException;
import id.ac.ui.cs.advprog.everest.authentication.exception.AuthenticationException;
import id.ac.ui.cs.advprog.everest.common.utils.TimestampUtil;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.*;
import id.ac.ui.cs.advprog.everest.authentication.interfaces.JwtTokenParser;
import id.ac.ui.cs.advprog.everest.authentication.interfaces.JwtTokenValidator;
import id.ac.ui.cs.advprog.everest.common.service.AuthServiceGrpcClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationInterceptor.class);
    private static final String ROLE_CLAIM = "role";
    private static final String FULL_NAME_CLAIM = "fullName";
    private static final String CREATED_AT_CLAIM = "createdAt";

    private final JwtTokenParser jwtTokenParser;
    private final JwtTokenValidator jwtTokenValidator;
    private final AuthServiceGrpcClient authServiceGrpcClient;

    public AuthenticationInterceptor(JwtTokenConsumerImpl jwtTokenConsumer,
                                     AuthServiceGrpcClient authServiceGrpcClient) {
        this.jwtTokenParser = jwtTokenConsumer;
        this.jwtTokenValidator = jwtTokenConsumer;
        this.authServiceGrpcClient = authServiceGrpcClient;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            // Not a controller method, skip authentication
            return true;
        }

        Method method = handlerMethod.getMethod();

        // Check if method has @PreAuthorize annotation
        boolean hasPreAuthorize = method.isAnnotationPresent(org.springframework.security.access.prepost.PreAuthorize.class);

        // Check if method has any parameters with @CurrentUser annotation
        boolean hasCurrentUserParam = Arrays.stream(method.getParameters())
                .anyMatch(parameter -> parameter.isAnnotationPresent(CurrentUser.class));

        // If neither annotation is present, skip authentication
        if (!hasPreAuthorize && !hasCurrentUserParam) {
            return true;
        }

        // Now proceed with authentication
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return false;
        }

        String token = authHeader.substring(7);

        try {
            // Always validate the token locally first
            if (!jwtTokenValidator.validateToken(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return false;
            }

            AuthenticatedUser user;

            // Check if we need full user data (when @CurrentUser is present)
            if (hasCurrentUserParam) {
                // Extract user ID from JWT and fetch complete data from auth service
                UUID userId = jwtTokenParser.getUserIdFromToken(token);
                user = fetchCompleteUserData(userId.toString());
                logger.debug("Fetched complete user data from auth service for @CurrentUser");
            } else {
                // Use lightweight approach - extract essential data from JWT claims
                user = createUserFromJwtClaims(token);
                logger.debug("Created user from JWT claims for @PreAuthorize only");
            }

            // Set user context
            UserContext.setUser(user);

            // Create Spring Security authentication
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name()));
            var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return true;

        } catch (AuthServiceException e) {
            logger.error("Auth service unavailable: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Authentication service unavailable");
            return false;
        } catch (AuthenticationException e) {
            logger.warn("Authentication failed: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error during authentication", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error during authentication");
            return false;
        }
    }

    /**
     * Fetches complete user data from auth service using user ID from JWT.
     * This is used when @CurrentUser annotation is present.
     */
    private AuthenticatedUser fetchCompleteUserData(String userId) {
        try {
            UserLookupResponse response = authServiceGrpcClient.lookupUserById(userId);

            if (!response.hasUserData()) {
                throw new AuthenticationException("User not found: " + userId);
            }

            UserData grpcUser = response.getUserData();
            UserIdentity grpcUserIdentity = grpcUser.getIdentity();
            UserProfile grpcUserProfile = grpcUser.getProfile();

            return AuthenticatedUser.builder()
                    .id(UUID.fromString(grpcUserIdentity.getId()))
                    .email(grpcUserIdentity.getEmail())
                    .fullName(grpcUserIdentity.getFullName())
                    .role(grpcUser.getIdentity().getRole())
                    .phoneNumber(grpcUserIdentity.getPhoneNumber())
                    .createdAt(grpcUserIdentity.hasCreatedAt() ?
                            TimestampUtil.toInstant(grpcUserIdentity.getCreatedAt()) : null)
                    .updatedAt(grpcUserIdentity.hasUpdatedAt() ?
                            TimestampUtil.toInstant(grpcUserIdentity.getUpdatedAt()) : null)
                    .address(grpcUserProfile.getAddress())
                    .workExperience(grpcUserProfile.getWorkExperience())
                    .totalJobsDone(grpcUserProfile.getTotalJobsDone())
                    .totalIncome(grpcUserProfile.getTotalIncome())
                    .build();

        } catch (AuthServiceException e) {
            throw e; // Re-throw to be handled by the caller
        } catch (Exception e) {
            throw new AuthServiceException("Failed to fetch user data", e);
        }
    }

    /**
     * Creates an AuthenticatedUser object from JWT claims only.
     * This is used when only @PreAuthorize is present (no @CurrentUser).
     * Contains essential data for authorization but not complete profile.
     */
    private AuthenticatedUser createUserFromJwtClaims(String token) {
        // Extract essential user information from token claims
        String email = jwtTokenParser.getEmailFromToken(token);
        UUID userId = jwtTokenParser.getUserIdFromToken(token);

        // Extract role from token
        String roleString;
        try {
            roleString = jwtTokenParser.getClaimFromToken(token, ROLE_CLAIM, String.class);
            if (roleString == null) {
                roleString = "USER"; // Default role if not specified
            }
        } catch (Exception e) {
            logger.warn("Role claim not found in token, using default role", e);
            roleString = "USER";
        }

        // Extract full name from token
        String fullName;
        try {
            fullName = jwtTokenParser.getClaimFromToken(token, FULL_NAME_CLAIM, String.class);
        } catch (Exception e) {
            logger.debug("Full name claim not found in token", e);
            fullName = null;
        }

        // Extract created at from token
        Instant createdAt;
        try {
            Long createdAtEpoch = jwtTokenParser.getClaimFromToken(token, CREATED_AT_CLAIM, Long.class);
            createdAt = createdAtEpoch != null ? Instant.ofEpochSecond(createdAtEpoch) : null;
        } catch (Exception e) {
            logger.debug("Created at claim not found in token", e);
            createdAt = null;
        }

        UserRole role;
        try {
            role = UserRole.valueOf(roleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid role '{}' in token, defaulting to CUSTOMER", roleString);
            role = UserRole.CUSTOMER; // Adjust default based on your UserRole enum
        }

        return AuthenticatedUser.builder()
                .id(userId)
                .email(email)
                .fullName(fullName)
                .role(role)
                .createdAt(createdAt)
                // Profile data not available from JWT - will be null:
                .phoneNumber(null)
                .address(null)
                .workExperience(null)
                .totalJobsDone(null)
                .totalIncome(null)
                .updatedAt(null)
                .build();
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                @Nullable Exception ex) {
        // Clean up after handling the request
        UserContext.clear();
    }
}
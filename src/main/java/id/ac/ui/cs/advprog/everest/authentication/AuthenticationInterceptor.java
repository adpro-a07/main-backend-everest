package id.ac.ui.cs.advprog.everest.authentication;

import id.ac.ui.cs.advprog.everest.authentication.exception.AuthServiceException;
import id.ac.ui.cs.advprog.everest.authentication.exception.InvalidTokenException;
import id.ac.ui.cs.advprog.everest.common.service.AuthServiceGrpcClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    private final AuthServiceGrpcClient authServiceGrpcClient;

    public AuthenticationInterceptor(AuthServiceGrpcClient authServiceGrpcClient) {
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
            AuthenticatedUser user = authServiceGrpcClient.validateToken(token);
            UserContext.setUser(user);

            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name()));
            var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return true;
        } catch (InvalidTokenException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return false;
        } catch (AuthServiceException e) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Authentication service unavailable");
            return false;
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error during authentication");
            return false;
        }
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
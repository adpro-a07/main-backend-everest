package id.ac.ui.cs.advprog.everest.authentication;

import id.ac.ui.cs.advprog.everest.authentication.exception.AuthServiceException;
import id.ac.ui.cs.advprog.everest.authentication.exception.InvalidTokenException;
import id.ac.ui.cs.advprog.everest.service.AuthServiceGrpcClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthFilter extends OncePerRequestFilter {
    private final AuthServiceGrpcClient authServiceGrpcClient;
    private final PublicRouteMatcher publicRouteMatcher;

    public AuthFilter(AuthServiceGrpcClient authServiceGrpcClient, PublicRouteMatcher publicRouteMatcher) {
        this.authServiceGrpcClient = authServiceGrpcClient;
        this.publicRouteMatcher = publicRouteMatcher;
    }

    @Override
    protected void doFilterInternal
            (@NonNull HttpServletRequest request,
             @NonNull HttpServletResponse response,
             @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        if (publicRouteMatcher.isPublic(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);
        try {
            AuthenticatedUser user = authServiceGrpcClient.validateToken(token);
            UserContext.setUser(user);
            filterChain.doFilter(request, response);
        } catch (InvalidTokenException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        } catch (AuthServiceException e) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Authentication service unavailable");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error during authentication");
        } finally {
            UserContext.clear();
        }
    }
}

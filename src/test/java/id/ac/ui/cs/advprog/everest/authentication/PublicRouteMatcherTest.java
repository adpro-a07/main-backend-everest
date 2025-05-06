package id.ac.ui.cs.advprog.everest.authentication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class PublicRouteMatcherTest {
    private PublicRouteMatcher matcher;

    @BeforeEach
    void setUp() {
        matcher = new PublicRouteMatcher();
        // Manually inject routes (simulating Spring's @ConfigurationProperties binding)
        matcher.setRoutes(List.of(
                "^/api/v1/public($|/.*)",
                "^/health$"
        ));
        // Manually trigger @PostConstruct logic
        matcher.compilePatterns();
    }

    @Test
    void shouldMatchPublicPrefix() {
        assertTrue(matcher.isPublic("/api/v1/public"));
        assertTrue(matcher.isPublic("/api/v1/public/info"));
    }

    @Test
    void shouldMatchExactRoute() {
        assertTrue(matcher.isPublic("/health"));
    }

    @Test
    void shouldNotMatchPrivateRoute() {
        assertFalse(matcher.isPublic("/api/v1/private"));
    }
}

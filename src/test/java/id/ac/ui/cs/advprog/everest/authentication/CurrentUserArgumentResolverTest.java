package id.ac.ui.cs.advprog.everest.authentication;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrentUserArgumentResolverTest {

    private final CurrentUserArgumentResolver resolver = new CurrentUserArgumentResolver();

    static class DummyController {
        public void withCurrentUser(@CurrentUser AuthenticatedUser user) {}
        public void withoutAnnotation(AuthenticatedUser user) {}
        public void wrongType(@CurrentUser String notAUser) {}
    }

    private MethodParameter getMethodParameter(String methodName, Class<?>... paramTypes) throws NoSuchMethodException {
        Method method = DummyController.class.getMethod(methodName, paramTypes);
        return new MethodParameter(method, 0);
    }

    @BeforeEach
    void setup() {
        UserContext.clear();
    }

    @AfterEach
    void teardown() {
        UserContext.clear();
    }

    // =============================
    // TEST supportsParameter
    // =============================

    @Test
    void supportsParameter_returnsTrue_ifAnnotatedWithCurrentUserAndCorrectType() throws Exception {
        MethodParameter parameter = getMethodParameter("withCurrentUser", AuthenticatedUser.class);
        assertTrue(resolver.supportsParameter(parameter));
    }

    @Test
    void supportsParameter_returnsFalse_ifNotAnnotated() throws Exception {
        MethodParameter parameter = getMethodParameter("withoutAnnotation", AuthenticatedUser.class);
        assertFalse(resolver.supportsParameter(parameter));
    }

    @Test
    void supportsParameter_returnsFalse_ifWrongType() throws Exception {
        Method method = DummyController.class.getMethod("wrongType", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        assertFalse(resolver.supportsParameter(parameter));
    }

    // =============================
    // TEST resolveArgument
    // =============================

    @Test
    void resolveArgument_returnsAuthenticatedUserFromUserContext() throws Exception {
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
        UserContext.setUser(user);

        MethodParameter parameter = getMethodParameter("withCurrentUser", AuthenticatedUser.class);

        NativeWebRequest webRequest = mock(NativeWebRequest.class);
        ModelAndViewContainer mavContainer = new ModelAndViewContainer();
        WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);

        Object result = resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

        assertEquals(user, result);
    }

    @Test
    void resolveArgument_returnsNull_ifUserContextIsEmpty() throws Exception {
        MethodParameter parameter = getMethodParameter("withCurrentUser", AuthenticatedUser.class);

        NativeWebRequest webRequest = mock(NativeWebRequest.class);
        ModelAndViewContainer mavContainer = new ModelAndViewContainer();
        WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);

        Object result = resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

        assertNull(result);
    }
}

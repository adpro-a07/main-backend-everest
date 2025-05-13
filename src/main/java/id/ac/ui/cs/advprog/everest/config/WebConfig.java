package id.ac.ui.cs.advprog.everest.config;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticationInterceptor;
import id.ac.ui.cs.advprog.everest.authentication.CurrentUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final CurrentUserArgumentResolver resolver;
    private final AuthenticationInterceptor authInterceptor;

    public WebConfig(CurrentUserArgumentResolver resolver, AuthenticationInterceptor authInterceptor) {
        this.resolver = resolver;
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(resolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor);
    }
}
package id.ac.ui.cs.advprog.everest.authentication;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.regex.Pattern;

@Component
@ConfigurationProperties(prefix = "public")
public class PublicRouteMatcher {

    @Setter
    @Getter
    private List<String> routes;

    private List<Pattern> compiledPatterns;

    @PostConstruct
    public void compilePatterns() {
        this.compiledPatterns = routes.stream()
                .map(Pattern::compile)
                .toList();
    }

    public boolean isPublic(String path) {
        return compiledPatterns.stream().anyMatch(pattern -> pattern.matcher(path).matches());
    }
}

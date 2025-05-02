package id.ac.ui.cs.advprog.everest;

import id.ac.ui.cs.advprog.everest.service.TestService;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class EverestApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );

        ApplicationContext context = SpringApplication.run(EverestApplication.class, args);
        TestService service = context.getBean(TestService.class);
        service.getHello();
    }

}

package id.ac.ui.cs.advprog.everest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
class EverestApplicationTests {

    @Test
    void contextLoads() {
    }

    @RestController
    @RequestMapping("/")
    public static class TestController {
        @GetMapping("/health")
        public String getHealth() {
            return "Health is ok!";
        }
    }
}

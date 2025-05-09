package id.ac.ui.cs.advprog.everest.modules.report.excecption;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ReportExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String,Object>> handleRSE(ResponseStatusException ex,
                                                        HttpServletRequest req) {
        ProblemDetail pd = ex.getBody();
        String reason = pd.getTitle();
        int code = pd.getStatus();
        String detail = ex.getReason();

        Map<String,Object> body = Map.of(
                "status",  code,
                "error",   reason,
                "message", detail,
                "path",    req.getRequestURI()
        );

        return ResponseEntity.status(code)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }
}

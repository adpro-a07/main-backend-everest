package id.ac.ui.cs.advprog.everest.common.exception;

import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void handleNotFound_ShouldReturnNotFoundStatus() {
        // Arrange
        String errorMessage = "Resource not found";
        ResourceNotFoundException exception = new ResourceNotFoundException(errorMessage);

        // Act
        ResponseEntity<GenericResponse<Void>> response = globalExceptionHandler.handleNotFound(exception);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null");
        assertFalse(response.getBody().isSuccess());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertNull(response.getBody().getData());
        // We can't verify logger calls since logger is final
    }

    @Test
    void handleValidationErrors_ShouldReturnBadRequestStatus() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        List<FieldError> fieldErrors = Arrays.asList(
                new FieldError("object", "field1", "must not be blank"),
                new FieldError("object", "field2", "must be positive")
        );

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        when(exception.getMessage()).thenReturn("Validation failed");

        // Act
        ResponseEntity<GenericResponse<Void>> response = globalExceptionHandler.handleValidationErrors(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null");
        assertFalse(response.getBody().isSuccess());
        assertEquals("field1: must not be blank; field2: must be positive", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void handleValidationErrors_WithNoFieldErrors_ShouldReturnBadRequestStatus() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());
        when(exception.getMessage()).thenReturn("Validation failed");

        // Act
        ResponseEntity<GenericResponse<Void>> response = globalExceptionHandler.handleValidationErrors(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null");
        assertFalse(response.getBody().isSuccess());
        assertEquals("", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void handleGrpcError_ShouldReturnServiceUnavailableStatus() {
        // Arrange
        StatusRuntimeException exception = new StatusRuntimeException(Status.UNAVAILABLE.withDescription("Service unavailable"));

        // Act
        ResponseEntity<GenericResponse<Void>> response = globalExceptionHandler.handleGrpcError(exception);

        // Assert
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null");
        assertFalse(response.getBody().isSuccess());
        assertEquals("User service unavailable", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void handleGeneric_ShouldReturnInternalServerErrorStatus() {
        // Arrange
        Exception exception = new RuntimeException("Unexpected error");

        // Act
        ResponseEntity<GenericResponse<Void>> response = globalExceptionHandler.handleGeneric(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null");
        assertFalse(response.getBody().isSuccess());
        assertEquals("Unexpected error occurred", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void handleBaseException_ShouldReturnProvidedStatus() {
        // Arrange
        BaseException exception = new BaseException("Custom error", HttpStatus.CONFLICT) {};

        // Act
        ResponseEntity<GenericResponse<Void>> response = globalExceptionHandler.handleBaseException(exception);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null");
        assertFalse(response.getBody().isSuccess());
        assertEquals("Custom error", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void handleAccessDenied_ShouldReturnForbiddenStatus() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // Act
        ResponseEntity<GenericResponse<Void>> response = globalExceptionHandler.handleAccessDenied(exception);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null");
        assertFalse(response.getBody().isSuccess());
        assertEquals("You are not authorized to access this resource", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void handleMethodNotSupported_ShouldReturnMethodNotAllowedStatus() {
        // Arrange
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("POST");

        // Act
        ResponseEntity<GenericResponse<Void>> response = globalExceptionHandler.handleMethodNotSupported(exception);

        // Assert
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null");
        assertFalse(response.getBody().isSuccess());
        assertEquals("HTTP method POST is not supported for this endpoint", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void handleMessageNotReadable_ShouldReturnBadRequestStatus() {
        // Arrange
        HttpInputMessage httpInputMessage = mock(HttpInputMessage.class);
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Invalid JSON", httpInputMessage);

        // Act
        ResponseEntity<GenericResponse<Void>> response = globalExceptionHandler.handleMessageNotReadable(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null");
        assertFalse(response.getBody().isSuccess());
        assertEquals("Malformed JSON request or invalid request body", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void buildErrorResponse_ShouldCreateResponseWithGivenStatusAndMessage() {
        // This is a private method test using reflection, but we can test it indirectly
        // through the public methods that use it

        // We'll test with a custom BaseException implementation
        HttpStatus customStatus = HttpStatus.GATEWAY_TIMEOUT;
        String customMessage = "Gateway timeout";
        BaseException exception = new BaseException(customMessage, customStatus) {};

        // Act
        ResponseEntity<GenericResponse<Void>> response = globalExceptionHandler.handleBaseException(exception);

        // Assert
        assertEquals(customStatus, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null");
        assertFalse(response.getBody().isSuccess());
        assertEquals(customMessage, response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void handleNoResourceFound_ShouldReturnNotFoundStatus() {
        // Arrange
        NoResourceFoundException exception = new NoResourceFoundException(HttpMethod.GET, "/nonexistent");

        // Act
        ResponseEntity<GenericResponse<Void>> response = globalExceptionHandler.handleNoResourceFound(exception);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody(), "Response body should not be null");
        assertFalse(response.getBody().isSuccess());
        assertEquals("Resource not found", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }
}
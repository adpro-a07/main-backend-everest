package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model;

import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserRequestTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    void testUserRequestCreationWithUserIdAndDescription() {
        // Happy path - testing constructor with userId and description
        UUID userId = UUID.randomUUID();
        String description = "Fix washing machine";
        UserRequest request = new UserRequest(userId, description);

        assertNotNull(request.getRequest_id(), "Request ID should be automatically generated");
        assertEquals(userId, request.getUser_id(), "User ID should match provided value");
        assertEquals(description, request.getUserDescription(), "Description should match provided value");
    }

    @Test
    void testNoArgsConstructor() {
        // Test the no-args constructor
        UserRequest request = new UserRequest();

        assertNull(request.getRequest_id(), "Request ID should be null initially");
        assertNull(request.getUser_id(), "User ID should be null initially");
        assertNull(request.getUserDescription(), "Description should be null initially");
    }

    @Test
    void testAllArgsConstructor() {
        // Test the all-args constructor
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String description = "Repair refrigerator";

        UserRequest request = new UserRequest(requestId, userId, description);

        assertEquals(requestId, request.getRequest_id(), "Request ID should match the provided value");
        assertEquals(userId, request.getUser_id(), "User ID should match the provided value");
        assertEquals(description, request.getUserDescription(), "Description should match the provided value");
    }

    @Test
    void testSetterAndGetterMethods() {
        // Test setter and getter methods
        UserRequest request = new UserRequest();

        UUID requestId = UUID.randomUUID();
        request.setRequest_id(requestId);
        assertEquals(requestId, request.getRequest_id(), "getRequest_id should return the set ID");

        UUID userId = UUID.randomUUID();
        request.setUser_id(userId);
        assertEquals(userId, request.getUser_id(), "getUser_id should return the set ID");

        String description = "Fix air conditioner";
        request.setUserDescription(description);
        assertEquals(description, request.getUserDescription(), "getUserDescription should return the set description");
    }

    @Test
    void testPrePersistMethod() {
        // Test the @PrePersist method by simulating its behavior
        UserRequest request = new UserRequest();
        assertNull(request.getRequest_id(), "Request ID should be null before onCreate");

        // Invoke the method manually to simulate pre-persist behavior
        request.onCreate();

        assertNotNull(request.getRequest_id(), "Request ID should be generated after onCreate");
    }

    @Test
    void testPrePersistDoesNotOverrideExistingId() {
        // Test that @PrePersist doesn't change an existing ID
        UUID originalId = UUID.randomUUID();
        UserRequest request = new UserRequest();
        request.setRequest_id(originalId);

        // Simulate @PrePersist call
        request.onCreate();

        assertEquals(originalId, request.getRequest_id(), "Existing Request ID should not be changed by onCreate");
    }

    @Test
    void testDescriptionSizeValidation() {
        // Bad path - Test description length validation
        String tooLongDescription = "X".repeat(501);
        UserRequest request = new UserRequest();
        request.setUserDescription(tooLongDescription);
        request.setUser_id(UUID.randomUUID()); // Add required user_id

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size(), "Should have one violation for too long description");

        ConstraintViolation<UserRequest> violation = violations.iterator().next();
        assertEquals("userDescription", violation.getPropertyPath().toString());
        assertTrue(violation.getMessageTemplate().contains("Size"));
    }

    @Test
    void testValidDescriptionSize() {
        // Happy path - Test valid description length
        String validDescription = "X".repeat(500);
        UserRequest request = new UserRequest();
        request.setUserDescription(validDescription);
        request.setUser_id(UUID.randomUUID()); // Add required user_id
        request.setRequest_id(UUID.randomUUID()); // Add required request_id

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Should have no violations for valid description length");
    }

    @Test
    void testStringUUIDConversion() {
        // Happy path - Test converting string to UUID
        String validUuidString = "123e4567-e89b-12d3-a456-426614174000";
        UUID uuid = UUID.fromString(validUuidString);

        UserRequest request = new UserRequest();
        request.setUser_id(uuid);

        assertEquals(UUID.fromString(validUuidString), request.getUser_id(),
                "UUID should be correctly converted from string");
    }

    @Test
    void testInvalidUUIDStringConversion() {
        // Bad path - Test invalid UUID string
        String invalidUuidString = "not-a-uuid";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            UUID.fromString(invalidUuidString);
        });

        assertTrue(exception.getMessage().contains("Invalid UUID string"),
                "Should throw exception for invalid UUID format");
    }
}
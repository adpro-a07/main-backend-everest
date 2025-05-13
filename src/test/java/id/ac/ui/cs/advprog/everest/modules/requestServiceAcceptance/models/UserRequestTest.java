package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models;

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
    void testUserRequestCreationWithDescription() {
        // Happy path - testing the single parameter constructor
        String description = "Fix washing machine";
        UserRequest request = new UserRequest(description);

        assertNotNull(request.getId(), "ID should be automatically generated");
        assertEquals(description, request.getUserDescription(), "Description should match provided value");
    }

    @Test
    void testNoArgsConstructor() {
        // Test the no-args constructor
        UserRequest request = new UserRequest();

        assertNull(request.getId(), "ID should be null initially");
        assertNull(request.getUserDescription(), "Description should be null initially");
    }

    @Test
    void testAllArgsConstructor() {
        // Test the all-args constructor
        UUID id = UUID.randomUUID();
        String description = "Repair refrigerator";

        UserRequest request = new UserRequest(id, description);

        assertEquals(id, request.getId(), "ID should match the provided value");
        assertEquals(description, request.getUserDescription(), "Description should match the provided value");
    }

    @Test
    void testSetterAndGetterMethods() {
        // Test setter and getter methods
        UserRequest request = new UserRequest();

        UUID id = UUID.randomUUID();
        request.setId(id);
        assertEquals(id, request.getId(), "getId should return the set ID");

        String description = "Fix air conditioner";
        request.setUserDescription(description);
        assertEquals(description, request.getUserDescription(), "getUserDescription should return the set description");
    }

    @Test
    void testPrePersistMethod() {
        // Test the @PrePersist method by simulating its behavior
        UserRequest request = new UserRequest();
        assertNull(request.getId(), "ID should be null before onCreate");

        // Invoke the method manually to simulate pre-persist behavior
        request.onCreate();

        assertNotNull(request.getId(), "ID should be generated after onCreate");
    }

    @Test
    void testPrePersistDoesNotOverrideExistingId() {
        // Test that @PrePersist doesn't change an existing ID
        UUID originalId = UUID.randomUUID();
        UserRequest request = new UserRequest();
        request.setId(originalId);

        // Simulate @PrePersist call
        request.onCreate();

        assertEquals(originalId, request.getId(), "Existing ID should not be changed by onCreate");
    }

    @Test
    void testDescriptionSizeValidation() {
        // Test description length validation - negative test
        String tooLongDescription = "X".repeat(501);
        UserRequest request = new UserRequest();
        request.setUserDescription(tooLongDescription);

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size(), "Should have one violation for too long description");

        ConstraintViolation<UserRequest> violation = violations.iterator().next();
        assertEquals("userDescription", violation.getPropertyPath().toString());
        assertTrue(violation.getMessageTemplate().contains("Size"));
    }

    @Test
    void testValidDescriptionSize() {
        // Test valid description length - happy path
        String validDescription = "X".repeat(500);
        UserRequest request = new UserRequest();
        request.setUserDescription(validDescription);

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Should have no violations for valid description length");
    }
}
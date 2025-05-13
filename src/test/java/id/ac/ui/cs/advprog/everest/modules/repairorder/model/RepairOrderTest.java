package id.ac.ui.cs.advprog.everest.modules.repairorder.model;

import id.ac.ui.cs.advprog.everest.modules.repairorder.model.enums.RepairOrderStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class RepairOrderTest {

    private Validator validator;
    private UUID validCustomerId;
    private UUID validTechnicianId;
    private String validItemName;
    private String validItemCondition;
    private String validIssueDescription;
    private LocalDate validServiceDate;
    private RepairOrderStatus validStatus;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Valid test data
        validCustomerId = UUID.randomUUID();
        validTechnicianId = UUID.randomUUID();
        validItemName = "Laptop";
        validItemCondition = "Screen cracked";
        validIssueDescription = "The screen is cracked and needs replacement";
        validServiceDate = LocalDate.now().plusDays(2); // Future date
        validStatus = RepairOrderStatus.PENDING_CONFIRMATION;
    }

    @Test
    void testValidRepairOrder() {
        // Create a valid repair order
        RepairOrder repairOrder = RepairOrder.builder()
                .customerId(validCustomerId)
                .technicianId(validTechnicianId)
                .itemName(validItemName)
                .itemCondition(validItemCondition)
                .issueDescription(validIssueDescription)
                .desiredServiceDate(validServiceDate)
                .status(validStatus)
                .build();

        // Validate
        Set<ConstraintViolation<RepairOrder>> violations = validator.validate(repairOrder);
        assertTrue(violations.isEmpty(), "Validation should pass for a valid repair order");

        // Check getters
        assertEquals(validCustomerId, repairOrder.getCustomerId());
        assertEquals(validTechnicianId, repairOrder.getTechnicianId());
        assertEquals(validItemName, repairOrder.getItemName());
        assertEquals(validItemCondition, repairOrder.getItemCondition());
        assertEquals(validIssueDescription, repairOrder.getIssueDescription());
        assertEquals(validServiceDate, repairOrder.getDesiredServiceDate());
        assertEquals(validStatus, repairOrder.getStatus());
    }

    @Test
    void testSetters() {
        // Create a repair order
        RepairOrder repairOrder = new RepairOrder();

        // Use setters
        UUID newCustomerId = UUID.randomUUID();
        UUID newTechnicianId = UUID.randomUUID();
        String newItemName = "Desktop";
        String newItemCondition = "Won't power on";
        String newIssueDescription = "Computer doesn't turn on when pressing power button";
        LocalDate newServiceDate = LocalDate.now().plusDays(5);
        RepairOrderStatus newStatus = RepairOrderStatus.IN_PROGRESS;

        repairOrder.setCustomerId(newCustomerId);
        repairOrder.setTechnicianId(newTechnicianId);
        repairOrder.setItemName(newItemName);
        repairOrder.setItemCondition(newItemCondition);
        repairOrder.setIssueDescription(newIssueDescription);
        repairOrder.setDesiredServiceDate(newServiceDate);
        repairOrder.setStatus(newStatus);

        // Test getters
        assertEquals(newCustomerId, repairOrder.getCustomerId());
        assertEquals(newTechnicianId, repairOrder.getTechnicianId());
        assertEquals(newItemName, repairOrder.getItemName());
        assertEquals(newItemCondition, repairOrder.getItemCondition());
        assertEquals(newIssueDescription, repairOrder.getIssueDescription());
        assertEquals(newServiceDate, repairOrder.getDesiredServiceDate());
        assertEquals(newStatus, repairOrder.getStatus());
    }

    @Test
    void testNullCustomerId() {
        RepairOrder repairOrder = RepairOrder.builder()
                .customerId(null) // Invalid: null customerId
                .technicianId(validTechnicianId)
                .itemName(validItemName)
                .itemCondition(validItemCondition)
                .issueDescription(validIssueDescription)
                .desiredServiceDate(validServiceDate)
                .status(validStatus)
                .build();

        Set<ConstraintViolation<RepairOrder>> violations = validator.validate(repairOrder);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("customerId", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void testNullTechnicianId() {
        RepairOrder repairOrder = RepairOrder.builder()
                .customerId(validCustomerId)
                .technicianId(null) // Invalid: null technicianId
                .itemName(validItemName)
                .itemCondition(validItemCondition)
                .issueDescription(validIssueDescription)
                .desiredServiceDate(validServiceDate)
                .status(validStatus)
                .build();

        Set<ConstraintViolation<RepairOrder>> violations = validator.validate(repairOrder);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("technicianId", violations.iterator().next().getPropertyPath().toString());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidItemNames")
    void testInvalidItemName(String itemName, String expectedViolationType) {
        RepairOrder repairOrder = RepairOrder.builder()
                .customerId(validCustomerId)
                .technicianId(validTechnicianId)
                .itemName(itemName)
                .itemCondition(validItemCondition)
                .issueDescription(validIssueDescription)
                .desiredServiceDate(validServiceDate)
                .status(validStatus)
                .build();

        Set<ConstraintViolation<RepairOrder>> violations = validator.validate(repairOrder);
        assertFalse(violations.isEmpty());

        // Find violation for itemName
        ConstraintViolation<RepairOrder> violation = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("itemName"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected violation for itemName not found"));

        // Check if the violation message contains the expected constraint type
        assertTrue(violation.getMessageTemplate().contains(expectedViolationType),
                "Expected violation of type " + expectedViolationType);
    }

    private static Stream<Arguments> provideInvalidItemNames() {
        return Stream.of(
                Arguments.of(null, "NotBlank"),
                Arguments.of("", "NotBlank"),
                Arguments.of("   ", "NotBlank"),
                Arguments.of("A".repeat(101), "Size") // Too long (max 100)
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidItemConditions")
    void testInvalidItemCondition(String itemCondition, String expectedViolationType) {
        RepairOrder repairOrder = RepairOrder.builder()
                .customerId(validCustomerId)
                .technicianId(validTechnicianId)
                .itemName(validItemName)
                .itemCondition(itemCondition)
                .issueDescription(validIssueDescription)
                .desiredServiceDate(validServiceDate)
                .status(validStatus)
                .build();

        Set<ConstraintViolation<RepairOrder>> violations = validator.validate(repairOrder);
        assertFalse(violations.isEmpty());

        // Find violation for itemCondition
        ConstraintViolation<RepairOrder> violation = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("itemCondition"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected violation for itemCondition not found"));

        // Check if the violation message contains the expected constraint type
        assertTrue(violation.getMessageTemplate().contains(expectedViolationType),
                "Expected violation of type " + expectedViolationType);
    }

    private static Stream<Arguments> provideInvalidItemConditions() {
        return Stream.of(
                Arguments.of(null, "NotBlank"),
                Arguments.of("", "NotBlank"),
                Arguments.of("   ", "NotBlank"),
                Arguments.of("A".repeat(101), "Size") // Too long (max 100)
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidIssueDescriptions")
    void testInvalidIssueDescription(String issueDescription, String expectedViolationType) {
        RepairOrder repairOrder = RepairOrder.builder()
                .customerId(validCustomerId)
                .technicianId(validTechnicianId)
                .itemName(validItemName)
                .itemCondition(validItemCondition)
                .issueDescription(issueDescription)
                .desiredServiceDate(validServiceDate)
                .status(validStatus)
                .build();

        Set<ConstraintViolation<RepairOrder>> violations = validator.validate(repairOrder);
        assertFalse(violations.isEmpty());

        // Find violation for issueDescription
        ConstraintViolation<RepairOrder> violation = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("issueDescription"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected violation for issueDescription not found"));

        // Check if the violation message contains the expected constraint type
        assertTrue(violation.getMessageTemplate().contains(expectedViolationType),
                "Expected violation of type " + expectedViolationType);
    }

    private static Stream<Arguments> provideInvalidIssueDescriptions() {
        return Stream.of(
                Arguments.of(null, "NotBlank"),
                Arguments.of("", "NotBlank"),
                Arguments.of("   ", "NotBlank"),
                Arguments.of("A".repeat(501), "Size") // Too long (max 500)
        );
    }

    @Test
    void testPastServiceDate() {
        LocalDate pastDate = LocalDate.now().minusDays(1);

        RepairOrder repairOrder = RepairOrder.builder()
                .customerId(validCustomerId)
                .technicianId(validTechnicianId)
                .itemName(validItemName)
                .itemCondition(validItemCondition)
                .issueDescription(validIssueDescription)
                .desiredServiceDate(pastDate)
                .status(validStatus)
                .build();

        Set<ConstraintViolation<RepairOrder>> violations = validator.validate(repairOrder);
        assertFalse(violations.isEmpty());

        // Find violation for desiredServiceDate
        ConstraintViolation<RepairOrder> violation = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("desiredServiceDate"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected violation for desiredServiceDate not found"));

        // Check if the violation is related to @FutureOrPresent
        assertTrue(violation.getMessageTemplate().contains("FutureOrPresent"),
                "Expected violation for past date to be related to @FutureOrPresent");
    }

    @Test
    void testCurrentDateServiceDate() {
        LocalDate currentDate = LocalDate.now();

        RepairOrder repairOrder = RepairOrder.builder()
                .customerId(validCustomerId)
                .technicianId(validTechnicianId)
                .itemName(validItemName)
                .itemCondition(validItemCondition)
                .issueDescription(validIssueDescription)
                .desiredServiceDate(currentDate)
                .status(validStatus)
                .build();

        Set<ConstraintViolation<RepairOrder>> violations = validator.validate(repairOrder);
        assertTrue(violations.isEmpty(), "Current date should be valid with @FutureOrPresent");
    }

    @Test
    void testNullServiceDate() {
        RepairOrder repairOrder = RepairOrder.builder()
                .customerId(validCustomerId)
                .technicianId(validTechnicianId)
                .itemName(validItemName)
                .itemCondition(validItemCondition)
                .issueDescription(validIssueDescription)
                .desiredServiceDate(null)
                .status(validStatus)
                .build();

        Set<ConstraintViolation<RepairOrder>> violations = validator.validate(repairOrder);
        assertFalse(violations.isEmpty());

        // Find violation for desiredServiceDate
        ConstraintViolation<RepairOrder> violation = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("desiredServiceDate"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected violation for desiredServiceDate not found"));

        // Check if the violation is related to @NotNull
        assertTrue(violation.getMessageTemplate().contains("NotNull"),
                "Expected violation for null date to be related to @NotNull");
    }

    @Test
    void testNullStatus() {
        RepairOrder repairOrder = RepairOrder.builder()
                .customerId(validCustomerId)
                .technicianId(validTechnicianId)
                .itemName(validItemName)
                .itemCondition(validItemCondition)
                .issueDescription(validIssueDescription)
                .desiredServiceDate(validServiceDate)
                .status(null)
                .build();

        Set<ConstraintViolation<RepairOrder>> violations = validator.validate(repairOrder);
        assertFalse(violations.isEmpty());

        // Find violation for status
        ConstraintViolation<RepairOrder> violation = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("status"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected violation for status not found"));

        // Check if the violation is related to @NotNull
        assertTrue(violation.getMessageTemplate().contains("NotNull"),
                "Expected violation for null status to be related to @NotNull");
    }

    @Test
    void testAllRepairOrderStatuses() {
        // Test all enum values can be used
        for (RepairOrderStatus status : RepairOrderStatus.values()) {
            RepairOrder repairOrder = RepairOrder.builder()
                    .customerId(validCustomerId)
                    .technicianId(validTechnicianId)
                    .itemName(validItemName)
                    .itemCondition(validItemCondition)
                    .issueDescription(validIssueDescription)
                    .desiredServiceDate(validServiceDate)
                    .status(status)
                    .build();

            Set<ConstraintViolation<RepairOrder>> violations = validator.validate(repairOrder);
            assertTrue(violations.isEmpty(), "Validation should pass for status " + status);
            assertEquals(status, repairOrder.getStatus());
        }
    }

    @Test
    void testMultipleValidationErrors() {
        // Create a repair order with multiple validation errors
        RepairOrder repairOrder = RepairOrder.builder()
                .customerId(null)
                .technicianId(null)
                .itemName("")
                .itemCondition("")
                .issueDescription("")
                .desiredServiceDate(LocalDate.now().minusDays(1))
                .status(null)
                .build();

        Set<ConstraintViolation<RepairOrder>> violations = validator.validate(repairOrder);
        // We expect 7 violations (null customerId, null technicianId, blank itemName,
        // blank itemCondition, blank issueDescription, past date, null status)
        assertEquals(7, violations.size(), "Should have 7 constraint violations");
    }
}
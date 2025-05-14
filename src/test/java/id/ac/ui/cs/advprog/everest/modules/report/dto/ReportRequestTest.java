package id.ac.ui.cs.advprog.everest.modules.report.dto;

import id.ac.ui.cs.advprog.everest.modules.report.model.enums.ReportStatus;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ReportRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldPassValidation() {
        ReportRequest request = ReportRequest.builder()
                .technicianName("John Doe")
                .repairDetails("Replaced motherboard")
                .repairDate(LocalDate.now())
                .status(ReportStatus.COMPLETED)
                .build();

        var violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenTechnicianNameBlank() {
        ReportRequest request = ReportRequest.builder()
                .technicianName("")
                .repairDetails("Repair details")
                .repairDate(LocalDate.now())
                .status(ReportStatus.IN_PROGRESS)
                .build();

        var violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Technician name is required", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenRepairDateNull() {
        ReportRequest request = ReportRequest.builder()
                .technicianName("Alice Smith")
                .repairDetails("Repair details")
                .repairDate(null)
                .status(ReportStatus.PENDING_CONFIRMATION)
                .build();

        var violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Repair date is required", violations.iterator().next().getMessage());
    }
}

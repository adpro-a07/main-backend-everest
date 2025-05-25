package id.ac.ui.cs.advprog.everest.modules.technicianReport.controller;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.authentication.CurrentUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.dto.CreateTechnicianReportDraftRequest;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.dto.TechnicianReportDraftResponse;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.service.TechnicianReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/technician-reports")
public class TechnicianReportController {
    private final TechnicianReportService technicianReportService;

    public TechnicianReportController(TechnicianReportService technicianReportService) {
        this.technicianReportService = technicianReportService;
    }

    @PreAuthorize("hasRole('TECHNICIAN')")
    @GetMapping
    public ResponseEntity<?> getTechnicianReportByStatusForTechnician(
            @RequestParam(value = "status", required = false) String status,
            @CurrentUser AuthenticatedUser user
    ) {
        GenericResponse<List<TechnicianReportDraftResponse>> response = technicianReportService
                .getTechnicianReportByStatusForTechnician(status, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('TECHNICIAN')")
    @PostMapping
    public ResponseEntity<?> createTechnicianReportDraft(
            @Valid @RequestBody CreateTechnicianReportDraftRequest createTechnicianReportDraft,
            @CurrentUser AuthenticatedUser user
    ) {
        GenericResponse<TechnicianReportDraftResponse> response = technicianReportService
                .createTechnicianReportDraft(createTechnicianReportDraft, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('TECHNICIAN')")
    @PutMapping("/{reportId}")
    public ResponseEntity<?> updateTechnicianReportDraft(
            @PathVariable String reportId,
            @Valid @RequestBody CreateTechnicianReportDraftRequest createTechnicianReportDraft,
            @CurrentUser AuthenticatedUser user
    ) {
        GenericResponse<TechnicianReportDraftResponse> response = technicianReportService
                .updateTechnicianReportDraft(reportId, createTechnicianReportDraft, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('TECHNICIAN')")
    @DeleteMapping("/{reportId}")
    public ResponseEntity<?> deleteTechnicianReportDraft(
            @PathVariable String reportId,
            @CurrentUser AuthenticatedUser user
    ) {
        GenericResponse<TechnicianReportDraftResponse> response = technicianReportService
                .deleteTechnicianReportDraft(reportId, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('TECHNICIAN')")
    @PostMapping("/{reportId}/start")
    public ResponseEntity<?> startWork(
            @PathVariable String reportId,
            @CurrentUser AuthenticatedUser user
    ) {
        GenericResponse<TechnicianReportDraftResponse> response = technicianReportService
                .startWork(reportId, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('TECHNICIAN')")
    @PostMapping("/{reportId}/complete")
    public ResponseEntity<?> completeWork(
            @PathVariable String reportId,
            @CurrentUser AuthenticatedUser user
    ) {
        GenericResponse<TechnicianReportDraftResponse> response = technicianReportService
                .completeWork(reportId, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/for-customer")
    public ResponseEntity<?> getTechnicianReportByStatusForCustomer(
            @RequestParam(value = "status", required = false) String status,
            @CurrentUser AuthenticatedUser user
    ) {
        GenericResponse<List<TechnicianReportDraftResponse>> response = technicianReportService
                .getTechnicianReportByStatusForCustomer(status, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/{reportId}/accept")
    public ResponseEntity<?> acceptTechnicianReportSubmit(
            @PathVariable String reportId,
            @CurrentUser AuthenticatedUser user
    ) {
        GenericResponse<Void> response = technicianReportService
                .acceptTechnicianReportSubmit(reportId, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/{reportId}/reject")
    public ResponseEntity<?> rejectTechnicianReportSubmit(
            @PathVariable String reportId,
            @CurrentUser AuthenticatedUser user
    ) {
        GenericResponse<Void> response = technicianReportService
                .rejectTechnicianReportSubmit(reportId, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('TECHNICIAN')")
    @PostMapping("/{reportId}/submit")
    public ResponseEntity<?> submitTechnicianReportDraft(
            @PathVariable String reportId,
            @CurrentUser AuthenticatedUser technician
    ) {
        GenericResponse<TechnicianReportDraftResponse> response = technicianReportService
                .submitTechnicianReportDraft(reportId, technician);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('TECHNICIAN', 'CUSTOMER')")
    @GetMapping("/{reportId}")
    public ResponseEntity<?> getTechnicianReportById(
            @PathVariable String reportId,
            @CurrentUser AuthenticatedUser user
    ) {
        GenericResponse<TechnicianReportDraftResponse> response = technicianReportService
                .getTechnicianReportById(reportId, user);
        return ResponseEntity.ok(response);
    }
}
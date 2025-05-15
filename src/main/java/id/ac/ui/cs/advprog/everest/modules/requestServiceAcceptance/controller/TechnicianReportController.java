package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.controller;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.authentication.CurrentUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.CreateTechnicianReportDraft;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.TechnicianReportDraftResponse;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.service.TechnicianReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class TechnicianReportController {
    private final TechnicianReportService technicianReportService;

    public TechnicianReportController(TechnicianReportService technicianReportService) {
        this.technicianReportService = technicianReportService;
    }

    @PreAuthorize("hasRole('TECHNICIAN')")
    @PostMapping("/technician-reports")
    public ResponseEntity<?> createTechnicianReportDraft(
            @Valid @RequestBody CreateTechnicianReportDraft createTechnicianReportDraft,
            @CurrentUser AuthenticatedUser user
    ) {
        GenericResponse<TechnicianReportDraftResponse> response = technicianReportService
                .createTechnicianReportDraft(createTechnicianReportDraft, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('TECHNICIAN')")
    @PutMapping("/technician-reports/{reportId}")
    public ResponseEntity<?> updateTechnicianReportDraft(
            @PathVariable String reportId,
            @Valid @RequestBody CreateTechnicianReportDraft createTechnicianReportDraft,
            @CurrentUser AuthenticatedUser user
    ) {
        GenericResponse<TechnicianReportDraftResponse> response = technicianReportService
                .updateTechnicianReportDraft(reportId, createTechnicianReportDraft, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('TECHNICIAN')")
    @DeleteMapping("/technician-reports/{reportId}")
    public ResponseEntity<?> deleteTechnicianReportDraft(
            @PathVariable String reportId,
            @CurrentUser AuthenticatedUser user
    ) {
        GenericResponse<TechnicianReportDraftResponse> response = technicianReportService
                .deleteTechnicianReportDraft(reportId, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/technician-reports/{reportId}/accept")
    public ResponseEntity<?> acceptTechnicianReportDraft(
            @PathVariable String reportId,
            @CurrentUser AuthenticatedUser user
    ) {
        GenericResponse<Void> response = technicianReportService
                .acceptTechnicianReportSubmit(reportId, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/technician-reports/{reportId}/reject")
    public ResponseEntity<?> rejectTechnicianReportDraft(
            @PathVariable String reportId,
            @CurrentUser AuthenticatedUser user
    ) {
        GenericResponse<Void> response = technicianReportService
                .rejectTechnicianReportSubmit(reportId, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('TECHNICIAN')")
    @PostMapping("/technician-reports/{reportId}/submit")
    public ResponseEntity<?> submitTechnicianReportDraft(
            @PathVariable String reportId,
            @CurrentUser AuthenticatedUser technician
    ) {
        GenericResponse<TechnicianReportDraftResponse> response = technicianReportService
                .submitTechnicianReportDraft(reportId, technician);
        return ResponseEntity.ok(response);
    }
}
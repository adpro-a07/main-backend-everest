package id.ac.ui.cs.advprog.everest.modules.report.controller;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.authentication.CurrentUser;
import id.ac.ui.cs.advprog.everest.modules.report.dto.ReportResponse;
import id.ac.ui.cs.advprog.everest.modules.report.model.enums.ReportStatus;
import id.ac.ui.cs.advprog.everest.modules.report.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<ReportResponse>> getReportList(
            @RequestParam(required = false) String technician,
            @RequestParam(required = false) ReportStatus status,
            @CurrentUser AuthenticatedUser user
    ) {

        List<ReportResponse> reports;
        if (technician != null && status != null) {
            reports = reportService.getReportsByTechnicianAndStatus(technician, status, user);
        } else if (technician != null) {
            reports = reportService.getReportsByTechnician(technician, user);
        } else if (status != null) {
            reports = reportService.getReportsByStatus(status, user);
        } else {
            reports = reportService.getAllReports(user);
        }
        return ResponseEntity.ok(reports);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> getReportDetailById(
            @PathVariable UUID id,
            @CurrentUser AuthenticatedUser user
    ) {
        try {
            ReportResponse report = reportService.getReportById(id, user);
            return ResponseEntity.ok(report);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Report not found with id: " + id,
                    ex
            );
        }
    }
}

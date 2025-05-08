package id.ac.ui.cs.advprog.everest.modules.report.controller;

import id.ac.ui.cs.advprog.everest.modules.report.model.Report;
import id.ac.ui.cs.advprog.everest.modules.report.model.enums.ReportStatus;
import id.ac.ui.cs.advprog.everest.modules.report.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<List<Report>> getReportList(
            @RequestParam(required = false) String technician,
            @RequestParam(required = false) ReportStatus status) {

        List<Report> reports;
        if (technician != null && status != null) {
            reports = reportService.getReportsByTechnicianAndStatus(technician, status);
        } else if (technician != null) {
            reports = reportService.getReportsByTechnician(technician);
        } else if (status != null) {
            reports = reportService.getReportsByStatus(status);
        } else {
            reports = reportService.getAllReports();
        }
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Report> getReportDetailById(@PathVariable int id) {
        try {
            Report rpt = reportService.getReportById(id);
            return ResponseEntity.ok(rpt);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Report not found with id: " + id,
                    ex
            );
        }
    }

    @ControllerAdvice
    public class ControllerExceptionHandler {

        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
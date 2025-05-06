package id.ac.ui.cs.advprog.everest.controller;

import id.ac.ui.cs.advprog.everest.model.Report;
import id.ac.ui.cs.advprog.everest.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String viewReports(
            @RequestParam(required = false) String technician,
            @RequestParam(required = false) String status,
            Model model) {

        List<Report> reports;

        // Handle null parameters
        technician = (technician != null && !technician.isEmpty()) ? technician : null;
        status = (status != null && !status.isEmpty()) ? status : null;

        if (technician != null && status != null) {
            reports = reportService.getReportsByTechnicianAndStatus(technician, status);
        } else if (technician != null) {
            reports = reportService.getReportsByTechnician(technician);
        } else if (status != null) {
            reports = reportService.getReportsByStatus(status);
        } else {
            reports = reportService.getAllReports();
        }

        model.addAttribute("reports", reports);
        model.addAttribute("currentTechnician", technician != null ? technician : "");
        model.addAttribute("currentStatus", status != null ? status : "");

        return "report/list";
    }

    @GetMapping("/{id}")
    public String viewReportDetail(@PathVariable int id, Model model) {
        try {
            Report report = reportService.getReportById(id);
            model.addAttribute("report", report);
            return "report/detail";
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
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
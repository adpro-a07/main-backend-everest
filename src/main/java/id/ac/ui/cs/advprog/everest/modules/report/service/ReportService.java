package id.ac.ui.cs.advprog.everest.modules.report.service;

import id.ac.ui.cs.advprog.everest.modules.report.model.enums.ReportStatus;
import id.ac.ui.cs.advprog.everest.modules.report.model.Report;

import java.util.List;
import java.util.UUID;

public interface ReportService {
    List<Report> getAllReports();
    Report getReportById(UUID id);
    List<Report> getReportsByTechnician(String technicianName);
    List<Report> getReportsByStatus(ReportStatus status);
    List<Report> getReportsByTechnicianAndStatus(String technicianName, ReportStatus status);
}
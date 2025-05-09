package id.ac.ui.cs.advprog.everest.modules.report.service;

import id.ac.ui.cs.advprog.everest.modules.report.dto.ReportResponse;
import id.ac.ui.cs.advprog.everest.modules.report.model.enums.ReportStatus;
import id.ac.ui.cs.advprog.everest.modules.report.model.Report;

import java.util.List;
import java.util.UUID;

public interface ReportService {
    List<ReportResponse> getAllReports();
    ReportResponse getReportById(UUID id);
    List<ReportResponse> getReportsByTechnician(String technicianName);
    List<ReportResponse> getReportsByStatus(ReportStatus status);
    List<ReportResponse> getReportsByTechnicianAndStatus(String technicianName, ReportStatus status);
}
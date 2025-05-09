package id.ac.ui.cs.advprog.everest.modules.report.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.modules.report.dto.ReportResponse;
import id.ac.ui.cs.advprog.everest.modules.report.model.enums.ReportStatus;

import java.util.List;
import java.util.UUID;

public interface ReportService {
    List<ReportResponse> getAllReports(AuthenticatedUser user);
    ReportResponse getReportById(UUID id, AuthenticatedUser user);
    List<ReportResponse> getReportsByTechnician(String technicianName, AuthenticatedUser user);
    List<ReportResponse> getReportsByStatus(ReportStatus status, AuthenticatedUser user);
    List<ReportResponse> getReportsByTechnicianAndStatus(String technicianName, ReportStatus status, AuthenticatedUser user);
}
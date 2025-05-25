package id.ac.ui.cs.advprog.everest.modules.report.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.modules.report.dto.ReportResponse;

import java.util.List;
import java.util.UUID;

public interface ReportService {
    List<ReportResponse> getAllReports(AuthenticatedUser user);
    ReportResponse getReportById(UUID id, AuthenticatedUser user);
    List<ReportResponse> getReportsByStatus(String status, AuthenticatedUser user);
    List<ReportResponse> getReportsByDiagnosis(String diagnosis, AuthenticatedUser user);
    List<ReportResponse> getReportsByActionPlan(String actionPlan, AuthenticatedUser user);
    List<ReportResponse> getReportsByTechnicianId(UUID technicianId, AuthenticatedUser user);
}
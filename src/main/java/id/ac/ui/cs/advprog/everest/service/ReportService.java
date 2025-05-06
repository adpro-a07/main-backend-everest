package id.ac.ui.cs.advprog.everest.service;

import id.ac.ui.cs.advprog.everest.model.Report;
import id.ac.ui.cs.advprog.everest.model.enums.ReportStatus;

import java.util.List;

public interface ReportService {
    List<Report> getAllReports();
    Report getReportById(int id);
    List<Report> getReportsByTechnician(String technicianName);
    List<Report> getReportsByStatus(ReportStatus status);
    List<Report> getReportsByTechnicianAndStatus(String technicianName, ReportStatus status);
}
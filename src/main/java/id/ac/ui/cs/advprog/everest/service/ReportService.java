package id.ac.ui.cs.advprog.everest.service;

import id.ac.ui.cs.advprog.everest.model.Report;
import java.util.List;

public interface ReportService {
    List<Report> getAllReports();
    Report getReportById(int id);
    List<Report> getReportsByTechnician(String technicianName);
    List<Report> getReportsByStatus(String status);
    List<Report> getReportsByTechnicianAndStatus(String technicianName, String status);
}
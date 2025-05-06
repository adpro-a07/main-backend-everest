package id.ac.ui.cs.advprog.everest.service;

import id.ac.ui.cs.advprog.everest.model.Report;
import id.ac.ui.cs.advprog.everest.model.enums.ReportStatus;
import id.ac.ui.cs.advprog.everest.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;

    @Autowired
    public ReportServiceImpl(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    @Override
    public Report getReportById(int id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
    }

    @Override
    public List<Report> getReportsByTechnician(String technicianName) {
        return reportRepository.findByTechnicianNameContainingIgnoreCase(technicianName);
    }

    @Override
    public List<Report> getReportsByStatus(ReportStatus status) {
        return reportRepository.findByStatus(status);
    }

    @Override
    public List<Report> getReportsByTechnicianAndStatus(String technicianName, ReportStatus status) {
        return reportRepository.findByTechnicianNameContainingIgnoreCaseAndStatus(technicianName, status);
    }
}
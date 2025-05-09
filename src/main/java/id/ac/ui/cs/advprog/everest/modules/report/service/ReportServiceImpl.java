package id.ac.ui.cs.advprog.everest.modules.report.service;


import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.modules.report.dto.ReportResponse;
import id.ac.ui.cs.advprog.everest.modules.report.model.enums.ReportStatus;
import id.ac.ui.cs.advprog.everest.modules.report.model.Report;
import id.ac.ui.cs.advprog.everest.modules.report.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;

    @Autowired
    public ReportServiceImpl(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public List<ReportResponse> getAllReports(AuthenticatedUser user) {
        return reportRepository.findAll()
                .stream()
                .map(this::mapToReportResponse)
                .toList();
    }

    @Override
    public ReportResponse getReportById(UUID id, AuthenticatedUser user) {
        // you may check here that a TECHNICIAN only fetches their own report
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
        return mapToReportResponse(report);
    }

    @Override
    public List<ReportResponse> getReportsByTechnician(String technicianName, AuthenticatedUser user) {
        return reportRepository
                .findByTechnicianNameContainingIgnoreCase(technicianName)
                .stream()
                .map(this::mapToReportResponse)
                .toList();
    }

    @Override
    public List<ReportResponse> getReportsByStatus(ReportStatus status, AuthenticatedUser user) {
        return reportRepository
                .findByStatus(status)
                .stream()
                .map(this::mapToReportResponse)
                .toList();
    }

    @Override
    public List<ReportResponse> getReportsByTechnicianAndStatus(String technicianName, ReportStatus status, AuthenticatedUser user) {
        return reportRepository
                .findByTechnicianNameContainingIgnoreCaseAndStatus(technicianName, status)
                .stream()
                .map(this::mapToReportResponse)
                .toList();
    }

    private ReportResponse mapToReportResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .technicianName(report.getTechnicianName())
                .repairDetails(report.getRepairDetails())
                .repairDate(report.getRepairDate())
                .status(report.getStatus().name())
                .build();
    }
}


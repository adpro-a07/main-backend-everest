package id.ac.ui.cs.advprog.everest.modules.report.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.exception.ResourceNotFoundException;
import id.ac.ui.cs.advprog.everest.common.exception.ValidationException;
import id.ac.ui.cs.advprog.everest.modules.report.dto.ReportResponse;
import id.ac.ui.cs.advprog.everest.modules.report.repository.ReportRepository;
import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.TechnicianReport;
import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.state.CompletedState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private static final String COMPLETED_STATUS = new CompletedState().getName();;

    @Autowired
    public ReportServiceImpl(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public List<ReportResponse> getAllReports(AuthenticatedUser user) {
        return reportRepository.findByStatus(COMPLETED_STATUS)
                .stream()
                .map(this::mapToReportResponse)
                .toList();
    }

    @Override
    public ReportResponse getReportById(UUID id, AuthenticatedUser user) {
        TechnicianReport report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
        if (!COMPLETED_STATUS.equals(report.getStatus())) {
            throw new ValidationException("Report is not completed");
        }
        return mapToReportResponse(report);
    }

    @Override
    public List<ReportResponse> getReportsByStatus(String status, AuthenticatedUser user) {
        if (!COMPLETED_STATUS.equals(status)) {
            return List.of();
        }
        return reportRepository
                .findByStatus(COMPLETED_STATUS)
                .stream()
                .map(this::mapToReportResponse)
                .toList();
    }

    @Override
    public List<ReportResponse> getReportsByDiagnosis(String diagnosis, AuthenticatedUser user) {
        return reportRepository
                .findByDiagnosisContainingIgnoreCaseAndStatus(diagnosis, COMPLETED_STATUS)
                .stream()
                .map(this::mapToReportResponse)
                .toList();
    }

    @Override
    public List<ReportResponse> getReportsByActionPlan(String actionPlan, AuthenticatedUser user) {
        return reportRepository
                .findByActionPlanContainingIgnoreCaseAndStatus(actionPlan, COMPLETED_STATUS)
                .stream()
                .map(this::mapToReportResponse)
                .toList();
    }

    @Override
    public List<ReportResponse> getReportsByTechnicianId(UUID technicianId, AuthenticatedUser user) {
        return reportRepository
                .findByTechnicianIdAndStatus(technicianId, COMPLETED_STATUS)
                .stream()
                .map(this::mapToReportResponse)
                .toList();
    }

    private ReportResponse mapToReportResponse(TechnicianReport report) {
        return ReportResponse.builder()
                .id(report.getReportId())
                .technicianId(report.getTechnicianId())
                .diagnosis(report.getDiagnosis())
                .actionPlan(report.getActionPlan())
                .estimatedCost(report.getEstimatedCost())
                .estimatedTimeSeconds(report.getEstimatedTimeSeconds())
                .status(report.getStatus())
                .lastUpdatedAt(report.getLastUpdatedAt())
                .build();
    }
}
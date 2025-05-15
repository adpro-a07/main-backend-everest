package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.CreateTechnicianReportDraft;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.TechnicianReportDraftResponse;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.exception.DatabaseException;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.exception.IllegalStateTransitionException;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.exception.InvalidTechnicianReportStateException;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model.TechnicianReport;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model.UserRequest;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.TechnicianReportRepository;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.UserRequestRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.upperCase;

@Service
public class TechnicianReportServiceImpl implements TechnicianReportService {

    private final TechnicianReportRepository technicianReportRepository;
    private final UserRequestRepository userRequestRepository;

    public TechnicianReportServiceImpl(
            TechnicianReportRepository technicianReportRepository,
            UserRequestRepository userRequestRepository) {
        this.technicianReportRepository = technicianReportRepository;
        this.userRequestRepository = userRequestRepository;
    }

    @Override
    public GenericResponse<List<TechnicianReportDraftResponse>> getTechnicianReportByStatus(String status, AuthenticatedUser technician) {
        if (technician == null) {
            return new GenericResponse<>(false, "Technician cannot be null", null);
        }

        try {
            List<TechnicianReport> reports = technicianReportRepository.findAllByTechnicianIdAndStatus(technician.id(), upperCase(status));
            List<TechnicianReportDraftResponse> response = reports.stream()
                    .map(this::buildTechnicianReportDraftResponse)
                    .toList();
            return new GenericResponse<>(true, "Technician reports retrieved successfully", response);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Failed to retrieve technician reports", ex);
        }
    }

    @Override
    public GenericResponse<TechnicianReportDraftResponse> createTechnicianReportDraft(
            CreateTechnicianReportDraft createTechnicianReportDraft,
            AuthenticatedUser technician) {

        if (createTechnicianReportDraft == null || technician == null) {
            return new GenericResponse<>(false, "Report data or technician cannot be null", null);
        }

        String userRequestId = createTechnicianReportDraft.getUserRequestId();
        if (userRequestId == null || userRequestId.isEmpty()) {
            return new GenericResponse<>(false, "User request ID cannot be null or empty", null);
        }

        try {
            // Find the user request
            UserRequest userRequest = userRequestRepository.findById(UUID.fromString(userRequestId))
                    .orElseThrow(() -> new InvalidTechnicianReportStateException("User request not found"));

            // Create new technician report with DRAFT status
            TechnicianReport technicianReport = TechnicianReport.builder()
                    .reportId(UUID.randomUUID())
                    .userRequest(userRequest)
                    .technicianId(technician.id())
                    .diagnosis(createTechnicianReportDraft.getDiagnosis())
                    .actionPlan(createTechnicianReportDraft.getActionPlan())
                    .estimatedCost(createTechnicianReportDraft.getEstimatedCost())
                    .estimatedTime(Duration.ofSeconds(createTechnicianReportDraft.getEstimatedTimeSeconds()))
                    .build();

            // Save the report
            TechnicianReport savedReport = technicianReportRepository.save(technicianReport);

            // Create and return response
            TechnicianReportDraftResponse response = buildTechnicianReportDraftResponse(savedReport);

            return new GenericResponse<>(true, "Technician report draft created successfully", response);
        } catch (IllegalArgumentException | DataAccessException | InvalidTechnicianReportStateException |
        IllegalStateTransitionException ex) {
            return new GenericResponse<>(false, ex.getMessage(), null);
        }
    }

    @Override
    public GenericResponse<TechnicianReportDraftResponse> updateTechnicianReportDraft(
            String technicianReportDraftId,
            CreateTechnicianReportDraft createTechnicianReportDraft,
            AuthenticatedUser technician) {

        if (technicianReportDraftId == null || createTechnicianReportDraft == null || technician == null) {
            return new GenericResponse<>(false, "Report data or technician cannot be null", null);
        }

        try {
            TechnicianReport technicianReport = technicianReportRepository.findByReportId(UUID.fromString(technicianReportDraftId))
                    .orElseThrow(() -> new InvalidTechnicianReportStateException("Technician report not found"));

            // Verify the report belongs to this technician
            if (!technicianReport.getTechnicianId().equals(technician.id())) {
                throw new InvalidTechnicianReportStateException("You are not authorized to update this report");
            }

            // Verify report is in DRAFT state
            if (!"DRAFT".equals(technicianReport.getStatus())) {
                throw new InvalidTechnicianReportStateException("Only report drafts can be updated");
            }

            // Update the report fields
            technicianReport.setDiagnosis(createTechnicianReportDraft.getDiagnosis());
            technicianReport.setActionPlan(createTechnicianReportDraft.getActionPlan());
            technicianReport.setEstimatedCost(createTechnicianReportDraft.getEstimatedCost());
            technicianReport.setEstimatedTimeSeconds(createTechnicianReportDraft.getEstimatedTimeSeconds());

            // Save updated report
            TechnicianReport updatedReport = technicianReportRepository.save(technicianReport);

            // Create and return response
            TechnicianReportDraftResponse response = buildTechnicianReportDraftResponse(updatedReport);

            return new GenericResponse<>(true, "Technician report draft updated successfully", response);
        } catch (IllegalArgumentException | DataAccessException | InvalidTechnicianReportStateException |
                 IllegalStateTransitionException ex) {
            return new GenericResponse<>(false, ex.getMessage(), null);
        }
    }

    @Override
    public GenericResponse<TechnicianReportDraftResponse> deleteTechnicianReportDraft(
            String technicianReportDraftId,
            AuthenticatedUser technician) {

        if (technicianReportDraftId == null || technician == null) {
            return new GenericResponse<>(false, "Report data or technician cannot be null", null);
        }

        try {
            TechnicianReport technicianReport = technicianReportRepository.findByReportId(UUID.fromString(technicianReportDraftId))
                    .orElseThrow(() -> new InvalidTechnicianReportStateException("Technician report not found"));

            // Verify the report belongs to this technician
            if (!technicianReport.getTechnicianId().equals(technician.id())) {
                throw new InvalidTechnicianReportStateException("You are not authorized to delete this report");
            }

            // Verify report is in DRAFT state
            if (!"DRAFT".equals(technicianReport.getStatus())) {
                throw new InvalidTechnicianReportStateException("Only report drafts can be deleted");
            }

            // Create response before deleting
            TechnicianReportDraftResponse response = buildTechnicianReportDraftResponse(technicianReport);

            // Delete the report
            technicianReportRepository.delete(technicianReport);

            return new GenericResponse<>(true, "Technician report draft deleted successfully", response);
        } catch (IllegalArgumentException | DataAccessException | InvalidTechnicianReportStateException |
                 IllegalStateTransitionException ex) {
            return new GenericResponse<>(false, ex.getMessage(), null);
        }
    }

    @Override
    public GenericResponse<TechnicianReportDraftResponse> startWork(
            String technicianReportDraftId,
            AuthenticatedUser technician) {

        if (technicianReportDraftId == null || technician == null) {
            return new GenericResponse<>(false, "Report data or technician cannot be null", null);
        }

        try {
            TechnicianReport technicianReport = technicianReportRepository.findByReportId(UUID.fromString(technicianReportDraftId))
                    .orElseThrow(() -> new InvalidTechnicianReportStateException("Technician report not found"));

            // Verify the report belongs to this technician
            if (!technicianReport.getTechnicianId().equals(technician.id())) {
                throw new InvalidTechnicianReportStateException("You are not authorized to start work on this report");
            }

            // Verify report is in APPROVED state
            if (!"APPROVED".equals(technicianReport.getStatus())) {
                throw new InvalidTechnicianReportStateException("Only report Approved one can be started");
            }

            // Change the state to IN_PROGRESS
            technicianReport.startWork();
            // Save updated report
            TechnicianReport updatedReport = technicianReportRepository.save(technicianReport);
            // Create and return response
            TechnicianReportDraftResponse response = buildTechnicianReportDraftResponse(updatedReport);
            return new GenericResponse<>(true, "Technician report draft started successfully", response);
        } catch (IllegalArgumentException | DataAccessException | InvalidTechnicianReportStateException |
                 IllegalStateTransitionException ex) {
            return new GenericResponse<>(false, ex.getMessage(), null);
        }
    }

    @Override
    public GenericResponse<TechnicianReportDraftResponse> completeWork(
            String technicianReportDraftId,
            AuthenticatedUser technician) {

        if (technicianReportDraftId == null || technician == null) {
            return new GenericResponse<>(false, "Report data or technician cannot be null", null);
        }

        try {
            TechnicianReport technicianReport = technicianReportRepository.findByReportId(UUID.fromString(technicianReportDraftId))
                    .orElseThrow(() -> new InvalidTechnicianReportStateException("Technician report not found"));

            // Verify the report belongs to this technician
            if (!technicianReport.getTechnicianId().equals(technician.id())) {
                throw new InvalidTechnicianReportStateException("You are not authorized to complete work on this report");
            }

            // Verify report is in IN_PROGRESS state
            if (!"IN_PROGRESS".equals(technicianReport.getStatus())) {
                throw new InvalidTechnicianReportStateException("Only report in progress can be completed");
            }

            // Change the state
            technicianReport.complete();
            // Save updated report
            TechnicianReport updatedReport = technicianReportRepository.save(technicianReport);
            // Create and return response
            TechnicianReportDraftResponse response = buildTechnicianReportDraftResponse(updatedReport);
            return new GenericResponse<>(true, "Technician report draft completed successfully", response);
        } catch (IllegalArgumentException | DataAccessException | InvalidTechnicianReportStateException |
                 IllegalStateTransitionException ex) {
            return new GenericResponse<>(false, ex.getMessage(), null);
        }
    }


    @Override
    public GenericResponse<List<TechnicianReportDraftResponse>> getTechnicianReportSubmissions(String status, AuthenticatedUser customer) {
        if (customer == null) {
            return new GenericResponse<>(false, "Customer cannot be null", null);
        }

        try {
            List<TechnicianReport> reports = technicianReportRepository.findAllByStatus(status);
            if (reports.isEmpty()) {
                return new GenericResponse<>(false, "No technician report submissions found", null);
            }
            // Filter reports by customer ID
            reports = reports.stream()
                    .filter(report -> report.getUserRequest().getUserId().equals(customer.id()))
                    .filter(report -> !report.getStatus().equals("DRAFT"))
                    .toList();

            List<TechnicianReportDraftResponse> response = reports.stream()
                    .map(this::buildTechnicianReportDraftResponse)
                    .toList();
            return new GenericResponse<>(true, "Technician report submissions retrieved successfully", response);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Failed to retrieve technician report submissions", ex);
        }
    }

    @Override
    public GenericResponse<Void> acceptTechnicianReportSubmit(
            String technicianReportDraftId,
            AuthenticatedUser customer) {

        if (technicianReportDraftId == null || customer == null) {
            return new GenericResponse<>(false, "Report data or technician cannot be null", null);
        }

        try {
            // Find the technician report
            TechnicianReport technicianReport = technicianReportRepository.findByReportId(UUID.fromString(technicianReportDraftId))
                    .orElseThrow(() -> new InvalidTechnicianReportStateException("Technician report draft not found"));

            // Check if the customer owns the request
            UserRequest userRequest = technicianReport.getUserRequest();
            if (!userRequest.getUserId().equals(customer.id())) {
                throw new InvalidTechnicianReportStateException("You are not authorized to accept this report");
            }

            // Check if the report is in DRAFT state
            if (!"SUBMITTED".equals(technicianReport.getStatus())) {
                throw new InvalidTechnicianReportStateException("This report is not in draft state");
            }

            // Change the state to APPROVED
            technicianReport.approve();

            // Save the updated report
            technicianReportRepository.save(technicianReport);

            return new GenericResponse<>(true, "Technician report draft accepted successfully", null);
        } catch (IllegalArgumentException | DataAccessException | InvalidTechnicianReportStateException |
                IllegalStateTransitionException ex) {
            return new GenericResponse<>(false, ex.getMessage(), null);
        }
    }

    @Override
    public GenericResponse<Void> rejectTechnicianReportSubmit(
            String technicianReportDraftId,
            AuthenticatedUser customer) {

        if (technicianReportDraftId == null || customer == null) {
            return new GenericResponse<>(false, "Report data or technician cannot be null", null);
        }

        try {
            // Find the technician report
            TechnicianReport technicianReport = technicianReportRepository.findByReportId(UUID.fromString(technicianReportDraftId))
                    .orElseThrow(() -> new InvalidTechnicianReportStateException("Technician report draft not found"));

            // Check if the customer owns the request
            UserRequest userRequest = technicianReport.getUserRequest();
            if (!userRequest.getUserId().equals(customer.id())) {
                throw new InvalidTechnicianReportStateException("You are not authorized to reject this report");
            }

            // Check if the report is in DRAFT state
            if (!"SUBMITTED".equals(technicianReport.getStatus())) {
                throw new InvalidTechnicianReportStateException("This report is not in draft state");
            }

            // Change the state to REJECTED
            technicianReport.reject();

            // Save the updated report
            technicianReportRepository.save(technicianReport);

            return new GenericResponse<>(true, "Technician report draft rejected successfully", null);
        } catch (IllegalArgumentException | DataAccessException | InvalidTechnicianReportStateException |
                 IllegalStateTransitionException ex) {
            return new GenericResponse<>(false, ex.getMessage(), null);
        }
    }

    @Override
    public GenericResponse<TechnicianReportDraftResponse> submitTechnicianReportDraft(
            String technicianReportDraftId,
            AuthenticatedUser technician) {

        if (technicianReportDraftId == null || technician == null) {
            return new GenericResponse<>(false, "Report data or technician cannot be null", null);
        }

        try {
            TechnicianReport technicianReport = technicianReportRepository.findByReportId(UUID.fromString(technicianReportDraftId))
                    .orElseThrow(() -> new InvalidTechnicianReportStateException("Technician report not found"));

            // Verify the report belongs to this technician
            if (!technicianReport.getTechnicianId().equals(technician.id())) {
                throw new InvalidTechnicianReportStateException("You are not authorized to submit this report");
            }

            // Verify report is in DRAFT state
            if (!"DRAFT".equals(technicianReport.getStatus())) {
                throw new InvalidTechnicianReportStateException("Only report drafts can be submitted");
            }

            // Change the state to SUBMITTED
            technicianReport.submit();
            // Save updated report
            TechnicianReport updatedReport = technicianReportRepository.save(technicianReport);
            // Create and return response
            TechnicianReportDraftResponse response = buildTechnicianReportDraftResponse(updatedReport);
            return new GenericResponse<>(true, "Technician report draft submitted successfully", response);
        } catch (IllegalArgumentException | DataAccessException | InvalidTechnicianReportStateException |
                 IllegalStateTransitionException ex) {
            return new GenericResponse<>(false, ex.getMessage(), null);
        }
        }

    // Helper method to build response DTO
    private TechnicianReportDraftResponse buildTechnicianReportDraftResponse(TechnicianReport report) {
        return TechnicianReportDraftResponse.builder()
                .reportId(report.getReportId())
                .userRequestId(report.getUserRequest().getRequestId())
                .technicianId(report.getTechnicianId())
                .diagnosis(report.getDiagnosis())
                .actionPlan(report.getActionPlan())
                .estimatedCost(report.getEstimatedCost())
                .estimatedTimeSeconds(report.getEstimatedTimeSeconds())
                .status(report.getStatus())
                .build();
    }

    // Method to get draft reports owned by the technician
    public List<TechnicianReport> getDraftReportsForTechnician(AuthenticatedUser technician) {
        if (technician == null) {
            throw new InvalidTechnicianReportStateException("Technician cannot be null");
        }

        try {
            return technicianReportRepository.findAllByTechnicianIdAndStatus(technician.id(), "DRAFT");
        } catch (DataAccessException ex) {
            throw new DatabaseException("Failed to retrieve technician reports", ex);
        }
    }
}
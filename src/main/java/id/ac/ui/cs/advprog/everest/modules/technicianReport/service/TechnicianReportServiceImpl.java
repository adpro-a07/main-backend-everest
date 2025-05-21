package id.ac.ui.cs.advprog.everest.modules.technicianReport.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.RepairOrder;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.enums.RepairOrderStatus;
import id.ac.ui.cs.advprog.everest.modules.repairorder.repository.RepairOrderRepository;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.dto.CreateTechnicianReportDraftRequest;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.dto.TechnicianReportDraftResponse;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.exception.*;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.model.TechnicianReport;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.model.UserRequest;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.repository.TechnicianReportRepository;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.repository.UserRequestRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.upperCase;

@Service
public class TechnicianReportServiceImpl implements TechnicianReportService {

    private final TechnicianReportRepository technicianReportRepository;
    private final RepairOrderRepository repairOrderRepository;

    public TechnicianReportServiceImpl(
            TechnicianReportRepository technicianReportRepository,
            RepairOrderRepository repairOrderRepository) {
        this.technicianReportRepository = technicianReportRepository;
        this.repairOrderRepository = repairOrderRepository;
    }

    @Override
    public GenericResponse<TechnicianReportDraftResponse> createTechnicianReportDraft(
            CreateTechnicianReportDraftRequest createTechnicianReportDraft,
            AuthenticatedUser technician) {

        try {
            if (createTechnicianReportDraft == null || technician == null)
                throw new InvalidDataTechnicianReport("Report data or technician cannot be null");

            String repairOrderId = createTechnicianReportDraft.getRepairOrderId();
            if (repairOrderId == null || repairOrderId.isEmpty())
                throw new InvalidDataTechnicianReport("Report data or technician cannot be null or empty");

            RepairOrder repairOrder = repairOrderRepository.findById(UUID.fromString(repairOrderId))
                    .orElseThrow(() -> new InvalidTechnicianReportStateException("Repair order not found"));

            if (!repairOrder.getTechnicianId().toString().equals(technician.id().toString())){
                throw new IllegalAccessTechnicianReport("Technician is not authorized to this repair order");
            }

            if (repairOrder.getStatus() != RepairOrderStatus.PENDING_CONFIRMATION) {
                throw new InvalidTechnicianReportStateException("Repair order is not in progress");
            }

            if (createTechnicianReportDraft.getEstimatedTimeSeconds() == null || createTechnicianReportDraft.getEstimatedTimeSeconds() < 0) {
                throw new InvalidDataTechnicianReport("Estimated time cannot be less than 0");
            }

            if (createTechnicianReportDraft.getEstimatedCost() == null || createTechnicianReportDraft.getEstimatedCost().compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidDataTechnicianReport("Estimated cost cannot be less than 0");
            }

            if (createTechnicianReportDraft.getDiagnosis() == null || createTechnicianReportDraft.getDiagnosis().isEmpty()) {
                throw new InvalidDataTechnicianReport("Diagnosis cannot be null or empty");
            }

            if (createTechnicianReportDraft.getActionPlan() == null || createTechnicianReportDraft.getActionPlan().isEmpty()) {
                throw new InvalidDataTechnicianReport("Action plan cannot be null or empty");
            }

            // TODO: Please refactor this code to use a more appropriate method for checking if a report already exists
            boolean hasNonRejectedReport = technicianReportRepository
                    .findAllByRepairOrderId(UUID.fromString(repairOrderId))
                    .stream()
                    .anyMatch(report -> !"REJECTED".equals(report.getStatus()));
            if (hasNonRejectedReport) {
                throw new DatabaseException("Report already exists");
            }

            TechnicianReport technicianReport = TechnicianReport.builder()
                    .reportId(UUID.randomUUID())
                    .repairOrder(repairOrder)
                    .technicianId(technician.id())
                    .diagnosis(createTechnicianReportDraft.getDiagnosis())
                    .actionPlan(createTechnicianReportDraft.getActionPlan())
                    .estimatedCost(createTechnicianReportDraft.getEstimatedCost())
                    .estimatedTimeSeconds(createTechnicianReportDraft.getEstimatedTimeSeconds())
                    .build();

            TechnicianReport savedReport = technicianReportRepository.save(technicianReport);

            TechnicianReportDraftResponse response = buildTechnicianReportDraftResponse(savedReport);

            return new GenericResponse<>(true, "Technician report draft created successfully", response);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    @Override
    public GenericResponse<TechnicianReportDraftResponse> updateTechnicianReportDraft(
            String technicianReportDraftId,
            CreateTechnicianReportDraftRequest createTechnicianReportDraft,
            AuthenticatedUser technician) {

        try {
            if (technicianReportDraftId == null || createTechnicianReportDraft == null || technician == null)
                throw new InvalidDataTechnicianReport("Report data or technician cannot be null");

            TechnicianReport technicianReport = technicianReportRepository.findByReportId(UUID.fromString(technicianReportDraftId))
                    .orElseThrow(() -> new InvalidTechnicianReportStateException("Technician report not found"));

            if (!technicianReport.getTechnicianId().equals(technician.id())) {
                throw new IllegalAccessTechnicianReport("You are not authorized to update this report");
            }

            if (!"DRAFT".equals(technicianReport.getStatus())) {
                throw new InvalidTechnicianReportStateException("Only report drafts can be updated");
            }

            Long estimatedTimeSeconds = createTechnicianReportDraft.getEstimatedTimeSeconds();
            if (estimatedTimeSeconds == null || estimatedTimeSeconds < 0) {
                throw new InvalidDataTechnicianReport("Estimated time cannot be less than 0");
            }

            BigDecimal estimatedCost = createTechnicianReportDraft.getEstimatedCost();
            if (estimatedCost == null || estimatedCost.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidDataTechnicianReport("Estimated cost cannot be less than 0");
            }

            if (createTechnicianReportDraft.getDiagnosis() == null || createTechnicianReportDraft.getDiagnosis().isEmpty()) {
                throw new InvalidDataTechnicianReport("Diagnosis cannot be null or empty");
            }

            if (createTechnicianReportDraft.getActionPlan() == null || createTechnicianReportDraft.getActionPlan().isEmpty()) {
                throw new InvalidDataTechnicianReport("Action plan cannot be null or empty");
            }

            technicianReport.setDiagnosis(createTechnicianReportDraft.getDiagnosis());
            technicianReport.setActionPlan(createTechnicianReportDraft.getActionPlan());
            technicianReport.setEstimatedCost(createTechnicianReportDraft.getEstimatedCost());
            technicianReport.setEstimatedTimeSeconds(createTechnicianReportDraft.getEstimatedTimeSeconds());

            TechnicianReport updatedReport = technicianReportRepository.save(technicianReport);

            TechnicianReportDraftResponse response = buildTechnicianReportDraftResponse(updatedReport);

            return new GenericResponse<>(true, "Technician report draft updated successfully", response);
        } catch (Exception ex) {
            return handleException(ex);
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

            if (!technicianReport.getTechnicianId().equals(technician.id())) {
                throw new IllegalAccessTechnicianReport("You are not authorized to delete this report");
            }

            if (!"DRAFT".equals(technicianReport.getStatus())) {
                throw new InvalidTechnicianReportStateException("Only report drafts can be deleted");
            }

            TechnicianReportDraftResponse response = buildTechnicianReportDraftResponse(technicianReport);

            technicianReportRepository.delete(technicianReport);

            return new GenericResponse<>(true, "Technician report draft deleted successfully", response);
        } catch (Exception ex) {
            return handleException(ex);
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

            if (!technicianReport.getTechnicianId().equals(technician.id())) {
                throw new IllegalAccessTechnicianReport("You are not authorized to submit this report");
            }

            if (!"DRAFT".equals(technicianReport.getStatus())) {
                throw new InvalidTechnicianReportStateException("Only report drafts can be submitted");
            }

            technicianReport.submit();
            TechnicianReport updatedReport = technicianReportRepository.save(technicianReport);
            TechnicianReportDraftResponse response = buildTechnicianReportDraftResponse(updatedReport);
            return new GenericResponse<>(true, "Technician report draft submitted successfully", response);
        } catch (Exception ex) {
            return handleException(ex);
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
            TechnicianReport technicianReport = technicianReportRepository.findByReportId(UUID.fromString(technicianReportDraftId))
                    .orElseThrow(() -> new InvalidTechnicianReportStateException("Technician report draft not found"));

            RepairOrder repairOrder = technicianReport.getRepairOrder();
            if (!repairOrder.getCustomerId().equals(customer.id())) {
                throw new InvalidTechnicianReportStateException("You are not authorized to accept this report");
            }

            if (!"SUBMITTED".equals(technicianReport.getStatus())) {
                throw new InvalidTechnicianReportStateException("This report is not in submitted state");
            }

            technicianReport.approve();
            technicianReportRepository.save(technicianReport);

            return new GenericResponse<>(true, "Technician report draft accepted successfully", null);
        } catch (Exception ex) {
            return handleException(ex);
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
            TechnicianReport technicianReport = technicianReportRepository.findByReportId(UUID.fromString(technicianReportDraftId))
                    .orElseThrow(() -> new InvalidTechnicianReportStateException("Technician report draft not found"));

            RepairOrder repairOrder = technicianReport.getRepairOrder();
            if (!repairOrder.getCustomerId().equals(customer.id())) {
                throw new IllegalAccessTechnicianReport("You are not authorized to reject this report");
            }

            if (!"SUBMITTED".equals(technicianReport.getStatus())) {
                throw new InvalidTechnicianReportStateException("This report is not in submitted state");
            }

            technicianReport.reject();
            technicianReportRepository.save(technicianReport);

            return new GenericResponse<>(true, "Technician report draft rejected successfully", null);
        } catch (Exception ex) {
            return handleException(ex);
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

            if (!technicianReport.getTechnicianId().equals(technician.id())) {
                throw new InvalidTechnicianReportStateException("You are not authorized to start work on this report");
            }

            if (!"APPROVED".equals(technicianReport.getStatus())) {
                throw new InvalidTechnicianReportStateException("Only approved reports can be started");
            }

            technicianReport.getRepairOrder().setStatus(RepairOrderStatus.IN_PROGRESS);

            technicianReport.startWork();
            TechnicianReport updatedReport = technicianReportRepository.save(technicianReport);
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

            if (!technicianReport.getTechnicianId().equals(technician.id())) {
                throw new InvalidTechnicianReportStateException("You are not authorized to complete work on this report");
            }

            if (!"IN_PROGRESS".equals(technicianReport.getStatus())) {
                throw new InvalidTechnicianReportStateException("Only reports in progress can be completed");
            }

            // TODO: Uncomment this line if you want to update the repair order status to COMPLETED
            // technicianReport.getRepairOrder().setStatus(RepairOrderStatus.COMPLETED);

            technicianReport.complete();
            TechnicianReport updatedReport = technicianReportRepository.save(technicianReport);
            TechnicianReportDraftResponse response = buildTechnicianReportDraftResponse(updatedReport);
            return new GenericResponse<>(true, "Technician report draft completed successfully", response);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    @Override
    public GenericResponse<List<TechnicianReportDraftResponse>> getTechnicianReportByStatusForTechnician(String status, AuthenticatedUser technician) {
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
            return handleException(ex);
        }
    }

    @Override
    public GenericResponse<List<TechnicianReportDraftResponse>> getTechnicianReportByStatusForCustomer(String status, AuthenticatedUser customer) {
        if (customer == null) {
            return new GenericResponse<>(false, "Customer cannot be null", null);
        }

        try {
            if ("DRAFT".equals(status)) {
                throw new InvalidTechnicianReportStateException("Only report above Draft can be seen by Customer");
            }

            List<TechnicianReport> reports = technicianReportRepository.findAllByStatus(status);
            if (reports.isEmpty()) {
                return new GenericResponse<>(false, "No technician report submissions found", null);
            }
            reports = reports.stream()
                    .filter(report -> report.getRepairOrder().getCustomerId().equals(customer.id()))
                    .filter(report -> !report.getStatus().equals("DRAFT"))
                    .toList();

            List<TechnicianReportDraftResponse> response = reports.stream()
                    .map(this::buildTechnicianReportDraftResponse)
                    .toList();
            return new GenericResponse<>(true, "Technician report submissions retrieved successfully", response);
        } catch (DataAccessException ex) {
            return handleException(ex);
        }
    }

    private TechnicianReportDraftResponse buildTechnicianReportDraftResponse(TechnicianReport report) {
        return TechnicianReportDraftResponse.builder()
                .reportId(report.getReportId())
                .repairOrderId(report.getRepairOrder().getId())
                .technicianId(report.getTechnicianId())
                .diagnosis(report.getDiagnosis())
                .actionPlan(report.getActionPlan())
                .estimatedCost(report.getEstimatedCost())
                .estimatedTimeSeconds(report.getEstimatedTimeSeconds())
                .status(report.getStatus())
                .build();
    }

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

    private <T> GenericResponse<T> handleException(Exception ex) {
        return new GenericResponse<>(false, ex.getMessage(), null);
    }
}
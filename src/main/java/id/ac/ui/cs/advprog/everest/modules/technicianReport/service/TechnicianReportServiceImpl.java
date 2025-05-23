package id.ac.ui.cs.advprog.everest.modules.technicianReport.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.messaging.RepairEventPublisher;
import id.ac.ui.cs.advprog.everest.messaging.events.RepairOrderCompletedEvent;
import id.ac.ui.cs.advprog.everest.modules.repairorder.dto.ViewRepairOrderResponse;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.RepairOrder;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.enums.RepairOrderStatus;
import id.ac.ui.cs.advprog.everest.modules.repairorder.repository.RepairOrderRepository;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.dto.CreateTechnicianReportDraftRequest;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.dto.TechnicianReportDraftResponse;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.exception.*;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.model.TechnicianReport;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.repository.TechnicianReportRepository;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.upperCase;

@Service
public class TechnicianReportServiceImpl implements TechnicianReportService {

    private final TechnicianReportRepository technicianReportRepository;
    private final RepairOrderRepository repairOrderRepository;
    private final RepairEventPublisher repairEventPublisher;
    private final TechnicianReportAuditLogger auditLogger;

    public TechnicianReportServiceImpl(
            TechnicianReportRepository technicianReportRepository,
            RepairOrderRepository repairOrderRepository,
            RepairEventPublisher repairEventPublisher,
            TechnicianReportAuditLogger auditLogger
    ) {
        this.technicianReportRepository = technicianReportRepository;
        this.repairOrderRepository = repairOrderRepository;
        this.repairEventPublisher = repairEventPublisher;
        this.auditLogger = auditLogger;
    }

    @Override
    public GenericResponse<TechnicianReportDraftResponse> createTechnicianReportDraft(
            CreateTechnicianReportDraftRequest createTechnicianReportDraft,
            AuthenticatedUser technician) {

        try {
            createTechnicianReportDraft.validate();

            String repairOrderId = createTechnicianReportDraft.getRepairOrderId();
            if (repairOrderId == null || repairOrderId.isEmpty())
                throw new InvalidDataTechnicianReport("Report data or technician cannot be null or empty");

            RepairOrder repairOrder = repairOrderRepository.findById(UUID.fromString(repairOrderId))
                    .orElseThrow(() -> new InvalidTechnicianReportStateException("Repair order not found"));

            if (!repairOrder.getTechnicianId().toString().equals(technician.id().toString())){
                throw new IllegalAccessTechnicianReport("Technician", "create a report based on this repair order");
            }

            if (repairOrder.getStatus() != RepairOrderStatus.PENDING_CONFIRMATION) {
                throw new InvalidTechnicianReportStateException("Repair order is not in progress");
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
            auditLogger.logReportAction("CREATE_DRAFT", savedReport.getReportId().toString(), technician.id().toString());
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
            createTechnicianReportDraft.validate();
            if (technicianReportDraftId == null)
                throw new InvalidDataTechnicianReport("Report data cannot be null");

            TechnicianReport technicianReport = technicianReportRepository.findByReportId(UUID.fromString(technicianReportDraftId))
                    .orElseThrow(() -> new InvalidTechnicianReportStateException("Technician report not found"));

            if (!technicianReport.getTechnicianId().equals(technician.id())) {
                throw new IllegalAccessTechnicianReport("Technician","update this report");
            }

            if (!"DRAFT".equals(technicianReport.getStatus())) {
                throw new InvalidTechnicianReportStateException("Only report drafts can be updated");
            }

            technicianReport.setDiagnosis(createTechnicianReportDraft.getDiagnosis());
            technicianReport.setActionPlan(createTechnicianReportDraft.getActionPlan());
            technicianReport.setEstimatedCost(createTechnicianReportDraft.getEstimatedCost());
            technicianReport.setEstimatedTimeSeconds(createTechnicianReportDraft.getEstimatedTimeSeconds());

            TechnicianReport updatedReport = technicianReportRepository.save(technicianReport);

            TechnicianReportDraftResponse response = buildTechnicianReportDraftResponse(updatedReport);
            auditLogger.logReportAction("UPDATE_DRAFT", updatedReport.getReportId().toString(), technician.id().toString());
            return new GenericResponse<>(true, "Technician report draft updated successfully", response);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    @Override
    public GenericResponse<TechnicianReportDraftResponse> deleteTechnicianReportDraft(
            String technicianReportDraftId,
            AuthenticatedUser technician) {

        try {
            if (technicianReportDraftId == null)
                throw new InvalidDataTechnicianReport("Report data cannot be null");

            TechnicianReport technicianReport = technicianReportRepository.findByReportId(UUID.fromString(technicianReportDraftId))
                    .orElseThrow(() -> new InvalidTechnicianReportStateException("Technician report not found"));

            if (!technicianReport.getTechnicianId().equals(technician.id())) {
                throw new IllegalAccessTechnicianReport("Technician" ,"delete this report");
            }

            if (!"DRAFT".equals(technicianReport.getStatus())) {
                throw new InvalidTechnicianReportStateException("Only report drafts can be deleted");
            }

            TechnicianReportDraftResponse response = buildTechnicianReportDraftResponse(technicianReport);

            technicianReportRepository.delete(technicianReport);
            auditLogger.logReportAction("DELETE_DRAFT", technicianReport.getReportId().toString(), technician.id().toString());
            return new GenericResponse<>(true, "Technician report draft deleted successfully", response);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    @Override
    public GenericResponse<TechnicianReportDraftResponse> submitTechnicianReportDraft(
            String technicianReportDraftId,
            AuthenticatedUser technician) {

        try {
            if (technicianReportDraftId == null)
                throw new InvalidDataTechnicianReport("Report data cannot be null");

            TechnicianReport technicianReport = technicianReportRepository.findByReportId(UUID.fromString(technicianReportDraftId))
                    .orElseThrow(() -> new InvalidTechnicianReportStateException("Technician report not found"));

            if (!technicianReport.getTechnicianId().toString().equals(technician.id().toString())) {
                throw new IllegalAccessTechnicianReport("Technician" ,"submit this report");
            }

            if (!"DRAFT".equals(technicianReport.getStatus())) {
                throw new InvalidTechnicianReportStateException("Only report drafts can be submitted");
            }

            technicianReport.submit();
            TechnicianReport updatedReport = technicianReportRepository.save(technicianReport);
            TechnicianReportDraftResponse response = buildTechnicianReportDraftResponse(updatedReport);
            auditLogger.logReportAction("SUBMIT_DRAFT", updatedReport.getReportId().toString(), technician.id().toString());
            return new GenericResponse<>(true, "Technician report draft submitted successfully", response);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    @Override
    public GenericResponse<Void> acceptTechnicianReportSubmit(
            String technicianReportDraftId,
            AuthenticatedUser customer) {

        try {
            if (technicianReportDraftId == null)
                throw new InvalidDataTechnicianReport("Report data cannot be null");

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

            auditLogger.logReportAction("ACCEPT_SUBMIT", technicianReport.getReportId().toString(), customer.id().toString());
            return new GenericResponse<>(true, "Technician report draft accepted successfully", null);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    @Override
    public GenericResponse<Void> rejectTechnicianReportSubmit(
            String technicianReportDraftId,
            AuthenticatedUser customer) {

        try {
            if (technicianReportDraftId == null)
                throw new InvalidDataTechnicianReport("Report data cannot be null");

            TechnicianReport technicianReport = technicianReportRepository.findByReportId(UUID.fromString(technicianReportDraftId))
                    .orElseThrow(() -> new InvalidTechnicianReportStateException("Technician report draft not found"));

            RepairOrder repairOrder = technicianReport.getRepairOrder();
            if (!repairOrder.getCustomerId().equals(customer.id())) {
                throw new IllegalAccessTechnicianReport("Customer", "reject this report");
            }

            if (!"SUBMITTED".equals(technicianReport.getStatus())) {
                throw new InvalidTechnicianReportStateException("This report is not in submitted state");
            }

            technicianReport.reject();
            technicianReportRepository.save(technicianReport);

            auditLogger.logReportAction("REJECT_SUBMIT", technicianReport.getReportId().toString(), customer.id().toString());
            return new GenericResponse<>(true, "Technician report draft rejected successfully", null);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    @Override
    public GenericResponse<TechnicianReportDraftResponse> startWork(
            String technicianReportDraftId,
            AuthenticatedUser technician) {

        try {
            if (technicianReportDraftId == null)
                throw new InvalidDataTechnicianReport("Report data cannot be null");

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
            auditLogger.logReportAction("START_WORK", updatedReport.getReportId().toString(), technician.id().toString());
            return new GenericResponse<>(true, "Technician report draft started successfully", response);
        } catch (Exception ex) {
           return handleException(ex);
        }
    }

    @Override
    public GenericResponse<TechnicianReportDraftResponse> completeWork(
            String technicianReportDraftId,
            AuthenticatedUser technician) {

        try {
            if (technicianReportDraftId == null)
                throw new InvalidDataTechnicianReport("Report data cannot be null");

            TechnicianReport technicianReport = technicianReportRepository.findByReportId(UUID.fromString(technicianReportDraftId))
                    .orElseThrow(() -> new InvalidTechnicianReportStateException("Technician report not found"));

            if (!technicianReport.getTechnicianId().equals(technician.id())) {
                throw new InvalidTechnicianReportStateException("You are not authorized to complete work on this report");
            }

            if (!"IN_PROGRESS".equals(technicianReport.getStatus())) {
                throw new InvalidTechnicianReportStateException("Only reports in progress can be completed");
            }

            technicianReport.complete();
            TechnicianReport updatedReport = technicianReportRepository.save(technicianReport);
            TechnicianReportDraftResponse response = buildTechnicianReportDraftResponse(updatedReport);

            RepairOrderCompletedEvent repairOrderCompletedEvent = RepairOrderCompletedEvent.builder()
                    .repairOrderId(technicianReport.getReportId())
                    .technicianId(technicianReport.getTechnicianId())
                    .amount(technicianReport.getEstimatedCost())
                    .completedAt(Instant.now())
                    .build();

            technicianReport.getRepairOrder().setStatus(RepairOrderStatus.COMPLETED);
            repairEventPublisher.publishRepairCompleted(repairOrderCompletedEvent);

            auditLogger.logReportAction("COMPLETE_WORK", updatedReport.getReportId().toString(), technician.id().toString());
            return new GenericResponse<>(true, "Technician report draft completed successfully", response);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    @Override
    public GenericResponse<List<TechnicianReportDraftResponse>> getTechnicianReportByStatusForTechnician(String status, AuthenticatedUser technician) {
        try {
            List<TechnicianReport> reports = technicianReportRepository.findAllByTechnicianIdAndStatus(technician.id(), upperCase(status));
            List<TechnicianReportDraftResponse> response = reports.stream()
                    .map(this::buildTechnicianReportDraftResponse)
                    .toList();

            reports.forEach(report -> auditLogger.logReportAction("GET_BY_STATUS_TECHNICIAN", report.getReportId().toString(), technician.id().toString()));
            return new GenericResponse<>(true, "Technician reports retrieved successfully", response);
        } catch (DataAccessException ex) {
            return handleException(ex);
        }
    }

    @Override
    public GenericResponse<List<TechnicianReportDraftResponse>> getTechnicianReportByStatusForCustomer(String status, AuthenticatedUser customer) {

        try {
            if (status.equals("DRAFT")) {
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
            reports.forEach(report -> auditLogger.logReportAction("GET_BY_STATUS_CUSTOMER", report.getReportId().toString(), customer.id().toString()));
            return new GenericResponse<>(true, "Technician report submissions retrieved successfully", response);
        } catch (DataAccessException ex) {
            return handleException(ex);
        }
    }

    @Override
    public GenericResponse<TechnicianReportDraftResponse> getTechnicianReportById(String technicianReportId, AuthenticatedUser user) {
        try {
            if (technicianReportId == null)
                throw new InvalidDataTechnicianReport("Report data cannot be null");

            TechnicianReport technicianReport = technicianReportRepository.findByReportId(UUID.fromString(technicianReportId))
                    .orElseThrow(() -> new InvalidTechnicianReportStateException("Technician report not found"));

            if (user.role() == UserRole.CUSTOMER) technicianReport.getState().readPermissions(technicianReport);
            else if (user.role() == UserRole.TECHNICIAN) {
                if (!technicianReport.getTechnicianId().equals(user.id())) {
                    throw new InvalidTechnicianReportStateException("You are not authorized to view this report");
                }
            }

            TechnicianReportDraftResponse response = buildTechnicianReportDraftResponse(technicianReport);
            auditLogger.logReportAction("GET_BY_ID", technicianReport.getReportId().toString(), user.id().toString());
            return new GenericResponse<>(true, "Technician report retrieved successfully", response);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    @Override
    public GenericResponse<List<ViewRepairOrderResponse>> getRepairOrderByTechnicianId(AuthenticatedUser user) {
        try {
            List<ViewRepairOrderResponse> repairOrders = repairOrderRepository.findByTechnicianId(user.id()).stream()
                    .filter(repairOrder -> repairOrder.getStatus() == RepairOrderStatus.PENDING_CONFIRMATION)
                    .map(ro -> ViewRepairOrderResponse.builder()
                            .id(ro.getId())
                            .customerId(ro.getCustomerId())
                            .technicianId(ro.getTechnicianId())
                            .status(ro.getStatus())
                            .itemName(ro.getItemName())
                            .itemCondition(ro.getItemCondition())
                            .issueDescription(ro.getIssueDescription())
                            .desiredServiceDate(ro.getDesiredServiceDate())
                            .createdAt(ro.getCreatedAt())
                            .updatedAt(ro.getUpdatedAt())
                            .build())
                    .collect(Collectors.toList());

            return new GenericResponse<>(true, "Repair orders retrieved successfully", repairOrders);
        } catch (Exception ex) {
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

    private <T> GenericResponse<T> handleException(Exception ex) {
        return new GenericResponse<>(false, ex.getMessage(), null);
    }
}
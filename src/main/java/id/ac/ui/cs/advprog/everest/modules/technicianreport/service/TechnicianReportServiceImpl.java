package id.ac.ui.cs.advprog.everest.modules.technicianreport.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.messaging.RepairEventPublisher;
import id.ac.ui.cs.advprog.everest.messaging.events.RepairOrderCompletedEvent;
import id.ac.ui.cs.advprog.everest.modules.repairorder.dto.ViewRepairOrderResponse;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.RepairOrder;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.enums.RepairOrderStatus;
import id.ac.ui.cs.advprog.everest.modules.repairorder.repository.RepairOrderRepository;
import id.ac.ui.cs.advprog.everest.modules.technicianreport.dto.CreateTechnicianReportDraftRequest;
import id.ac.ui.cs.advprog.everest.modules.technicianreport.dto.TechnicianReportDraftResponse;
import id.ac.ui.cs.advprog.everest.modules.technicianreport.exception.*;
import id.ac.ui.cs.advprog.everest.modules.technicianreport.model.TechnicianReport;
import id.ac.ui.cs.advprog.everest.modules.technicianreport.repository.TechnicianReportRepository;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static id.ac.ui.cs.advprog.everest.modules.technicianreport.constants.ReportConstants.*;
import static org.apache.commons.lang3.StringUtils.upperCase;

@Service
public class TechnicianReportServiceImpl implements TechnicianReportService {

    private final TechnicianReportRepository technicianReportRepository;
    private final RepairOrderRepository repairOrderRepository;
    private final RepairEventPublisher repairEventPublisher;
    private final TechnicianReportAuditLogger auditLogger;

    private static final String TECHNICIAN_REPORT_NOT_FOUND = "Technician report not found";
    private static final String TECHNICIAN_REPORT_DATA_NULL = "Report data cannot be null";
    private static final String REPAIR_ORDER_NOT_FOUND = "Repair order not found";
    private static final String REPAIR_ORDER_ALREADY_EXISTS = "Repair order already exists";
    private static final String UNAUTHORIZED_REPORT_ACCESS = "You are not authorized to access this report";
    private static final String UNAUTHORIZED_REPORT_UPDATE = "You are not authorized to update this report";
    private static final String REPORT_NOT_SUBMITTED = "This report is not in submitted state";
    private static final String ONLY_APPROVED_REPORTS_CAN_START = "Only approved reports can be started";
    private static final String ONLY_IN_PROGRESS_REPORTS_CAN_COMPLETE = "Only reports in progress can be completed";
    private static final String ONLY_DRAFTS_CAN_BE_UPDATED = "Only report drafts can be updated";
    private static final String REPAIR_ORDER_NOT_IN_PROGRESS = "Repair order is not in progress";
    private static final String CUSTOMER_CANNOT_VIEW_DRAFTS = "Only report above Draft can be seen by Customer";

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
            CreateTechnicianReportDraftRequest request,
            AuthenticatedUser technician) {

        try {
            request.validate();

            RepairOrder repairOrder = findRepairOrderById(request.getRepairOrderId());
            validateRepairOrderForReportCreation(repairOrder, technician);
            validateNoExistingNonRejectedReport(request.getRepairOrderId());

            TechnicianReport technicianReport = createNewTechnicianReport(request, repairOrder, technician);
            TechnicianReport savedReport = technicianReportRepository.save(technicianReport);

            TechnicianReportDraftResponse response = buildTechnicianReportDraftResponse(savedReport);
            logAuditAction("CREATE_DRAFT", savedReport.getReportId().toString(), technician.id().toString());

            return createSuccessResponse("Technician report draft created successfully", response);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    @Override
    public GenericResponse<TechnicianReportDraftResponse> updateTechnicianReportDraft(
            String technicianReportDraftId,
            CreateTechnicianReportDraftRequest request,
            AuthenticatedUser technician) {

        try {
            request.validate();
            TechnicianReport technicianReport = findTechnicianReportById(technicianReportDraftId);
            validateTechnicianCanUpdateReport(technicianReport, technician);

            updateTechnicianReportFields(technicianReport, request);
            TechnicianReport updatedReport = technicianReportRepository.save(technicianReport);

            TechnicianReportDraftResponse response = buildTechnicianReportDraftResponse(updatedReport);
            logAuditAction("UPDATE_DRAFT", updatedReport.getReportId().toString(), technician.id().toString());

            return createSuccessResponse("Technician report draft updated successfully", response);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    @Override
    public GenericResponse<TechnicianReportDraftResponse> deleteTechnicianReportDraft(
            String technicianReportDraftId,
            AuthenticatedUser technician) {

        try {
            TechnicianReport technicianReport = findTechnicianReportById(technicianReportDraftId);
            validateTechnicianCanUpdateReport(technicianReport, technician);

            TechnicianReportDraftResponse response = buildTechnicianReportDraftResponse(technicianReport);
            technicianReportRepository.delete(technicianReport);

            logAuditAction("DELETE_DRAFT", technicianReport.getReportId().toString(), technician.id().toString());
            return createSuccessResponse("Technician report draft deleted successfully", response);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    @Override
    public GenericResponse<TechnicianReportDraftResponse> submitTechnicianReportDraft(
            String technicianReportDraftId,
            AuthenticatedUser technician) {

        try {
            TechnicianReport technicianReport = findTechnicianReportById(technicianReportDraftId);
            validateTechnicianCanUpdateReport(technicianReport, technician);

            technicianReport.submit();
            TechnicianReport updatedReport = technicianReportRepository.save(technicianReport);

            TechnicianReportDraftResponse response = buildTechnicianReportDraftResponse(updatedReport);

            logAuditAction("SUBMIT_DRAFT", updatedReport.getReportId().toString(), technician.id().toString());

            return createSuccessResponse("Technician report draft submitted successfully", response);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    @Override
    public GenericResponse<Void> acceptTechnicianReportSubmit(
            String technicianReportDraftId,
            AuthenticatedUser customer) {

        try {
            TechnicianReport technicianReport = findTechnicianReportById(technicianReportDraftId);
            validateCustomerOwnsReportAndCanAct(technicianReport, customer, REPORT_NOT_SUBMITTED);

            technicianReport.approve();
            technicianReportRepository.save(technicianReport);

            logAuditAction("ACCEPT_SUBMIT", technicianReport.getReportId().toString(), customer.id().toString());
            return createSuccessResponse("Technician report draft accepted successfully", null);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    @Override
    public GenericResponse<Void> rejectTechnicianReportSubmit(
            String technicianReportDraftId,
            AuthenticatedUser customer) {

        try {
            TechnicianReport technicianReport = findTechnicianReportById(technicianReportDraftId);
            validateCustomerOwnsReportAndCanAct(technicianReport, customer, REPORT_NOT_SUBMITTED);

            technicianReport.reject();
            technicianReportRepository.save(technicianReport);

            logAuditAction("REJECT_SUBMIT", technicianReport.getReportId().toString(), customer.id().toString());
            return createSuccessResponse("Technician report draft rejected successfully", null);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    @Override
    public GenericResponse<TechnicianReportDraftResponse> startWork(
            String technicianReportDraftId,
            AuthenticatedUser technician) {

        try {
            TechnicianReport technicianReport = findTechnicianReportById(technicianReportDraftId);
            validateTechnicianCanStartWork(technicianReport, technician);

            technicianReport.getRepairOrder().setStatus(RepairOrderStatus.IN_PROGRESS);
            technicianReport.startWork();

            TechnicianReport updatedReport = technicianReportRepository.save(technicianReport);
            TechnicianReportDraftResponse response = buildTechnicianReportDraftResponse(updatedReport);

            logAuditAction("START_WORK", updatedReport.getReportId().toString(), technician.id().toString());
            return createSuccessResponse("Technician report draft started successfully", response);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    @Override
    public GenericResponse<TechnicianReportDraftResponse> completeWork(
            String technicianReportDraftId,
            AuthenticatedUser technician) {

        try {
            TechnicianReport technicianReport = findTechnicianReportById(technicianReportDraftId);
            validateTechnicianCanCompleteWork(technicianReport, technician);

            technicianReport.complete();
            updateRepairOrderStatusToCompleted(technicianReport);
            technicianReport.getRepairOrder().setStatus(RepairOrderStatus.COMPLETED);
            TechnicianReport updatedReport = technicianReportRepository.save(technicianReport);

            publishRepairCompletedEvent(updatedReport);

            TechnicianReportDraftResponse response = buildTechnicianReportDraftResponse(updatedReport);
            logAuditAction("COMPLETE_WORK", updatedReport.getReportId().toString(), technician.id().toString());

            return createSuccessResponse("Technician report draft completed successfully", response);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    @Override
    public GenericResponse<List<TechnicianReportDraftResponse>> getTechnicianReportByStatusForTechnician(
            String status,
            AuthenticatedUser technician) {

        try {
            List<TechnicianReport> reports = technicianReportRepository
                    .findAllByTechnicianIdAndStatus(technician.id(), upperCase(status));

            List<TechnicianReportDraftResponse> response = reports.stream()
                    .map(this::buildTechnicianReportDraftResponse)
                    .toList();

            logReportsAuditAction(reports, "GET_BY_STATUS_TECHNICIAN", technician.id().toString());
            return createSuccessResponse("Technician reports retrieved successfully", response);
        } catch (DataAccessException ex) {
            return handleException(ex);
        }
    }

    @Override
    public GenericResponse<List<TechnicianReportDraftResponse>> getTechnicianReportByStatusForCustomer(
            String status,
            AuthenticatedUser customer) {

        try {
            validateCustomerCanViewStatus(status);

            List<TechnicianReport> reports = getCustomerVisibleReports(upperCase(status), customer);
            if (reports.isEmpty()) {
                return new GenericResponse<>(false, "No technician report submissions found", null);
            }
            List<TechnicianReportDraftResponse> response = reports.stream()
                    .map(report -> {
                        Long estimatedCost = report.getEstimatedCost();
                        int discount = report.getRepairOrder().getCoupon().getDiscountAmount();
                        Long finalCost = (estimatedCost != null) ? Long.valueOf(estimatedCost - discount) : estimatedCost;

                        TechnicianReportDraftResponse resp = buildTechnicianReportDraftResponse(report);
                        resp.setEstimatedCost(finalCost);
                        return resp;
                    })
                    .toList();

            logReportsAuditAction(reports, "GET_BY_STATUS_CUSTOMER", customer.id().toString());

            return createSuccessResponse("Technician report submissions retrieved successfully", response);
        } catch (DataAccessException ex) {
            return handleException(ex);
        }
    }

    @Override
    public GenericResponse<TechnicianReportDraftResponse> getTechnicianReportById(
            String technicianReportId,
            AuthenticatedUser user) {

        try {
            TechnicianReport technicianReport = findTechnicianReportById(technicianReportId);
            validateUserCanViewReport(technicianReport, user);

            TechnicianReportDraftResponse response = buildTechnicianReportDraftResponse(technicianReport);
            logAuditAction("GET_BY_ID", technicianReport.getReportId().toString(), user.id().toString());

            return createSuccessResponse("Technician report retrieved successfully", response);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    @Override
    public GenericResponse<List<ViewRepairOrderResponse>> getRepairOrderByTechnicianId(AuthenticatedUser user) {
        try {
            List<ViewRepairOrderResponse> repairOrders = repairOrderRepository.findByTechnicianId(user.id())
                    .stream()
                    .filter(this::isPendingConfirmation)
                    .map(this::buildViewRepairOrderResponse)
                    .toList();

            return createSuccessResponse("Repair orders retrieved successfully", repairOrders);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    private RepairOrder findRepairOrderById(String repairOrderId) {
        return repairOrderRepository.findById(UUID.fromString(repairOrderId))
                .orElseThrow(() -> new InvalidTechnicianReportStateException(REPAIR_ORDER_NOT_FOUND));
    }

    private TechnicianReport findTechnicianReportById(String technicianReportId) {
        if (technicianReportId == null) {
            throw new InvalidDataTechnicianReport(TECHNICIAN_REPORT_DATA_NULL);
        }

        return technicianReportRepository.findByReportId(UUID.fromString(technicianReportId))
                .orElseThrow(() -> new InvalidTechnicianReportStateException(TECHNICIAN_REPORT_NOT_FOUND));
    }

    private void validateNoExistingNonRejectedReport(String repairOrderId) {
        UUID repairOrderIdUUID = UUID.fromString(repairOrderId);
        if (technicianReportRepository.existsByRepairOrderIdAndStatusNot(repairOrderIdUUID, REJECTED)) {
            throw new DatabaseException(REPAIR_ORDER_ALREADY_EXISTS);
        }
    }

    private TechnicianReport createNewTechnicianReport(
            CreateTechnicianReportDraftRequest request,
            RepairOrder repairOrder,
            AuthenticatedUser technician) {

        return TechnicianReport.builder()
                .reportId(UUID.randomUUID())
                .repairOrder(repairOrder)
                .technicianId(technician.id())
                .diagnosis(request.getDiagnosis())
                .actionPlan(request.getActionPlan())
                .estimatedCost(request.getEstimatedCost())
                .estimatedTimeSeconds(request.getEstimatedTimeSeconds())
                .build();
    }

    private void updateTechnicianReportFields(TechnicianReport report, CreateTechnicianReportDraftRequest request) {
        report.setDiagnosis(request.getDiagnosis());
        report.setActionPlan(request.getActionPlan());
        report.setEstimatedCost(request.getEstimatedCost());
        report.setEstimatedTimeSeconds(request.getEstimatedTimeSeconds());
    }

    private void validateRepairOrderForReportCreation(RepairOrder repairOrder, AuthenticatedUser technician) {
        if (repairOrder.getStatus() != RepairOrderStatus.PENDING_CONFIRMATION) {
            throw new InvalidTechnicianReportStateException(REPAIR_ORDER_NOT_IN_PROGRESS);
        }

        if (!repairOrder.getTechnicianId().equals(technician.id())) {
            throw new IllegalAccessTechnicianReport("Technician", "create a report based on this repair order");
        }
    }

    private void validateTechnicianCanUpdateReport(TechnicianReport technicianReport, AuthenticatedUser technician) {
        if (!technicianReport.getTechnicianId().equals(technician.id())) {
            throw new IllegalAccessTechnicianReport("Technician", "update this report");
        }

        if (!technicianReport.technicianCanModify()) {
            throw new InvalidTechnicianReportStateException(ONLY_DRAFTS_CAN_BE_UPDATED);
        }
    }

    private void validateCustomerOwnsReportAndCanAct(TechnicianReport technicianReport, AuthenticatedUser customer, String requiredStatusMessage) {
        RepairOrder repairOrder = technicianReport.getRepairOrder();
        if (!repairOrder.getCustomerId().equals(customer.id())) {
            throw new IllegalAccessTechnicianReport("Customer", "perform this action on the report");
        }
        if (!SUBMITTED.equals(technicianReport.getStatus())) {
            throw new InvalidTechnicianReportStateException(requiredStatusMessage);
        }
    }

    private void validateTechnicianCanStartWork(TechnicianReport technicianReport, AuthenticatedUser technician) {
        if (!technicianReport.getTechnicianId().equals(technician.id())) {
            throw new InvalidTechnicianReportStateException(UNAUTHORIZED_REPORT_UPDATE);
        }

        if (!APPROVED.equals(technicianReport.getStatus())) {
            throw new InvalidTechnicianReportStateException(ONLY_APPROVED_REPORTS_CAN_START);
        }
    }

    private void validateTechnicianCanCompleteWork(TechnicianReport technicianReport, AuthenticatedUser technician) {
        if (!technicianReport.getTechnicianId().equals(technician.id())) {
            throw new InvalidTechnicianReportStateException(UNAUTHORIZED_REPORT_UPDATE);
        }

        if (!IN_PROGRESS.equals(technicianReport.getStatus())) {
            throw new InvalidTechnicianReportStateException(ONLY_IN_PROGRESS_REPORTS_CAN_COMPLETE);
        }
    }

    private void validateCustomerCanViewStatus(String status) {
        if (DRAFT.equals(upperCase(status))) {
            throw new InvalidTechnicianReportStateException(CUSTOMER_CANNOT_VIEW_DRAFTS);
        }
    }

    private void validateUserCanViewReport(TechnicianReport technicianReport, AuthenticatedUser user) {
        if (user.role() == UserRole.CUSTOMER) {
            if (!technicianReport.getRepairOrder().getCustomerId().equals(user.id())) {
                throw new InvalidTechnicianReportStateException(UNAUTHORIZED_REPORT_ACCESS);
            }
            technicianReport.customerCanSee();
        } else if (user.role() == UserRole.TECHNICIAN && !technicianReport.getTechnicianId().equals(user.id())) {
            throw new InvalidTechnicianReportStateException(UNAUTHORIZED_REPORT_ACCESS);
        }
    }

    private List<TechnicianReport> getCustomerVisibleReports(String status, AuthenticatedUser customer) {
        List<TechnicianReport> reports = technicianReportRepository.findAllByStatus(status);
        return reports.stream()
                .filter(report -> report.getRepairOrder().getCustomerId().equals(customer.id()))
                .toList();
    }

    private void publishRepairCompletedEvent(TechnicianReport technicianReport) {
        RepairOrderCompletedEvent event = RepairOrderCompletedEvent.builder()
                .repairOrderId(technicianReport.getRepairOrder().getId())
                .technicianId(technicianReport.getTechnicianId())
                .amount(technicianReport.getEstimatedCost())
                .completedAt(Instant.now())
                .build();

        repairEventPublisher.publishRepairCompleted(event);
    }

    private void updateRepairOrderStatusToCompleted(TechnicianReport technicianReport) {
        technicianReport.getRepairOrder().setStatus(RepairOrderStatus.COMPLETED);
    }

    private boolean isPendingConfirmation(RepairOrder repairOrder) {
        return repairOrder.getStatus() == RepairOrderStatus.PENDING_CONFIRMATION;
    }

    private ViewRepairOrderResponse buildViewRepairOrderResponse(RepairOrder ro) {
        return ViewRepairOrderResponse.builder()
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
                .build();
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

    private <T> GenericResponse<T> createSuccessResponse(String message, T data) {
        return new GenericResponse<>(true, message, data);
    }

    private <T> GenericResponse<T> handleException(Exception ex) {
        return new GenericResponse<>(false, ex.getMessage(), null);
    }

    private void logAuditAction(String action, String reportId, String userId) {
        auditLogger.logReportAction(action, reportId, userId);
    }

    private void logReportsAuditAction(List<TechnicianReport> reports, String action, String userId) {
        reports.forEach(report -> auditLogger.logReportAction(action, report.getReportId().toString(), userId));
    }
}
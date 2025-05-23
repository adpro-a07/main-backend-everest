package id.ac.ui.cs.advprog.everest.modules.technicianReport.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.repairorder.dto.ViewRepairOrderResponse;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.RepairOrder;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.dto.CreateTechnicianReportDraftRequest;
import id.ac.ui.cs.advprog.everest.modules.technicianReport.dto.TechnicianReportDraftResponse;

import java.util.List;

public interface TechnicianReportService {
    GenericResponse<TechnicianReportDraftResponse> createTechnicianReportDraft(
            CreateTechnicianReportDraftRequest createTechnicianReportDraft,
            AuthenticatedUser technician
    );

    GenericResponse<TechnicianReportDraftResponse> updateTechnicianReportDraft(
            String technicianReportDraftId,
            CreateTechnicianReportDraftRequest createTechnicianReportDraft,
            AuthenticatedUser technician
    );

    GenericResponse<TechnicianReportDraftResponse> deleteTechnicianReportDraft(
            String technicianReportDraftId,
            AuthenticatedUser technician
    );

    GenericResponse<TechnicianReportDraftResponse> submitTechnicianReportDraft(
            String technicianReportDraftId,
            AuthenticatedUser technician
    );

    GenericResponse<Void> acceptTechnicianReportSubmit(
            String technicianReportDraftId,
            AuthenticatedUser customer
    );

    GenericResponse<Void> rejectTechnicianReportSubmit(
            String technicianReportDraftId,
            AuthenticatedUser customer
    );

    GenericResponse<TechnicianReportDraftResponse> startWork(
            String technicianReportDraftId,
            AuthenticatedUser technician
    );

    GenericResponse<TechnicianReportDraftResponse> completeWork(
            String technicianReportDraftId,
            AuthenticatedUser technician
    );

    GenericResponse<List<TechnicianReportDraftResponse>> getTechnicianReportByStatusForTechnician(
            String status,
            AuthenticatedUser technician
    );

    GenericResponse<List<TechnicianReportDraftResponse>> getTechnicianReportByStatusForCustomer(
            String status,
            AuthenticatedUser customer
    );

    GenericResponse<TechnicianReportDraftResponse> getTechnicianReportById(
            String technicianReportDraftId,
            AuthenticatedUser user
    );

    GenericResponse<List<ViewRepairOrderResponse>> getRepairOrderByTechnicianId(
            AuthenticatedUser user
    );
}
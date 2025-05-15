package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.CreateTechnicianReportDraft;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.TechnicianReportDraftResponse;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model.TechnicianReport;

import java.util.List;

public interface TechnicianReportService {
    // Technician
    GenericResponse<TechnicianReportDraftResponse> createTechnicianReportDraft(
            CreateTechnicianReportDraft createTechnicianReportDraft,
            AuthenticatedUser technician
    );

    GenericResponse<TechnicianReportDraftResponse> updateTechnicianReportDraft(
            String technicianReportDraftId,
            CreateTechnicianReportDraft createTechnicianReportDraft,
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

    GenericResponse<List<TechnicianReportDraftResponse>> getTechnicianReportByStatus(
            String status,
            AuthenticatedUser technician
    );

    // User
    GenericResponse<Void> acceptTechnicianReportSubmit(
            String technicianReportDraftId,
            AuthenticatedUser customer
    );

    GenericResponse<Void> rejectTechnicianReportSubmit(
            String technicianReportDraftId,
            AuthenticatedUser customer
    );

    GenericResponse<List<TechnicianReportDraftResponse>> getTechnicianReportSubmissions(
            String status,
            AuthenticatedUser customer
    );

}

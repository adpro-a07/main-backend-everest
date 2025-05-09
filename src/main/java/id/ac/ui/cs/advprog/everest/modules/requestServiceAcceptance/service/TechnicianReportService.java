package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.service;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.IncomingRequest;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.RequestStatus;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.TechnicianReport;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.IncomingRequestRepository;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.TechnicianReportRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TechnicianReportService {
    private final TechnicianReportRepository technicianReportRepository;
    private final IncomingRequestRepository incomingRequestRepository;
    private final RequestService requestService;

    @Autowired
    public TechnicianReportService(TechnicianReportRepository technicianReportRepository,
                                   IncomingRequestRepository incomingRequestRepository,
                                   RequestService requestService) {
        this.technicianReportRepository = technicianReportRepository;
        this.incomingRequestRepository = incomingRequestRepository;
        this.requestService = requestService;
    }

    @Transactional
    public TechnicianReport createReport(TechnicianReport report) {
        IncomingRequest request = incomingRequestRepository.findByRequestIdAndTechnicianId(
                        report.getRequestId(), report.getTechnicianId())
                .orElseThrow(() -> new EntityNotFoundException("Request not found or not assigned to this technician"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Cannot create report for request in state: " + request.getStatus());
        }

        requestService.processRequestAction(report.getRequestId(), report.getTechnicianId(), "create_report");

        return technicianReportRepository.save(report);
    }

    @Transactional
    public TechnicianReport updateReport(TechnicianReport updatedReport) {
        TechnicianReport existingReport = technicianReportRepository.findById(updatedReport.getReportId())
                .orElseThrow(() -> new EntityNotFoundException("Report not found with id: " + updatedReport.getReportId()));

        if (!existingReport.getTechnicianId().equals(updatedReport.getTechnicianId())) {
            throw new IllegalArgumentException("Technician does not own this report");
        }

        IncomingRequest request = incomingRequestRepository.findById(updatedReport.getRequestId())
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));

        if (request.getStatus() == RequestStatus.REPORTED) {
            if (updatedReport.getEstimatedCost() != null && updatedReport.getEstimatedTime() != null) {
                requestService.processRequestAction(request.getRequestId(), request.getTechnicianId(), "create_estimate");
            }
        }

        return technicianReportRepository.save(updatedReport);
    }

    @Transactional(readOnly = true)
    public Optional<TechnicianReport> getReportByRequestId(Long requestId) {
        return technicianReportRepository.findByRequestId(requestId);
    }

    @Transactional(readOnly = true)
    public List<TechnicianReport> getReportsByTechnicianId(Long technicianId) {
        return technicianReportRepository.findByTechnicianId(technicianId);
    }
}
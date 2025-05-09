package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.service;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.*;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.IncomingRequestRepository;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.TechnicianReportRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TechnicianReportServiceTest {

    @Mock
    private TechnicianReportRepository technicianReportRepository;

    @Mock
    private IncomingRequestRepository incomingRequestRepository;

    @Mock
    private RequestService requestService;

    @InjectMocks
    private TechnicianReportService technicianReportService;

    private Long reportId = 1L;
    private Long requestId = 2L;
    private Long technicianId = 3L;
    private IncomingRequest incomingRequest;
    private TechnicianReport technicianReport;

    @BeforeEach
    void setUp() {
        incomingRequest = new IncomingRequest(requestId, technicianId, "Computer issue", RequestStatus.PENDING);

        technicianReport = TechnicianReport.builder()
                .reportId(reportId)
                .requestId(requestId)
                .technicianId(technicianId)
                .diagnosis("Hard drive failure")
                .actionPlan("Replace hard drive")
                .build();
    }

    @Test
    void testCreateReport() {
        when(incomingRequestRepository.findByRequestIdAndTechnicianId(requestId, technicianId))
                .thenReturn(Optional.of(incomingRequest));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenReturn(technicianReport);

        TechnicianReport result = technicianReportService.createReport(technicianReport);

        assertEquals(technicianReport, result);
        verify(incomingRequestRepository).findByRequestIdAndTechnicianId(requestId, technicianId);
        verify(requestService).processRequestAction(requestId, technicianId, "create_report");
        verify(technicianReportRepository).save(technicianReport);
    }

    @Test
    void testCreateReport_RequestNotFound() {
        when(incomingRequestRepository.findByRequestIdAndTechnicianId(requestId, technicianId))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                technicianReportService.createReport(technicianReport)
        );

        assertEquals("Request not found or not assigned to this technician", exception.getMessage());
        verify(incomingRequestRepository).findByRequestIdAndTechnicianId(requestId, technicianId);
        verify(requestService, never()).processRequestAction(anyLong(), anyLong(), anyString());
        verify(technicianReportRepository, never()).save(any(TechnicianReport.class));
    }

    @Test
    void testCreateReport_InvalidState() {
        IncomingRequest reportedRequest = new IncomingRequest(requestId, technicianId, "Computer issue", RequestStatus.REPORTED);
        when(incomingRequestRepository.findByRequestIdAndTechnicianId(requestId, technicianId))
                .thenReturn(Optional.of(reportedRequest));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                technicianReportService.createReport(technicianReport)
        );

        assertEquals("Cannot create report for request in state: REPORTED", exception.getMessage());
        verify(incomingRequestRepository).findByRequestIdAndTechnicianId(requestId, technicianId);
        verify(requestService, never()).processRequestAction(anyLong(), anyLong(), anyString());
        verify(technicianReportRepository, never()).save(any(TechnicianReport.class));
    }

    @Test
    void testUpdateReport() {
        TechnicianReport existingReport = TechnicianReport.builder()
                .reportId(reportId)
                .requestId(requestId)
                .technicianId(technicianId)
                .diagnosis("Initial diagnosis")
                .actionPlan("Initial plan")
                .build();

        TechnicianReport updatedReport = TechnicianReport.builder()
                .reportId(reportId)
                .requestId(requestId)
                .technicianId(technicianId)
                .diagnosis("Updated diagnosis")
                .actionPlan("Updated plan")
                .build();

        when(technicianReportRepository.findById(reportId)).thenReturn(Optional.of(existingReport));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenReturn(updatedReport);
        when(incomingRequestRepository.findById(requestId)).thenReturn(Optional.of(incomingRequest));

        TechnicianReport result = technicianReportService.updateReport(updatedReport);
        assertEquals(updatedReport, result);
        verify(technicianReportRepository).findById(reportId);
        verify(technicianReportRepository).save(updatedReport);
    }

    @Test
    void testUpdateReport_WithEstimates() {
        TechnicianReport existingReport = TechnicianReport.builder()
                .reportId(reportId)
                .requestId(requestId)
                .technicianId(technicianId)
                .diagnosis("Initial diagnosis")
                .actionPlan("Initial plan")
                .build();

        TechnicianReport updatedReport = TechnicianReport.builder()
                .reportId(reportId)
                .requestId(requestId)
                .technicianId(technicianId)
                .diagnosis("Updated diagnosis")
                .actionPlan("Updated plan")
                .estimatedCost(new BigDecimal("500.00"))
                .estimatedTime(Duration.ofHours(2))
                .build();

        IncomingRequest reportedRequest = new IncomingRequest(requestId, technicianId, "Computer issue", RequestStatus.REPORTED);

        when(technicianReportRepository.findById(reportId)).thenReturn(Optional.of(existingReport));
        when(incomingRequestRepository.findById(requestId)).thenReturn(Optional.of(reportedRequest));
        when(technicianReportRepository.save(any(TechnicianReport.class))).thenReturn(updatedReport);

        TechnicianReport result = technicianReportService.updateReport(updatedReport);

        assertEquals(updatedReport, result);
        verify(technicianReportRepository).findById(reportId);
        verify(incomingRequestRepository).findById(requestId);
        verify(requestService).processRequestAction(requestId, technicianId, "create_estimate");
        verify(technicianReportRepository).save(updatedReport);
    }

    @Test
    void testUpdateReport_NotFound() {
        when(technicianReportRepository.findById(reportId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                technicianReportService.updateReport(technicianReport)
        );

        assertEquals("Report not found with id: " + reportId, exception.getMessage());
        verify(technicianReportRepository).findById(reportId);
        verify(technicianReportRepository, never()).save(any(TechnicianReport.class));
    }

    @Test
    void testUpdateReport_NotOwnedByTechnician() {
        Long differentTechnicianId = 999L;
        TechnicianReport existingReport = TechnicianReport.builder()
                .reportId(reportId)
                .requestId(requestId)
                .technicianId(differentTechnicianId)  // Different technician
                .diagnosis("Initial diagnosis")
                .actionPlan("Initial plan")
                .build();

        when(technicianReportRepository.findById(reportId)).thenReturn(Optional.of(existingReport));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                technicianReportService.updateReport(technicianReport)
        );

        assertEquals("Technician does not own this report", exception.getMessage());
        verify(technicianReportRepository).findById(reportId);
        verify(technicianReportRepository, never()).save(any(TechnicianReport.class));
    }

    @Test
    void testGetReportByRequestId() {
        when(technicianReportRepository.findByRequestId(requestId)).thenReturn(Optional.of(technicianReport));

        Optional<TechnicianReport> result = technicianReportService.getReportByRequestId(requestId);

        assertTrue(result.isPresent());
        assertEquals(technicianReport, result.get());
        verify(technicianReportRepository).findByRequestId(requestId);
    }

    @Test
    void testGetReportsByTechnicianId() {
        List<TechnicianReport> expectedReports = Arrays.asList(technicianReport);
        when(technicianReportRepository.findByTechnicianId(technicianId)).thenReturn(expectedReports);

        List<TechnicianReport> result = technicianReportService.getReportsByTechnicianId(technicianId);

        assertEquals(expectedReports, result);
        verify(technicianReportRepository).findByTechnicianId(technicianId);
    }
}
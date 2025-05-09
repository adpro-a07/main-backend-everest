package id.ac.ui.cs.advprog.everest.modules.report.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.modules.report.dto.ReportResponse;
import id.ac.ui.cs.advprog.everest.modules.report.model.Report;
import id.ac.ui.cs.advprog.everest.modules.report.model.enums.ReportStatus;
import id.ac.ui.cs.advprog.everest.modules.report.repository.ReportRepository;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private Report sampleReport;
    private UUID sampleReportId;
    private AuthenticatedUser mockUser;

    @BeforeEach
    void setUp() {
        sampleReportId = UUID.randomUUID();
        mockUser = new AuthenticatedUser(
                UUID.randomUUID(),
                "tech@example.com",
                "Technician",
                UserRole.TECHNICIAN,
                "555-4321",
                Instant.now(),
                Instant.now(),
                "Bandung",
                null,
                0,
                0L
        );

        sampleReport = Report.builder()
                .id(sampleReportId)
                .technicianName("John Doe")
                .repairDetails("Fixed broken screen")
                .repairDate(LocalDate.now())
                .status(ReportStatus.COMPLETED)
                .build();
    }

    @Test
    void testGetAllReports() {
        when(reportRepository.findAll()).thenReturn(List.of(sampleReport));

        List<ReportResponse> result = reportService.getAllReports(mockUser);

        assertEquals(1, result.size());
        assertEquals(sampleReport.getTechnicianName(), result.get(0).getTechnicianName());
        verify(reportRepository).findAll();
    }

    @Test
    void testGetReportById() {
        when(reportRepository.findById(sampleReportId)).thenReturn(Optional.of(sampleReport));

        ReportResponse result = reportService.getReportById(sampleReportId, mockUser);

        assertNotNull(result);
        assertEquals(sampleReportId.toString(), result.getId().toString());
        assertEquals(sampleReport.getRepairDetails(), result.getRepairDetails());
        verify(reportRepository).findById(sampleReportId);
    }

    @Test
    void testGetReportByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(reportRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                reportService.getReportById(nonExistentId, mockUser)
        );
        verify(reportRepository).findById(nonExistentId);
    }

    @Test
    void testGetReportsByTechnician() {
        when(reportRepository.findByTechnicianNameContainingIgnoreCase("John"))
                .thenReturn(List.of(sampleReport));

        List<ReportResponse> result = reportService.getReportsByTechnician("John", mockUser);

        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getTechnicianName());
        verify(reportRepository).findByTechnicianNameContainingIgnoreCase("John");
    }

    @Test
    void testGetReportsByTechnician_NoMatch() {
        when(reportRepository.findByTechnicianNameContainingIgnoreCase("Unknown"))
                .thenReturn(Collections.emptyList());

        List<ReportResponse> result = reportService.getReportsByTechnician("Unknown", mockUser);
        assertTrue(result.isEmpty(), "Should return empty list for unknown technician");
    }

    @Test
    void testGetReportsByTechnician_NullInput() {
        when(reportRepository.findByTechnicianNameContainingIgnoreCase(null))
                .thenReturn(Collections.emptyList());

        List<ReportResponse> result = reportService.getReportsByTechnician(null, mockUser);
        assertTrue(result.isEmpty(), "Should handle null technician name");
    }

    @Test
    void testGetReportsByStatus() {
        when(reportRepository.findByStatus(ReportStatus.COMPLETED))
                .thenReturn(List.of(sampleReport));

        List<ReportResponse> result = reportService.getReportsByStatus(
                ReportStatus.COMPLETED,
                mockUser
        );

        assertEquals(1, result.size());
        assertEquals("COMPLETED", result.get(0).getStatus());
        verify(reportRepository).findByStatus(ReportStatus.COMPLETED);
    }

    @Test
    void testGetReportsByStatus_NoReportsReturned() {
        when(reportRepository.findByStatus(ReportStatus.CANCELLED))
                .thenReturn(Collections.emptyList());

        List<ReportResponse> result = reportService.getReportsByStatus(
                ReportStatus.CANCELLED,
                mockUser
        );

        assertTrue(result.isEmpty(), "Should return empty list for cancelled status");
    }

    @Test
    void testGetReportsByStatus_NullStatus() {
        when(reportRepository.findByStatus(null))
                .thenReturn(Collections.emptyList());

        List<ReportResponse> result = reportService.getReportsByStatus(null, mockUser);
        assertTrue(result.isEmpty(), "Should handle null status");
    }

    @Test
    void testGetReportsByTechnicianAndStatus() {
        when(reportRepository.findByTechnicianNameContainingIgnoreCaseAndStatus(
                "John",
                ReportStatus.COMPLETED
        )).thenReturn(List.of(sampleReport));

        List<ReportResponse> result = reportService.getReportsByTechnicianAndStatus(
                "John",
                ReportStatus.COMPLETED,
                mockUser
        );

        assertEquals(1, result.size());
        verify(reportRepository).findByTechnicianNameContainingIgnoreCaseAndStatus(
                "John",
                ReportStatus.COMPLETED
        );
    }

    @Test
    void testGetReportsByTechnicianAndStatus_PartialMatch() {
        Report anotherReport = Report.builder()
                .technicianName("Johnny Cash")
                .status(ReportStatus.COMPLETED)
                .build();

        when(reportRepository.findByTechnicianNameContainingIgnoreCaseAndStatus(
                "john",
                ReportStatus.COMPLETED
        )).thenReturn(List.of(sampleReport, anotherReport));

        List<ReportResponse> result = reportService.getReportsByTechnicianAndStatus(
                "john",
                ReportStatus.COMPLETED,
                mockUser
        );

        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getTechnicianName());
        assertEquals("Johnny Cash", result.get(1).getTechnicianName());
    }

    @Test
    void testEmptyReportList() {
        when(reportRepository.findAll()).thenReturn(Collections.emptyList());

        List<ReportResponse> result = reportService.getAllReports(mockUser);
        assertTrue(result.isEmpty(), "Should handle empty repository");
    }

    @Test
    void testSpecialCharactersInTechnicianName() {
        Report weirdNameReport = Report.builder()
                .technicianName("Jóhn Dôe")
                .status(ReportStatus.COMPLETED)
                .build();

        when(reportRepository.findByTechnicianNameContainingIgnoreCase("john"))
                .thenReturn(List.of(weirdNameReport));

        List<ReportResponse> result = reportService.getReportsByTechnician("john", mockUser);

        assertEquals(1, result.size());
        assertEquals("Jóhn Dôe", result.get(0).getTechnicianName());
    }
}
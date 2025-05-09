package id.ac.ui.cs.advprog.everest.modules.report.service;


import id.ac.ui.cs.advprog.everest.modules.report.dto.ReportResponse;
import id.ac.ui.cs.advprog.everest.modules.report.model.Report;
import id.ac.ui.cs.advprog.everest.modules.report.model.enums.ReportStatus;
import id.ac.ui.cs.advprog.everest.modules.report.service.ReportServiceImpl;
import id.ac.ui.cs.advprog.everest.modules.report.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @BeforeEach
    void setUp() {
        sampleReportId = UUID.randomUUID();

        sampleReport = Report.builder()
                .technicianName("John Doe")
                .repairDetails("Fixed broken screen")
                .repairDate(LocalDate.now())
                .status(ReportStatus.COMPLETED)
                .build();
        sampleReport.setId(sampleReportId);
    }

    @Test
    void testGetAllReports() {
        List<Report> reportList = new ArrayList<>();
        reportList.add(sampleReport);

        when(reportRepository.findAll()).thenReturn(reportList);

        List<ReportResponse> result = reportService.getAllReports();

        assertEquals(1, result.size());
        assertEquals(sampleReport.getTechnicianName(), result.get(0).getTechnicianName());
        verify(reportRepository, times(1)).findAll();
    }

    @Test
    void testGetReportById() {
        when(reportRepository.findById(sampleReportId)).thenReturn(Optional.of(sampleReport));

        // Execute
        ReportResponse result = reportService.getReportById(sampleReportId);

        // Verify
        assertNotNull(result);
        assertEquals(sampleReport.getTechnicianName(), result.getTechnicianName());
        assertEquals(sampleReport.getRepairDetails(), result.getRepairDetails());
        verify(reportRepository, times(1)).findById(sampleReportId);
    }

    @Test
    void testGetReportByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        when(reportRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(RuntimeException.class, () -> {
            reportService.getReportById(nonExistentId);
        });
        verify(reportRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void testGetReportsByTechnician() {
        // Setup
        List<Report> reportList = new ArrayList<>();
        reportList.add(sampleReport);

        String technicianName = "John";
        when(reportRepository.findByTechnicianNameContainingIgnoreCase(technicianName))
                .thenReturn(reportList);

        // Execute
        List<ReportResponse> result = reportService.getReportsByTechnician(technicianName);

        // Verify
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getTechnicianName());
        verify(reportRepository, times(1))
                .findByTechnicianNameContainingIgnoreCase(technicianName);
    }

    @Test
    void testGetReportsByTechnician_NoMatch() {
        when(reportRepository.findByTechnicianNameContainingIgnoreCase("Unknown"))
                .thenReturn(Collections.emptyList());

        List<ReportResponse> result = reportService.getReportsByTechnician("Unknown");
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetReportsByTechnician_NullInput() {
        when(reportRepository.findByTechnicianNameContainingIgnoreCase(null))
                .thenReturn(Collections.emptyList());

        List<ReportResponse> result = reportService.getReportsByTechnician(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetReportsByStatus() {
        List<Report> reportList = new ArrayList<>();
        reportList.add(sampleReport);

        when(reportRepository.findByStatus(ReportStatus.COMPLETED))
                .thenReturn(reportList);

        List<ReportResponse> result = reportService.getReportsByStatus(ReportStatus.COMPLETED);
        assertEquals(1, result.size());
        verify(reportRepository, times(1)).findByStatus(ReportStatus.COMPLETED);
    }

    @Test
    void testGetReportsByStatus_CaseInsensitive() {
        List<Report> reportList = new ArrayList<>();
        reportList.add(sampleReport);

        when(reportRepository.findByStatus(ReportStatus.COMPLETED))
                .thenReturn(reportList);

        List<ReportResponse> result = reportService.getReportsByStatus(ReportStatus.COMPLETED);
        assertEquals(1, result.size());
    }

    @Test
    void testGetReportsByStatus_NoReportsReturned() {
        when(reportRepository.findByStatus(ReportStatus.CANCELLED))
                .thenReturn(Collections.emptyList());

        List<ReportResponse> result = reportService.getReportsByStatus(ReportStatus.CANCELLED);

        assertTrue(result.isEmpty(), "Expected no reports for status CANCELLED");
        verify(reportRepository, times(1)).findByStatus(ReportStatus.CANCELLED);
    }

    @Test
    void testGetReportsByStatus_NullStatus() {
        when(reportRepository.findByStatus(null)).thenReturn(Collections.emptyList());

        List<ReportResponse> result = reportService.getReportsByStatus(null);

        assertTrue(result.isEmpty(), "Expected empty result when status is null");
        verify(reportRepository, times(1)).findByStatus(null);
    }

    @Test
    void testGetReportsByTechnicianAndStatus() {
        List<Report> reportList = new ArrayList<>();
        reportList.add(sampleReport);

        when(reportRepository.findByTechnicianNameContainingIgnoreCaseAndStatus(
                "John",
                ReportStatus.COMPLETED))
                .thenReturn(reportList);

        List<ReportResponse> result = reportService.getReportsByTechnicianAndStatus("John", ReportStatus.COMPLETED);
        assertEquals(1, result.size());
        verify(reportRepository, times(1))
                .findByTechnicianNameContainingIgnoreCaseAndStatus("John", ReportStatus.COMPLETED);
    }

    @Test
    void testGetReportsByTechnicianAndStatus_PartialMatch() {
        Report anotherReport = Report.builder()
                .technicianName("Johnny Cash")
                .status(ReportStatus.COMPLETED)
                .build();

        when(reportRepository.findByTechnicianNameContainingIgnoreCaseAndStatus(
                "john",
                ReportStatus.COMPLETED))
                .thenReturn(List.of(sampleReport, anotherReport));

        List<ReportResponse> result = reportService.getReportsByTechnicianAndStatus("john", ReportStatus.COMPLETED);
        assertEquals(2, result.size());
    }

    @Test
    void testGetReportsByTechnicianAndStatus_NoMatch() {
        when(reportRepository.findByTechnicianNameContainingIgnoreCaseAndStatus(
                "Alice",
                ReportStatus.PENDING_CONFIRMATION))
                .thenReturn(Collections.emptyList());

        List<ReportResponse> result = reportService.getReportsByTechnicianAndStatus("Alice", ReportStatus.PENDING_CONFIRMATION);
        assertTrue(result.isEmpty());
    }

    @Test
    void testEmptyReportList() {
        when(reportRepository.findAll()).thenReturn(Collections.emptyList());
        List<ReportResponse> result = reportService.getAllReports();
        assertTrue(result.isEmpty());
    }

    @Test
    void testSpecialCharactersInTechnicianName() {
        Report weirdNameReport = Report.builder()
                .technicianName("Jóhn Dôe")
                .status(ReportStatus.COMPLETED)
                .build();

        when(reportRepository.findByTechnicianNameContainingIgnoreCase("john"))
                .thenReturn(List.of(weirdNameReport));

        List<ReportResponse> result = reportService.getReportsByTechnician("john");
        assertEquals(1, result.size());
    }
}
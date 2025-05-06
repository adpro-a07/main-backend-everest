package id.ac.ui.cs.advprog.everest.service;

import id.ac.ui.cs.advprog.everest.model.Report;
import id.ac.ui.cs.advprog.everest.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private Report sampleReport;

    @BeforeEach
    void setUp() {
        // Set up a sample report
        sampleReport = Report.builder()
                .technicianName("John Doe")
                .repairDetails("Fixed broken screen")
                .repairDate(LocalDate.now())
                .status("Completed")
                .build();
        sampleReport.setId(1L);
    }

    @Test
    void testGetAllReports() {
        List<Report> reportList = new ArrayList<>();
        reportList.add(sampleReport);

        when(reportRepository.findAll()).thenReturn(reportList);

        List<Report> result = reportService.getAllReports();

        assertEquals(1, result.size());
        assertEquals(sampleReport.getTechnicianName(), result.get(0).getTechnicianName());
        verify(reportRepository, times(1)).findAll();
    }

    @Test
    void testGetReportById() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(sampleReport));

        // Execute
        Report result = reportService.getReportById(1L);

        // Verify
        assertNotNull(result);
        assertEquals(sampleReport.getTechnicianName(), result.getTechnicianName());
        assertEquals(sampleReport.getRepairDetails(), result.getRepairDetails());
        verify(reportRepository, times(1)).findById(1L);
    }

    @Test
    void testGetReportByIdNotFound() {
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(RuntimeException.class, () -> {
            reportService.getReportById(999L);
        });
        verify(reportRepository, times(1)).findById(999L);
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
        List<Report> result = reportService.getReportsByTechnician(technicianName);

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

        List<Report> result = reportService.getReportsByTechnician("Unknown");
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetReportsByTechnician_NullInput() {
        when(reportRepository.findByTechnicianNameContainingIgnoreCase(null))
                .thenReturn(Collections.emptyList());

        List<Report> result = reportService.getReportsByTechnician(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetReportsByStatus() {
        List<Report> reportList = new ArrayList<>();
        reportList.add(sampleReport);

        when(reportRepository.findByStatusIgnoreCase("Completed"))
                .thenReturn(reportList);

        List<Report> result = reportService.getReportsByStatus("Completed");
        assertEquals(1, result.size());
        verify(reportRepository, times(1)).findByStatusIgnoreCase("Completed");
    }

    @Test
    void testGetReportsByStatus_CaseInsensitive() {
        List<Report> reportList = new ArrayList<>();
        reportList.add(sampleReport);

        when(reportRepository.findByStatusIgnoreCase("completed"))
                .thenReturn(reportList);

        List<Report> result = reportService.getReportsByStatus("completed");
        assertEquals(1, result.size());
    }

    @Test
    void testGetReportsByStatus_InvalidStatus() {
        when(reportRepository.findByStatusIgnoreCase("Invalid"))
                .thenReturn(Collections.emptyList());

        List<Report> result = reportService.getReportsByStatus("Invalid");
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetReportsByTechnicianAndStatus() {
        List<Report> reportList = new ArrayList<>();
        reportList.add(sampleReport);

        when(reportRepository.findByTechnicianNameContainingIgnoreCaseAndStatusIgnoreCase(
                "John",
                "Completed"))
                .thenReturn(reportList);

        List<Report> result = reportService.getReportsByTechnicianAndStatus("John", "Completed");
        assertEquals(1, result.size());
        verify(reportRepository, times(1))
                .findByTechnicianNameContainingIgnoreCaseAndStatusIgnoreCase("John", "Completed");
    }

    @Test
    void testGetReportsByTechnicianAndStatus_PartialMatch() {
        Report anotherReport = Report.builder()
                .technicianName("Johnny Cash")
                .status("Completed")
                .build();

        when(reportRepository.findByTechnicianNameContainingIgnoreCaseAndStatusIgnoreCase(
                "john",
                "completed"))
                .thenReturn(List.of(sampleReport, anotherReport));

        List<Report> result = reportService.getReportsByTechnicianAndStatus("john", "completed");
        assertEquals(2, result.size());
    }

    @Test
    void testGetReportsByTechnicianAndStatus_NoMatch() {
        when(reportRepository.findByTechnicianNameContainingIgnoreCaseAndStatusIgnoreCase(
                "Alice",
                "In Progress"))
                .thenReturn(Collections.emptyList());

        List<Report> result = reportService.getReportsByTechnicianAndStatus("Alice", "In Progress");
        assertTrue(result.isEmpty());
    }

    @Test
    void testEmptyReportList() {
        when(reportRepository.findAll()).thenReturn(Collections.emptyList());
        List<Report> result = reportService.getAllReports();
        assertTrue(result.isEmpty());
    }

    @Test
    void testSpecialCharactersInTechnicianName() {
        Report weirdNameReport = Report.builder()
                .technicianName("Jóhn Dôe")
                .status("Completed")
                .build();

        when(reportRepository.findByTechnicianNameContainingIgnoreCase("john"))
                .thenReturn(List.of(weirdNameReport));

        List<Report> result = reportService.getReportsByTechnician("john");
        assertEquals(1, result.size());
    }

    

}
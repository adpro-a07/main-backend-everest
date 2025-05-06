package id.ac.ui.cs.advprog.everest.controller;

import id.ac.ui.cs.advprog.everest.model.Report;
import id.ac.ui.cs.advprog.everest.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    private Report createSampleReport(String technician, String status) {
        return Report.builder()
                .technicianName(technician)
                .repairDetails("Test details")
                .repairDate(LocalDate.now())
                .status(status)
                .build();
    }

    @Test
    void testGetAllReportsWithoutFilters() throws Exception {
        // Setup
        Report report1 = createSampleReport("John Doe", "Completed");
        Report report2 = createSampleReport("Alice Smith", "In Progress");

        when(reportService.getAllReports()).thenReturn(Arrays.asList(report1, report2));

        // Execute & Verify
        mockMvc.perform(get("/admin/reports"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("reports", hasSize(2)))
                .andExpect(model().attribute("currentTechnician", ""))
                .andExpect(model().attribute("currentStatus", ""));

        verify(reportService, times(1)).getAllReports(); // Tambahkan ini
        verify(reportService, never()).getReportsByTechnician(any());
        verify(reportService, never()).getReportsByStatus(any());
    }

    @Test
    void testGetAllReportsWithTechnicianFilter() throws Exception {
        // Setup
        Report report = createSampleReport("John Doe", "Completed");

        when(reportService.getReportsByTechnician("john"))
                .thenReturn(Collections.singletonList(report));

        // Execute & Verify
        mockMvc.perform(get("/admin/reports")
                        .param("technician", "john"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("reports", hasSize(1)))
                .andExpect(model().attribute("currentTechnician", "john"));

        verify(reportService, times(1)).getReportsByTechnician("john");
    }

    @Test
    void testGetAllReportsWithStatusFilter() throws Exception {
        Report report = createSampleReport("John Doe", "Completed");

        when(reportService.getReportsByStatus("Completed"))
                .thenReturn(Collections.singletonList(report));

        mockMvc.perform(get("/admin/reports")
                        .param("status", "Completed"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("reports", Collections.singletonList(report)))
                .andExpect(model().attribute("currentStatus", "Completed"));
    }

    @Test
    void testGetAllReportsWithTechnicianAndStatusFilters() throws Exception {
        Report report = createSampleReport("John Doe", "Completed");

        when(reportService.getReportsByTechnicianAndStatus("john", "Completed"))
                .thenReturn(Collections.singletonList(report));

        mockMvc.perform(get("/admin/reports")
                        .param("technician", "john")
                        .param("status", "Completed"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("reports", Collections.singletonList(report)));
    }

    @Test
    void testViewReportDetail() throws Exception {
        Report report = createSampleReport( "John Doe", "Completed");

        when(reportService.getReportById(1L)).thenReturn(report);

        mockMvc.perform(get("/admin/reports/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("report/detail"))
                .andExpect(model().attribute("report", report));
    }

    @Test
    void testViewReportDetailNotFound() throws Exception {
        when(reportService.getReportById(999L))
                .thenThrow(new RuntimeException("Report not found"));

        mockMvc.perform(get("/admin/reports/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSpecialCharacterSearch() throws Exception {
        Report report = createSampleReport( "Jöhn Dœ", "Completed");

        when(reportService.getReportsByTechnician("öhn"))
                .thenReturn(Collections.singletonList(report));

        mockMvc.perform(get("/admin/reports")
                        .param("technician", "öhn"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("reports"));
    }

    @Test
    void testEmptySearchResults() throws Exception {
        when(reportService.getReportsByTechnicianAndStatus("unknown", "Invalid"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/reports")
                        .param("technician", "unknown")
                        .param("status", "Invalid"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("reports", Collections.emptyList()));
    }
}
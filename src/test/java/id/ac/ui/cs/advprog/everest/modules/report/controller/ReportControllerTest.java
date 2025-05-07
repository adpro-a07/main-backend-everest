package id.ac.ui.cs.advprog.everest.modules.report.controller;

import id.ac.ui.cs.advprog.everest.common.service.AuthServiceGrpcClient;
import id.ac.ui.cs.advprog.everest.modules.report.model.Report;
import id.ac.ui.cs.advprog.everest.modules.report.model.enums.ReportStatus;
import id.ac.ui.cs.advprog.everest.modules.report.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.hasItem;
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

    @MockBean
    private AuthServiceGrpcClient authServiceGrpcClient;

    private Report createSampleReport(String technician, ReportStatus status) {
        return Report.builder()
                .technicianName(technician)
                .repairDetails("Test details")
                .repairDate(LocalDate.now())
                .status(status)
                .build();
    }

    @Test
    void testGetAllReportsWithoutFilters() throws Exception {
        Report report1 = createSampleReport("John Doe", ReportStatus.COMPLETED);
        Report report2 = createSampleReport("Alice Smith", ReportStatus.PENDING);

        when(reportService.getAllReports()).thenReturn(Arrays.asList(report1, report2));

        mockMvc.perform(get("/admin/reports"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("reports", hasSize(2)))
                .andExpect(model().attribute("currentTechnician", ""))
                .andExpect(model().attribute("currentStatus", ""));

        verify(reportService, times(1)).getAllReports();
        verify(reportService, never()).getReportsByTechnician(anyString());
        verify(reportService, never()).getReportsByStatus(any(ReportStatus.class));
        verify(reportService, never()).getReportsByTechnicianAndStatus(anyString(), any(ReportStatus.class));
    }

    @Test
    void testGetAllReportsWithTechnicianFilter() throws Exception {
        Report report = createSampleReport("John Doe", ReportStatus.COMPLETED);

        when(reportService.getReportsByTechnician("john")).thenReturn(Collections.singletonList(report));

        mockMvc.perform(get("/admin/reports").param("technician", "john"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("reports", hasSize(1)))
                .andExpect(model().attribute("currentTechnician", "john"))
                .andExpect(model().attribute("currentStatus", ""));

        verify(reportService, times(1)).getReportsByTechnician("john");
        verify(reportService, never()).getReportsByStatus(any(ReportStatus.class));
        verify(reportService, never()).getReportsByTechnicianAndStatus(anyString(), any(ReportStatus.class));
    }

    @Test
    void testViewReportDetail() throws Exception {
        Report report = createSampleReport("John Doe", ReportStatus.COMPLETED);

        when(reportService.getReportById(1)).thenReturn(report);

        mockMvc.perform(get("/admin/reports/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("report/detail"))
                .andExpect(model().attribute("report", report));

        verify(reportService, times(1)).getReportById(1);
    }

    @Test
    void testViewReportDetailNotFound() throws Exception {
        when(reportService.getReportById(999))
                .thenThrow(new RuntimeException("Report not found"));

        mockMvc.perform(get("/admin/reports/999"))
                .andExpect(status().isNotFound());

        verify(reportService, times(1)).getReportById(999);
    }

    @Test
    void testSpecialCharacterSearch() throws Exception {
        Report report = createSampleReport("Jöhn Dœ", ReportStatus.COMPLETED);

        when(reportService.getReportsByTechnician("öhn"))
                .thenReturn(Collections.singletonList(report));

        mockMvc.perform(get("/admin/reports").param("technician", "öhn"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("reports"));

        verify(reportService, times(1)).getReportsByTechnician("öhn");
    }

    @Test
    void testEmptySearchResults() throws Exception {
        when(reportService.getReportsByTechnicianAndStatus("unknown", ReportStatus.PENDING))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/reports")
                        .param("technician", "unknown")
                        .param("status", ReportStatus.PENDING.name()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("reports", Collections.emptyList()));

        verify(reportService, times(1)).getReportsByTechnicianAndStatus("unknown", ReportStatus.PENDING);
    }
}

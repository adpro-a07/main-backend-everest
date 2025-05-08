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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

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
        Report report2 = createSampleReport("Alice Smith", ReportStatus.PENDING_CONFIRMATION);

        when(reportService.getAllReports()).thenReturn(Arrays.asList(report1, report2));

        mockMvc.perform(get("/api/v1/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].technicianName").value("John Doe"))
                .andExpect(jsonPath("$[1].technicianName").value("Alice Smith"));

        verify(reportService, times(1)).getAllReports();
    }

    @Test
    void testGetAllReportsWithTechnicianFilter() throws Exception {
        Report report = createSampleReport("John Doe", ReportStatus.COMPLETED);

        when(reportService.getReportsByTechnician("john")).thenReturn(Collections.singletonList(report));

        mockMvc.perform(get("/api/v1/reports")
                        .param("technician", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));

        verify(reportService, times(1)).getReportsByTechnician("john");
    }

    @Test
    void testGetReportById() throws Exception {
        Report report = createSampleReport("John Doe", ReportStatus.COMPLETED);
        report.setId(1);

        when(reportService.getReportById(1)).thenReturn(report);

        mockMvc.perform(get("/api/v1/reports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L)) // Gunakan 1L untuk Long
                .andExpect(jsonPath("$.technicianName").value("John Doe"));
    }

    @Test
    void testGetReportByIdNotFound() throws Exception {
        when(reportService.getReportById(999))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Report not found with id: 999"
                ));

        mockMvc.perform(
                        get("/api/v1/reports/999")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Report not found with id: 999"));
    }

    @Test
    void testSearchWithSpecialCharacters() throws Exception {
        Report report = createSampleReport("Jöhn Dœ", ReportStatus.COMPLETED);

        when(reportService.getReportsByTechnician("öhn")).thenReturn(Collections.singletonList(report));

        mockMvc.perform(get("/api/v1/reports")
                        .param("technician", "öhn"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].technicianName").value("Jöhn Dœ"));
    }

    @Test
    void testCombinedSearchFilters() throws Exception {
        Report report = createSampleReport("John Doe", ReportStatus.COMPLETED);

        when(reportService.getReportsByTechnicianAndStatus("john", ReportStatus.COMPLETED))
                .thenReturn(Collections.singletonList(report));

        mockMvc.perform(get("/api/v1/reports")
                        .param("technician", "john")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }
}

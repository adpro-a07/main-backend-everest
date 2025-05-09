package id.ac.ui.cs.advprog.everest.modules.report.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import id.ac.ui.cs.advprog.everest.common.exception.GlobalExceptionHandler;
import id.ac.ui.cs.advprog.everest.common.exception.ResourceNotFoundException;
import id.ac.ui.cs.advprog.everest.common.service.AuthServiceGrpcClient;
import id.ac.ui.cs.advprog.everest.modules.report.dto.ReportRequest;
import id.ac.ui.cs.advprog.everest.modules.report.dto.ReportResponse;
import id.ac.ui.cs.advprog.everest.modules.report.excecption.ReportExceptionHandler;
import id.ac.ui.cs.advprog.everest.modules.report.model.Report;
import id.ac.ui.cs.advprog.everest.modules.report.model.enums.ReportStatus;
import id.ac.ui.cs.advprog.everest.modules.report.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        GlobalExceptionHandler.class,
        ReportExceptionHandler.class  // <-- tambahkan ini
})
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private AuthServiceGrpcClient authServiceGrpcClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private ReportResponse createSampleReportResponse(String technician, ReportStatus status) {
        return ReportResponse.builder()
                .technicianName(technician)
                .repairDetails("Test details")
                .repairDate(LocalDate.now())
                .status(status.name())
                .build();
    }

    private ReportRequest createSampleReportRequest(String technician, ReportStatus status) {
        return ReportRequest.builder()
                .technicianName(technician)
                .repairDetails("Test details")
                .repairDate(LocalDate.now())
                .status(status)
                .build();
    }

    @Test
    void testGetAllReportsWithoutFilters() throws Exception {
        ReportResponse report1 = createSampleReportResponse("John Doe", ReportStatus.COMPLETED);
        ReportResponse report2 = createSampleReportResponse("Alice Smith", ReportStatus.PENDING_CONFIRMATION);

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
        ReportResponse report = createSampleReportResponse("John Doe", ReportStatus.COMPLETED);

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
        UUID reportId = UUID.randomUUID();

        // Create the response with the specific ID we want to test
        ReportResponse report = ReportResponse.builder()
                .id(reportId)
                .technicianName("John Doe")
                .repairDetails("Test details")
                .repairDate(LocalDate.now())
                .status(ReportStatus.COMPLETED.name())
                .build();

        when(reportService.getReportById(eq(reportId))).thenReturn(report);

        mockMvc.perform(get("/api/v1/reports/{id}", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reportId.toString()))
                .andExpect(jsonPath("$.technicianName").value("John Doe"));

        verify(reportService).getReportById(eq(reportId));
    }

    @Test
    void testGetReportByIdNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        when(reportService.getReportById(nonExistentId))
                .thenThrow(new ResourceNotFoundException(
                        "Report not found with id: " + nonExistentId
                ));

        mockMvc.perform(get("/api/v1/reports/{id}", nonExistentId)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Report not found with id: " + nonExistentId))
                .andExpect(jsonPath("$.path").value("/api/v1/reports/" + nonExistentId));
    }

    @Test
    void testSearchWithSpecialCharacters() throws Exception {
        ReportResponse report = createSampleReportResponse("Jöhn Dœ", ReportStatus.COMPLETED);

        when(reportService.getReportsByTechnician("öhn")).thenReturn(Collections.singletonList(report));

        mockMvc.perform(get("/api/v1/reports")
                        .param("technician", "öhn"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].technicianName").value("Jöhn Dœ"));
    }

    @Test
    void testCombinedSearchFilters() throws Exception {
        ReportResponse report = createSampleReportResponse("John Doe", ReportStatus.COMPLETED);

        when(reportService.getReportsByTechnicianAndStatus("john", ReportStatus.COMPLETED))
                .thenReturn(Collections.singletonList(report));

        mockMvc.perform(get("/api/v1/reports")
                        .param("technician", "john")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }

    @Test
    void testGetReportByStatus() throws Exception {
        ReportResponse report = createSampleReportResponse("John Doe", ReportStatus.COMPLETED);
        List<ReportResponse> filtered = Collections.singletonList(report);

        when(reportService.getReportsByStatus(ReportStatus.COMPLETED))
                .thenReturn(filtered);

        mockMvc.perform(get("/api/v1/reports")
                        .param("status", "COMPLETED")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].technicianName").value("John Doe"))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));

        verify(reportService, times(1)).getReportsByStatus(ReportStatus.COMPLETED);
    }
}

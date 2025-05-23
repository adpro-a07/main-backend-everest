package id.ac.ui.cs.advprog.everest.modules.technicianReport.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TechnicianReportAuditLogger {
    @Async
    public void logReportAction(String action, String reportId, String technicianId) {
        System.out.printf("Audit: %s on report %s by technician %s%n", action, reportId, technicianId);
    }
}
package id.ac.ui.cs.advprog.everest.modules.technicianreport.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TechnicianReportAuditLogger {
    @Async
    public void logReportAction(String action, String reportId, String technicianId) {
        org.slf4j.LoggerFactory.getLogger(TechnicianReportAuditLogger.class)
                .info("Audit: {} on report {} by technician {}", action, reportId, technicianId);
    }
}
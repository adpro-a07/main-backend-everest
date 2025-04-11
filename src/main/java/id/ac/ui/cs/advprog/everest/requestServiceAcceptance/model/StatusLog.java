package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.model;

import java.time.LocalDateTime;

public class StatusLog {
    private final Long requestId;
    private final RequestStatus oldStatus;
    private final RequestStatus newStatus;
    private final Long technicianId;
    private final LocalDateTime timestamp;

    public StatusLog(Long requestId, RequestStatus oldStatus, RequestStatus newStatus, Long technicianId) {
        this.requestId = requestId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.technicianId = technicianId;
        this.timestamp = LocalDateTime.now();
    }

    public Long getRequestId() {
        return requestId;
    }

    public RequestStatus getOldStatus() {
        return oldStatus;
    }

    public RequestStatus getNewStatus() {
        return newStatus;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("StatusLog{requestId=%d, oldStatus=%s, newStatus=%s, " +
                        "technicianId=%d, timestamp=%s}",
                requestId, oldStatus, newStatus, technicianId, timestamp);
    }
}
package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "status_logs")
@Getter
@NoArgsConstructor // Required for JPA
public class StatusLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @Column(name = "request_id")
    private Long requestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private RequestStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status")
    private RequestStatus newStatus;

    @Column(name = "technician_id")
    private Long technicianId;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    public StatusLog(Long requestId, RequestStatus oldStatus, RequestStatus newStatus, Long technicianId) {
        this.requestId = requestId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.technicianId = technicianId;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("StatusLog{requestId=%d, oldStatus=%s, newStatus=%s, " +
                        "technicianId=%d, timestamp=%s}",
                requestId, oldStatus, newStatus, technicianId, timestamp);
    }
}
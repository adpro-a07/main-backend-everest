package id.ac.ui.cs.advprog.everest.modules.technicianReport.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_requests", indexes = {
        @Index(name = "idx_user_requests_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    public UserRequest(UUID userId, String userDescription) {
        this.requestId = UUID.randomUUID();
        this.userId = userId;
        this.userDescription = userDescription;
    }

    @Id
    @Column(name = "request_id", nullable = false, updatable = false)
    private UUID requestId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Size(max = 500)
    @Column(name = "user_description", length = 500)
    private String userDescription;

    @PrePersist
    protected void onCreate() {
        if (requestId == null) {
            requestId = UUID.randomUUID();
        }
    }
}
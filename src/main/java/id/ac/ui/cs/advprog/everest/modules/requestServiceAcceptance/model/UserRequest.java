package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    public UserRequest(UUID user_id, String userDescription) {
        this.request_id = UUID.randomUUID();
        this.userDescription = userDescription;
    }

    @Id
    @Column(name = "request_id", nullable = false, updatable = false)
    private UUID request_id;

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID user_id;

    @Size(max = 500)
    @Column(name = "user_description", length = 500)
    private String userDescription;

    @PrePersist
    protected void onCreate() {
        if (request_id == null) {
            request_id = UUID.randomUUID();
        }
    }
}
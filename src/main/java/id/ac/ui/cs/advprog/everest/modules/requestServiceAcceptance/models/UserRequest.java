package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models;

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
    public UserRequest(String userDescription) {
        this.id = UUID.randomUUID();
        this.userDescription = userDescription;
    }

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Size(max = 500)
    @Column(name = "user_description", length = 500)
    private String userDescription;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
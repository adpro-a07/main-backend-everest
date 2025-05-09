package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models;

import jakarta.persistence.*;
import jakarta.validation.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor // Required for JPA
@AllArgsConstructor
public class UserRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_description")
    private String userDescription;
}
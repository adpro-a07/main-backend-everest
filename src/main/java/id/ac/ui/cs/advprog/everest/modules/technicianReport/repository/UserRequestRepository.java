package id.ac.ui.cs.advprog.everest.modules.technicianReport.repository;

import id.ac.ui.cs.advprog.everest.modules.technicianReport.model.UserRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRequestRepository extends JpaRepository<UserRequest, UUID> {
    Optional<UserRequest> findByUserIdAndUserDescription(@NotBlank UUID userId, @NotBlank @Size(max = 500) String userDescription);

    List<UserRequest> findByUserId(@NotBlank UUID userId);
}
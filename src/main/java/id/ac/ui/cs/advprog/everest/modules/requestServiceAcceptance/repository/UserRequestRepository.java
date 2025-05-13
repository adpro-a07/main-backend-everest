package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model.UserRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRequestRepository extends JpaRepository<UserRequest, UUID> {
    // Changed to match actual field name (no underscore in method name)
    Optional<UserRequest> findByUserIdAndUserDescription(@NotBlank UUID userId, @NotBlank @Size(max = 500) String userDescription);

    // Changed to match actual field name (no underscore in method name)
    List<UserRequest> findByUserId(@NotBlank UUID userId);
}
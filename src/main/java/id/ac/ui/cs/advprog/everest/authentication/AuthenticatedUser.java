package id.ac.ui.cs.advprog.everest.authentication;


import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record AuthenticatedUser(
        UUID id,
        String email,
        String fullName,
        UserRole role,
        String phoneNumber,
        Instant createdAt,
        Instant updatedAt,
        String address,
        String workExperience,
        Integer totalJobsDone,
        Long totalIncome
) {}

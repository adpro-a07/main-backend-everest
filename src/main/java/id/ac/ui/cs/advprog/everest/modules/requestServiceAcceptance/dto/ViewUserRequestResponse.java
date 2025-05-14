package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Setter
@Getter
public class ViewUserRequestResponse {
    private UUID requestId;
    private UUID userId;
    private String userDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
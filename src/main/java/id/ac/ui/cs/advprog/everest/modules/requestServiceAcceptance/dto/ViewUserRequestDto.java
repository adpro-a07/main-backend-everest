package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class ViewUserRequestDto {
    private Long id;
    private String userDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
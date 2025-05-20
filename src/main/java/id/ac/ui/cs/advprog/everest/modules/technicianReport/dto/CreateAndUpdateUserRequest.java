package id.ac.ui.cs.advprog.everest.modules.technicianReport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAndUpdateUserRequest {
    @NotBlank
    @Size(max = 500)
    private String userDescription;
}
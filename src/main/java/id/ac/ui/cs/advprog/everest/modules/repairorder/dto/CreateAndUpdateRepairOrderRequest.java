package id.ac.ui.cs.advprog.everest.modules.repairorder.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateAndUpdateRepairOrderRequest {
    @NotBlank
    @Size(max = 100)
    private String itemName;

    @NotBlank
    @Size(max = 100)
    private String itemCondition;

    @NotBlank
    @Size(max = 500)
    private String issueDescription;

    @NotNull
    @FutureOrPresent
    private LocalDate desiredServiceDate;

    // TODO: Add chosen payment method column
    // TODO: Add optional coupon column
}

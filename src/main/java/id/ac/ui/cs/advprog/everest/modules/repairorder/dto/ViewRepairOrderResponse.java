package id.ac.ui.cs.advprog.everest.modules.repairorder.dto;

import id.ac.ui.cs.advprog.everest.modules.repairorder.model.enums.RepairOrderStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Setter
@Getter
public class ViewRepairOrderResponse {
    private UUID id;
    private UUID customerId;
    private UUID technicianId;
    private RepairOrderStatus status;
    private String itemName;
    private String itemCondition;
    private String issueDescription;
    private LocalDate desiredServiceDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

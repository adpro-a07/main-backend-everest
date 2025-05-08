package id.ac.ui.cs.advprog.everest.modules.repairorder.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.repairorder.dto.CreateRepairOrderRequest;

public interface RepairOrderService {
    GenericResponse<Void> createRepairOrder(CreateRepairOrderRequest createRepairOrderRequest,
                                            AuthenticatedUser customer);
}

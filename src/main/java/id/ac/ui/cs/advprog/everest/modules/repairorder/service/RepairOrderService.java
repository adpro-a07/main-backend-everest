package id.ac.ui.cs.advprog.everest.modules.repairorder.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.repairorder.dto.CreateAndUpdateRepairOrderRequest;
import id.ac.ui.cs.advprog.everest.modules.repairorder.dto.ViewRepairOrderResponse;

import java.util.List;

public interface RepairOrderService {
    GenericResponse<ViewRepairOrderResponse> createRepairOrder(
            CreateAndUpdateRepairOrderRequest createAndUpdateRepairOrderRequest,
            AuthenticatedUser customer
    );

    GenericResponse<List<ViewRepairOrderResponse>> getRepairOrders(AuthenticatedUser customer);

    GenericResponse<ViewRepairOrderResponse> updateRepairOrder(
            String repairOrderId,
            CreateAndUpdateRepairOrderRequest createAndUpdateRepairOrderRequest,
            AuthenticatedUser customer
    );

    GenericResponse<Void> deleteRepairOrder(String repairOrderId, AuthenticatedUser customer);
}

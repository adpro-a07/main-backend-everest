package id.ac.ui.cs.advprog.everest.modules.repairorder.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.common.service.UserServiceGrpcClient;
import id.ac.ui.cs.advprog.everest.modules.repairorder.dto.CreateAndUpdateRepairOrderRequest;
import id.ac.ui.cs.advprog.everest.modules.repairorder.dto.ViewRepairOrderResponse;
import id.ac.ui.cs.advprog.everest.modules.repairorder.exception.DatabaseException;
import id.ac.ui.cs.advprog.everest.modules.repairorder.exception.InvalidRepairOrderStateException;
import id.ac.ui.cs.advprog.everest.modules.repairorder.exception.TechnicianUnavailableException;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.RepairOrder;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.enums.RepairOrderStatus;
import id.ac.ui.cs.advprog.everest.modules.repairorder.repository.RepairOrderRepository;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.GetRandomTechnicianResponse;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserData;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RepairOrderServiceImpl implements RepairOrderService {
    private final UserServiceGrpcClient userServiceGrpcClient;
    private final RepairOrderRepository repairOrderRepository;

    public RepairOrderServiceImpl(UserServiceGrpcClient userServiceGrpcClient, RepairOrderRepository repairOrderRepository) {
        this.userServiceGrpcClient = userServiceGrpcClient;
        this.repairOrderRepository = repairOrderRepository;
    }

    @Override
    public GenericResponse<ViewRepairOrderResponse> createRepairOrder(CreateAndUpdateRepairOrderRequest request,
                                                   AuthenticatedUser customer) {
        if (request == null || customer == null) {
            throw new InvalidRepairOrderStateException("Request or customer cannot be null");
        }

        try {
            GetRandomTechnicianResponse response = userServiceGrpcClient.getRandomTechnician();

            if (!response.hasTechnician()) {
                throw new TechnicianUnavailableException();
            }

            UserData technician = response.getTechnician();

            RepairOrder repairOrder = RepairOrder.builder()
                    .customerId(customer.id())
                    .technicianId(UUID.fromString(technician.getIdentity().getId()))
                    .status(RepairOrderStatus.PENDING_CONFIRMATION)
                    .itemName(request.getItemName())
                    .itemCondition(request.getItemCondition())
                    .issueDescription(request.getIssueDescription())
                    .desiredServiceDate(request.getDesiredServiceDate())
                    .build();

            RepairOrder savedRepairOrder = repairOrderRepository.save(repairOrder);

            ViewRepairOrderResponse responseView = getViewRepairOrderResponse(savedRepairOrder);

            return new GenericResponse<>(true, "Repair order created successfully", responseView);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new InvalidRepairOrderStateException("Invalid technician ID or malformed data", ex);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Failed to save repair order", ex);
        }
    }

    private ViewRepairOrderResponse getViewRepairOrderResponse(RepairOrder repairOrder) {
        return ViewRepairOrderResponse.builder()
                .id(repairOrder.getId())
                .customerId(repairOrder.getCustomerId())
                .technicianId(repairOrder.getTechnicianId())
                .status(repairOrder.getStatus())
                .itemName(repairOrder.getItemName())
                .itemCondition(repairOrder.getItemCondition())
                .issueDescription(repairOrder.getIssueDescription())
                .desiredServiceDate(repairOrder.getDesiredServiceDate())
                .createdAt(repairOrder.getCreatedAt())
                .updatedAt(repairOrder.getUpdatedAt())
                .build();
    }

    @Override
    public GenericResponse<List<ViewRepairOrderResponse>> getRepairOrders(AuthenticatedUser customer) {
        if (customer == null) {
            throw new InvalidRepairOrderStateException("Customer cannot be null");
        }

        try {
            List<RepairOrder> repairOrders = repairOrderRepository.findByCustomerId(customer.id());

            List<ViewRepairOrderResponse> responseList = repairOrders.stream()
                    .map(this::getViewRepairOrderResponse)
                    .toList();

            return new GenericResponse<>(true, "Repair orders retrieved successfully", responseList);

        } catch (DataAccessException ex) {
            throw new DatabaseException("Failed to retrieve repair orders", ex);
        }
    }

    @Override
    public GenericResponse<ViewRepairOrderResponse> updateRepairOrder(
            String repairOrderId,
            CreateAndUpdateRepairOrderRequest createAndUpdateRepairOrderRequest,
            AuthenticatedUser customer
    ) {
        if (repairOrderId == null || createAndUpdateRepairOrderRequest == null || customer == null) {
            throw new InvalidRepairOrderStateException("Repair order ID, request, or customer cannot be null");
        }

        try {
            RepairOrder repairOrder = repairOrderRepository.findById(UUID.fromString(repairOrderId))
                    .orElseThrow(() -> new InvalidRepairOrderStateException("Repair order not found"));

            if (!repairOrder.getCustomerId().equals(customer.id())) {
                throw new InvalidRepairOrderStateException("You are not authorized to update this repair order");
            }

            // Make sure repair order is still pending confirmation
            if (repairOrder.getStatus() != RepairOrderStatus.PENDING_CONFIRMATION) {
                throw new InvalidRepairOrderStateException("Repair order cannot be updated");
            }

            repairOrder.setItemName(createAndUpdateRepairOrderRequest.getItemName());
            repairOrder.setItemCondition(createAndUpdateRepairOrderRequest.getItemCondition());
            repairOrder.setIssueDescription(createAndUpdateRepairOrderRequest.getIssueDescription());
            repairOrder.setDesiredServiceDate(createAndUpdateRepairOrderRequest.getDesiredServiceDate());

            RepairOrder updatedRepairOrder = repairOrderRepository.save(repairOrder);

            ViewRepairOrderResponse responseView = getViewRepairOrderResponse(updatedRepairOrder);

            return new GenericResponse<>(true, "Repair order updated successfully", responseView);

        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new InvalidRepairOrderStateException("Invalid data provided", ex);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Failed to update repair order", ex);
        }
    }

    @Override
    public GenericResponse<Void> deleteRepairOrder(String repairOrderId, AuthenticatedUser customer) {
        if (repairOrderId == null || customer == null) {
            throw new InvalidRepairOrderStateException("Repair order ID or customer cannot be null");
        }

        try {
            RepairOrder repairOrder = repairOrderRepository.findById(UUID.fromString(repairOrderId))
                    .orElseThrow(() -> new InvalidRepairOrderStateException("Repair order not found"));

            if (!repairOrder.getCustomerId().equals(customer.id())) {
                throw new InvalidRepairOrderStateException("You are not authorized to delete this repair order");
            }

            // Make sure repair order is still pending confirmation
            if (repairOrder.getStatus() != RepairOrderStatus.PENDING_CONFIRMATION) {
                throw new InvalidRepairOrderStateException("Repair order cannot be deleted");
            }

            repairOrderRepository.delete(repairOrder);

            return new GenericResponse<>(true, "Repair order deleted successfully", null);

        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new InvalidRepairOrderStateException("Invalid data provided", ex);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Failed to delete repair order", ex);
        }
    }
}

package id.ac.ui.cs.advprog.everest.modules.repairorder.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.common.service.UserServiceGrpcClient;
import id.ac.ui.cs.advprog.everest.modules.repairorder.dto.CreateRepairOrderRequest;
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
    public GenericResponse<Void> createRepairOrder(CreateRepairOrderRequest request,
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

            repairOrderRepository.save(repairOrder);

            return new GenericResponse<>(true, "Repair order created successfully", null);

        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new InvalidRepairOrderStateException("Invalid technician ID or malformed data", ex);
        } catch (DataAccessException ex) {
            throw new DatabaseException("Failed to save repair order", ex);
        }
    }
}

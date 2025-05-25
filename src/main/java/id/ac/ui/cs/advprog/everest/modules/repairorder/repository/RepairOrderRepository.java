package id.ac.ui.cs.advprog.everest.modules.repairorder.repository;

import id.ac.ui.cs.advprog.everest.modules.repairorder.model.RepairOrder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepairOrderRepository extends JpaRepository<RepairOrder, Long> {
    Optional<RepairOrder> findById(@NotBlank @Size(max=100) UUID id);

    List<RepairOrder> findByCustomerId(@NotBlank @Size(max=100) UUID customerId);

    List<RepairOrder> findByTechnicianId(@NotBlank @Size(max=100) UUID technicianId);
}

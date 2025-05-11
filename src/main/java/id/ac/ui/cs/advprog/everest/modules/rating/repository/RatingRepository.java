package id.ac.ui.cs.advprog.everest.modules.rating.repository;

import id.ac.ui.cs.advprog.everest.modules.rating.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {
    List<Rating> findAllByTechnicianId(UUID technicianId);
    List<Rating> findAllByUserId(UUID userId);
    boolean existsByUserIdAndRepairOrderId(UUID userId, UUID repairOrderId);
}

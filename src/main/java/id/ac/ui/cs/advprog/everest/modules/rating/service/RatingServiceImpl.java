package id.ac.ui.cs.advprog.everest.modules.rating.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.modules.rating.dto.CreateAndUpdateRatingRequest;
import id.ac.ui.cs.advprog.everest.modules.rating.model.Rating;
import id.ac.ui.cs.advprog.everest.modules.rating.repository.RatingRepository;
import id.ac.ui.cs.advprog.everest.modules.rating.strategy.AdminDeleteStrategy;
import id.ac.ui.cs.advprog.everest.modules.rating.strategy.RatingDeleteStrategy;
import id.ac.ui.cs.advprog.everest.modules.rating.strategy.UserDeleteStrategy;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.RepairOrder;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.enums.RepairOrderStatus;
import id.ac.ui.cs.advprog.everest.modules.repairorder.repository.RepairOrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RatingServiceImpl implements RatingService {

    private final RepairOrderRepository repairOrderRepository;
    private final RatingRepository ratingRepository;

    public RatingServiceImpl(RatingRepository ratingRepository, RepairOrderRepository repairOrderRepository) {
        this.ratingRepository = ratingRepository;
        this.repairOrderRepository = repairOrderRepository;
    }

    @Override
    public Rating createRating(AuthenticatedUser customer, UUID repairOrderId, CreateAndUpdateRatingRequest dto) {
        RepairOrder repairOrder = repairOrderRepository.findById(repairOrderId)
                .orElseThrow(() -> new RuntimeException("Repair order tidak ditemukan"));

        if (ratingRepository.existsByUserIdAndRepairOrderId(customer.id(), repairOrderId)) {
            throw new RuntimeException("Kamu sudah memberi rating untuk order ini.");
        }

        if (!repairOrder.getCustomerId().equals(customer.id())) {
            throw new RuntimeException("Kamu tidak memiliki akses ke order ini.");
        }

        if (!repairOrder.getStatus().equals(RepairOrderStatus.COMPLETED)) {
            throw new RuntimeException("Order belum selesai, tidak bisa memberi rating.");
        }

        Rating rating = Rating.builder()
                .userId(customer.id())
                .technicianId(repairOrder.getTechnicianId())
                .repairOrderId(repairOrderId)
                .comment(dto.getComment())
                .score(dto.getScore())
                .build();

        return ratingRepository.save(rating);
    }

    @Override
    public List<Rating> getRatingsByTechnician(UUID technicianId) {
        return ratingRepository.findAllByTechnicianId(technicianId).stream()
                .filter(r -> !r.isDeleted())
                .toList();
    }

    @Override
    public List<Rating> getRatingsByUser(AuthenticatedUser customer) {
        return ratingRepository.findAllByUserId(customer.id()).stream()
                .filter(r -> !r.isDeleted())
                .toList();
    }

    @Override
    public Rating updateRating(UUID ratingId, AuthenticatedUser customer, CreateAndUpdateRatingRequest dto) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Rating tidak ditemukan"));

        if (!rating.getUserId().equals(customer.id())) {
            throw new RuntimeException("Kamu tidak memiliki izin untuk mengubah rating ini.");
        }

        rating.update(dto.getComment(), dto.getScore());
        return ratingRepository.save(rating);
    }

    @Override
    public void deleteRating(UUID ratingId, AuthenticatedUser customer, boolean isAdmin) {
        RatingDeleteStrategy strategy = isAdmin
                ? new AdminDeleteStrategy(ratingRepository)
                : new UserDeleteStrategy(ratingRepository);

        strategy.delete(ratingId, customer.id());
    }
}

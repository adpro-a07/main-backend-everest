package id.ac.ui.cs.advprog.everest.modules.rating.service;

import id.ac.ui.cs.advprog.everest.modules.rating.dto.CreateAndUpdateRatingRequest;
import id.ac.ui.cs.advprog.everest.modules.rating.model.Rating;
import id.ac.ui.cs.advprog.everest.modules.rating.repository.RatingRepository;
import id.ac.ui.cs.advprog.everest.modules.rating.strategy.AdminDeleteStrategy;
import id.ac.ui.cs.advprog.everest.modules.rating.strategy.RatingDeleteStrategy;
import id.ac.ui.cs.advprog.everest.modules.rating.strategy.UserDeleteStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;

    public RatingServiceImpl(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    @Override
    public Rating createRating(String userId, String technicianId, CreateAndUpdateRatingRequest dto) {
        Rating rating = Rating.builder()
                .userId(userId)
                .technicianId(technicianId)
                .comment(dto.getComment())
                .rating(dto.getRating())
                .build();

        return ratingRepository.save(rating);
    }

    @Override
    public List<Rating> getRatingsByTechnician(String technicianId) {
        return ratingRepository.findAllByTechnicianId(technicianId).stream()
                .filter(r -> !r.isDeleted())
                .toList();
    }

    @Override
    public List<Rating> getRatingsByUser(String userId) {
        return ratingRepository.findAllByUserId(userId).stream()
                .filter(r -> !r.isDeleted())
                .toList();
    }

    @Override
    public Rating updateRating(UUID ratingId, String userId, CreateAndUpdateRatingRequest dto) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Rating tidak ditemukan"));

        if (!rating.getUserId().equals(userId)) {
            throw new RuntimeException("Kamu tidak memiliki izin untuk mengubah rating ini.");
        }

        rating.update(dto.getComment(), dto.getRating());
        return ratingRepository.save(rating);
    }

    @Override
    public void deleteRating(UUID ratingId, String userId, boolean isAdmin) {
        RatingDeleteStrategy strategy = isAdmin
                ? new AdminDeleteStrategy(ratingRepository)
                : new UserDeleteStrategy(ratingRepository);

        strategy.delete(ratingId, userId);
    }
}

package id.ac.ui.cs.advprog.everest.modules.rating.strategy;

import id.ac.ui.cs.advprog.everest.modules.rating.model.Rating;
import id.ac.ui.cs.advprog.everest.modules.rating.repository.RatingRepository;

import java.util.UUID;

public class AdminDeleteStrategy implements RatingDeleteStrategy {

    private final RatingRepository ratingRepository;

    public AdminDeleteStrategy(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    @Override
    public void delete(UUID ratingId, UUID userIdIgnored) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Rating tidak ditemukan"));

        rating.setDeleted(true);
        ratingRepository.save(rating);
    }
}

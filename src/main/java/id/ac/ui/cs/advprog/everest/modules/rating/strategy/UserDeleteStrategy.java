package id.ac.ui.cs.advprog.everest.modules.rating.strategy;

import id.ac.ui.cs.advprog.everest.modules.rating.model.Rating;
import id.ac.ui.cs.advprog.everest.modules.rating.repository.RatingRepository;

import java.util.UUID;

public class UserDeleteStrategy implements RatingDeleteStrategy {

    private final RatingRepository ratingRepository;

    public UserDeleteStrategy(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    @Override
    public void delete(UUID ratingId, UUID userId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Rating tidak ditemukan"));

        if (!rating.getUserId().equals(userId)) {
            throw new RuntimeException("Kamu tidak punya izin untuk menghapus rating ini.");
        }

        rating.setDeleted(true);
        ratingRepository.save(rating);
    }
}

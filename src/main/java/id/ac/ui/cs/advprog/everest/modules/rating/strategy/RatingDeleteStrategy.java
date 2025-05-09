package id.ac.ui.cs.advprog.everest.modules.rating.strategy;

import java.util.UUID;

public interface RatingDeleteStrategy {
    void delete(UUID ratingId, UUID currentUserId);
}

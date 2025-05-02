package id.ac.ui.cs.advprog.everest.repository;

import id.ac.ui.cs.advprog.everest.model.Rating;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

import java.util.*;

@Repository
public class RatingRepositoryImpl implements RatingRepository {
    private final Map<UUID, Rating> ratings = new HashMap<>();

    @Override
    public Rating save(Rating rating) {
        ratings.put(rating.getId(), rating);
        return rating;
    }

    @Override
    public Optional<Rating> findById(UUID id) {
        return Optional.ofNullable(ratings.get(id));
    }

    @Override
    public List<Rating> findAll() {
        return new ArrayList<>(ratings.values());
    }

    @Override
    public void deleteById(UUID id) {
        Rating rating = ratings.get(id);
        if (rating != null) {
            rating.setDeleted(true); // soft delete
            rating.setUpdatedAt(LocalDateTime.now());
        }
    }
}

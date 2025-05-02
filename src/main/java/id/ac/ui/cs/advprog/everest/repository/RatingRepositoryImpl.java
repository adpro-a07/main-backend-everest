package id.ac.ui.cs.advprog.everest.repository;

import id.ac.ui.cs.advprog.everest.model.Rating;
import org.springframework.stereotype.Repository;

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
}

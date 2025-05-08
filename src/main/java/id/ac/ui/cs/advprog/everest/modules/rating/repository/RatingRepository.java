package id.ac.ui.cs.advprog.everest.modules.rating.repository;

import id.ac.ui.cs.advprog.everest.modules.rating.model.Rating;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RatingRepository {
    Rating save(Rating rating);
    Optional<Rating> findById(UUID id);
    List<Rating> findAll();
    void deleteById(UUID id);
}

package id.ac.ui.cs.advprog.everest.modules.rating.service;

import id.ac.ui.cs.advprog.everest.modules.rating.dto.CreateRatingRequest;
import id.ac.ui.cs.advprog.everest.modules.rating.dto.UpdateRatingRequest;
import id.ac.ui.cs.advprog.everest.modules.rating.model.Rating;

import java.util.List;
import java.util.UUID;

public interface RatingService {
    Rating createRating(CreateRatingRequest dto);
    List<Rating> getAllRatings();
    Rating getRatingById(UUID id);
    Rating updateRating(UUID id, UpdateRatingRequest dto);
    void deleteRating(UUID id);
}

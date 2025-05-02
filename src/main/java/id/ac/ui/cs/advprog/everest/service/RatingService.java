package id.ac.ui.cs.advprog.everest.service;

import id.ac.ui.cs.advprog.everest.dto.CreateRatingRequest;
import id.ac.ui.cs.advprog.everest.dto.UpdateRatingRequest;
import id.ac.ui.cs.advprog.everest.model.Rating;

import java.util.List;
import java.util.UUID;

public interface RatingService {
    Rating createRating(CreateRatingRequest dto);
    List<Rating> getAllRatings();
    Rating getRatingById(UUID id);
    Rating updateRating(UUID id, UpdateRatingRequest dto);
    void deleteRating(UUID id);
}

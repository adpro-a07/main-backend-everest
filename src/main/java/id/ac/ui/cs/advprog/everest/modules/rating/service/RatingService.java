package id.ac.ui.cs.advprog.everest.modules.rating.service;

import id.ac.ui.cs.advprog.everest.modules.rating.dto.CreateAndUpdateRatingRequest;
import id.ac.ui.cs.advprog.everest.modules.rating.model.Rating;

import java.util.List;
import java.util.UUID;

public interface RatingService {
    Rating createRating(String userId, String technicianId, CreateAndUpdateRatingRequest dto);
    List<Rating> getRatingsByTechnician(String technicianId);
    List<Rating> getRatingsByUser(String userId);
    Rating updateRating(UUID id, String userId, CreateAndUpdateRatingRequest dto);
    void deleteRating(UUID id, String userId, boolean isAdmin);
}

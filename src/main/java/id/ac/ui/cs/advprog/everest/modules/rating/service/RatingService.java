package id.ac.ui.cs.advprog.everest.modules.rating.service;

import id.ac.ui.cs.advprog.everest.modules.rating.dto.CreateAndUpdateRatingRequest;
import id.ac.ui.cs.advprog.everest.modules.rating.model.Rating;
import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;

import java.util.List;
import java.util.UUID;

public interface RatingService {
    Rating createRating(AuthenticatedUser customer, UUID repairOrderId, CreateAndUpdateRatingRequest dto);
    List<Rating> getRatingsByTechnician(UUID technicianId);
    List<Rating> getRatingsByUser(AuthenticatedUser customer);
    Rating updateRating(UUID ratingId, AuthenticatedUser customer, CreateAndUpdateRatingRequest dto);
    void deleteRating(UUID ratingId, AuthenticatedUser customer, boolean isAdmin);
}

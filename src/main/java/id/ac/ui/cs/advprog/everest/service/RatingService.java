package id.ac.ui.cs.advprog.everest.service;

import id.ac.ui.cs.advprog.everest.dto.CreateRatingRequest;
import id.ac.ui.cs.advprog.everest.model.Rating;

public interface RatingService {
    Rating createRating(CreateRatingRequest dto);
}

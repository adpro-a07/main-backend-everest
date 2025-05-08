package id.ac.ui.cs.advprog.everest.modules.rating.service;

import id.ac.ui.cs.advprog.everest.modules.rating.dto.CreateRatingRequest;
import id.ac.ui.cs.advprog.everest.modules.rating.dto.UpdateRatingRequest;
import id.ac.ui.cs.advprog.everest.modules.rating.model.Rating;
import id.ac.ui.cs.advprog.everest.modules.rating.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;

    @Autowired
    public RatingServiceImpl(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    @Override
    public Rating createRating(CreateRatingRequest dto) {
        Rating rating = Rating.builder()
                .userId(dto.getUserId())
                .technicianId(dto.getTechnicianId())
                .comment(dto.getComment())
                .rating(dto.getRating())
                .build();

        return ratingRepository.save(rating);
    }

    @Override
    public List<Rating> getAllRatings() {
        return ratingRepository.findAll().stream()
                .filter(r -> !r.isDeleted())
                .toList();
    }

    @Override
    public Rating getRatingById(UUID id) {
        return ratingRepository.findById(id)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new RuntimeException("Rating tidak ditemukan"));
    }

    @Override
    public Rating updateRating(UUID id, UpdateRatingRequest dto) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rating tidak ditemukan"));

        rating.update(dto.getComment(), dto.getRating());
        return ratingRepository.save(rating);
    }

    @Override
    public void deleteRating(UUID id) {
        ratingRepository.deleteById(id);
    }
}

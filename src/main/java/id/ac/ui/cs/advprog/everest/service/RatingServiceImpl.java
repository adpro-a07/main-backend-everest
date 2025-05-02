package id.ac.ui.cs.advprog.everest.service;

import id.ac.ui.cs.advprog.everest.dto.CreateRatingRequest;
import id.ac.ui.cs.advprog.everest.model.Rating;
import id.ac.ui.cs.advprog.everest.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}

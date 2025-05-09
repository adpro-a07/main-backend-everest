package id.ac.ui.cs.advprog.everest.modules.rating.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.service.UserServiceGrpcClient;
import id.ac.ui.cs.advprog.everest.modules.rating.dto.CreateAndUpdateRatingRequest;
import id.ac.ui.cs.advprog.everest.modules.rating.model.Rating;
import id.ac.ui.cs.advprog.everest.modules.rating.repository.RatingRepository;
import id.ac.ui.cs.advprog.everest.modules.rating.strategy.AdminDeleteStrategy;
import id.ac.ui.cs.advprog.everest.modules.rating.strategy.RatingDeleteStrategy;
import id.ac.ui.cs.advprog.everest.modules.rating.strategy.UserDeleteStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RatingServiceImpl implements RatingService {

    private final UserServiceGrpcClient userServiceGrpcClient;
    private final RatingRepository ratingRepository;

    public RatingServiceImpl(UserServiceGrpcClient userServiceGrpcClient, RatingRepository ratingRepository) {
        this.userServiceGrpcClient = userServiceGrpcClient;
        this.ratingRepository = ratingRepository;
    }

    @Override
    public Rating createRating(AuthenticatedUser customer, UUID technicianId, CreateAndUpdateRatingRequest dto) {
        Rating rating = Rating.builder()
                .userId(customer.id())
                .technicianId(technicianId)
                .comment(dto.getComment())
                .rating(dto.getRating())
                .build();

        return ratingRepository.save(rating);
    }

    @Override
    public List<Rating> getRatingsByTechnician(UUID technicianId) {
        return ratingRepository.findAllByTechnicianId(technicianId).stream()
                .filter(r -> !r.isDeleted())
                .toList();
    }

    @Override
    public List<Rating> getRatingsByUser(AuthenticatedUser customer) {
        return ratingRepository.findAllByUserId(customer.id()).stream()
                .filter(r -> !r.isDeleted())
                .toList();
    }

    @Override
    public Rating updateRating(UUID ratingId, AuthenticatedUser customer, CreateAndUpdateRatingRequest dto) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Rating tidak ditemukan"));

        if (!rating.getUserId().equals(customer.id())) {
            throw new RuntimeException("Kamu tidak memiliki izin untuk mengubah rating ini.");
        }

        rating.update(dto.getComment(), dto.getRating());
        return ratingRepository.save(rating);
    }

    @Override
    public void deleteRating(UUID ratingId, AuthenticatedUser customer, boolean isAdmin) {
        RatingDeleteStrategy strategy = isAdmin
                ? new AdminDeleteStrategy(ratingRepository)
                : new UserDeleteStrategy(ratingRepository);

        strategy.delete(ratingId, customer.id());
    }
}

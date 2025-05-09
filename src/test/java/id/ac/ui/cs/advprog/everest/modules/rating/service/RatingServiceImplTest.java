package id.ac.ui.cs.advprog.everest.modules.rating.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.common.service.UserServiceGrpcClient;
import id.ac.ui.cs.advprog.everest.modules.rating.dto.CreateAndUpdateRatingRequest;
import id.ac.ui.cs.advprog.everest.modules.rating.model.Rating;
import id.ac.ui.cs.advprog.everest.modules.rating.repository.RatingRepository;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RatingServiceImplTest {

    private RatingRepository ratingRepository;
    private UserServiceGrpcClient userServiceGrpcClient;
    private RatingServiceImpl ratingService;
    private AuthenticatedUser user;
    private UUID userId;
    private UUID technicianId;

    @BeforeEach
    void setUp() {
        ratingRepository = mock(RatingRepository.class);
        ratingService = new RatingServiceImpl(ratingRepository);

        userId = UUID.randomUUID();
        technicianId = UUID.randomUUID();
        user = new AuthenticatedUser(
                userId,
                "fattah@example.com",
                "fattah",
                UserRole.CUSTOMER,
                "0821123123",
                Instant.now(),
                Instant.now(),
                "Depok",
                null,
                0,
                0L
        );
    }

    @Test
    void testCreateRatingSuccess() {
        CreateAndUpdateRatingRequest request = new CreateAndUpdateRatingRequest();
        request.setComment("Mantap");
        request.setRating(4);

        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rating result = ratingService.createRating(user, technicianId, request);

        assertEquals(userId, result.getUserId());
        assertEquals(technicianId, result.getTechnicianId());
        assertEquals("Mantap", result.getComment());
        assertEquals(4, result.getRating());
        assertFalse(result.isDeleted());
    }

    @Test
    void testGetRatingsByUserReturnsCorrectList() {
        Rating rating1 = Rating.builder().userId(userId).technicianId(UUID.randomUUID()).comment("A").rating(5).deleted(false).build();
        Rating rating2 = Rating.builder().userId(userId).technicianId(UUID.randomUUID()).comment("B").rating(4).deleted(false).build();

        when(ratingRepository.findAllByUserId(userId)).thenReturn(List.of(rating1, rating2));

        List<Rating> result = ratingService.getRatingsByUser(user);

        assertEquals(2, result.size());
        assertTrue(result.contains(rating1));
        assertTrue(result.contains(rating2));
    }

    @Test
    void testGetRatingsByTechnicianReturnsCorrectList() {
        Rating rating = Rating.builder().userId(UUID.randomUUID()).technicianId(technicianId).comment("C").rating(3).deleted(false).build();

        when(ratingRepository.findAllByTechnicianId(technicianId)).thenReturn(List.of(rating));

        List<Rating> result = ratingService.getRatingsByTechnician(technicianId);

        assertEquals(1, result.size());
        assertEquals("C", result.get(0).getComment());
    }

    @Test
    void testUpdateRatingSuccess() {
        UUID ratingId = UUID.randomUUID();
        Rating rating = Rating.builder()
                .id(ratingId)
                .userId(userId)
                .technicianId(technicianId)
                .comment("Before")
                .rating(2)
                .deleted(false)
                .build();

        CreateAndUpdateRatingRequest request = new CreateAndUpdateRatingRequest();
        request.setComment("Updated");
        request.setRating(5);

        when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(rating));
        when(ratingRepository.save(any())).thenReturn(rating);

        Rating updated = ratingService.updateRating(ratingId, user, request);

        assertEquals("Updated", updated.getComment());
        assertEquals(5, updated.getRating());
    }

    @Test
    void testUpdateRatingFailsIfUserMismatch() {
        UUID ratingId = UUID.randomUUID();
        Rating rating = Rating.builder()
                .id(ratingId)
                .userId(UUID.randomUUID()) // different user
                .technicianId(technicianId)
                .comment("Old")
                .rating(3)
                .build();

        CreateAndUpdateRatingRequest request = new CreateAndUpdateRatingRequest();
        request.setComment("New");
        request.setRating(4);

        when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(rating));

        assertThrows(RuntimeException.class, () -> ratingService.updateRating(ratingId, user, request));
    }

    @Test
    void testDeleteRatingAsUser() {
        UUID ratingId = UUID.randomUUID();
        Rating rating = Rating.builder()
                .id(ratingId)
                .userId(userId)
                .technicianId(technicianId)
                .comment("Good")
                .rating(4)
                .deleted(false)
                .build();

        when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(rating));
        when(ratingRepository.save(any())).thenReturn(rating);

        ratingService.deleteRating(ratingId, user, false);

        assertTrue(rating.isDeleted());
        verify(ratingRepository).save(argThat(Rating::isDeleted));
    }

    @Test
    void testDeleteRatingAsAdmin() {
        UUID ratingId = UUID.randomUUID();
        Rating rating = Rating.builder()
                .id(ratingId)
                .userId(UUID.randomUUID())
                .technicianId(technicianId)
                .comment("Admin delete")
                .rating(5)
                .deleted(false)
                .build();

        when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(rating));
        when(ratingRepository.save(any())).thenReturn(rating);

        ratingService.deleteRating(ratingId, user, true);

        assertTrue(rating.isDeleted());
        verify(ratingRepository).save(argThat(Rating::isDeleted));
    }
}

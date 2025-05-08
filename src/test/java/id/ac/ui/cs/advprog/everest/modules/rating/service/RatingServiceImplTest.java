package id.ac.ui.cs.advprog.everest.modules.rating.service;

import id.ac.ui.cs.advprog.everest.modules.rating.dto.CreateAndUpdateRatingRequest;
import id.ac.ui.cs.advprog.everest.modules.rating.model.Rating;
import id.ac.ui.cs.advprog.everest.modules.rating.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RatingServiceImplTest {

    private RatingRepository ratingRepository;
    private RatingServiceImpl ratingService;

    @BeforeEach
    void setUp() {
        ratingRepository = mock(RatingRepository.class);
        ratingService = new RatingServiceImpl(ratingRepository);
    }

    @Test
    void testCreateRatingSuccess() {
        CreateAndUpdateRatingRequest request = new CreateAndUpdateRatingRequest();
        request.setComment("Mantap");
        request.setRating(4);

        ArgumentCaptor<Rating> captor = ArgumentCaptor.forClass(Rating.class);
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String userId = "user-01";
        String technicianId = "tech-01";

        Rating result = ratingService.createRating(userId, technicianId, request);

        verify(ratingRepository).save(captor.capture());
        Rating saved = captor.getValue();

        assertEquals(userId, saved.getUserId());
        assertEquals(technicianId, saved.getTechnicianId());
        assertEquals("Mantap", saved.getComment());
        assertEquals(4, saved.getRating());
        assertFalse(saved.isDeleted());
    }

    @Test
    void testGetRatingsByUserReturnsCorrectList() {
        Rating rating1 = Rating.builder().userId("user-01").technicianId("tech-01").comment("A").rating(5).build();
        Rating rating2 = Rating.builder().userId("user-01").technicianId("tech-02").comment("B").rating(4).build();

        when(ratingRepository.findAllByUserId("user-01")).thenReturn(List.of(rating1, rating2));

        List<Rating> result = ratingService.getRatingsByUser("user-01");

        assertEquals(2, result.size());
        assertTrue(result.contains(rating1));
        assertTrue(result.contains(rating2));
    }

    @Test
    void testGetRatingsByTechnicianReturnsCorrectList() {
        Rating rating = Rating.builder().userId("user-02").technicianId("tech-99").comment("C").rating(3).build();

        when(ratingRepository.findAllByTechnicianId("tech-99")).thenReturn(List.of(rating));

        List<Rating> result = ratingService.getRatingsByTechnician("tech-99");

        assertEquals(1, result.size());
        assertEquals("C", result.get(0).getComment());
    }

    @Test
    void testUpdateRatingSuccess() {
        Rating rating = Rating.builder()
                .id(UUID.randomUUID())
                .userId("user-01")
                .technicianId("tech-01")
                .comment("Before")
                .rating(2)
                .build();

        when(ratingRepository.findById(rating.getId())).thenReturn(Optional.of(rating));
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateAndUpdateRatingRequest request = new CreateAndUpdateRatingRequest();
        request.setComment("After");
        request.setRating(5);

        Rating updated = ratingService.updateRating(rating.getId(), "user-01", request);

        assertEquals("After", updated.getComment());
        assertEquals(5, updated.getRating());
    }

    @Test
    void testUpdateRatingFailsIfUserMismatch() {
        Rating rating = Rating.builder()
                .id(UUID.randomUUID())
                .userId("owner-user")
                .technicianId("tech")
                .comment("X")
                .rating(2)
                .build();

        when(ratingRepository.findById(rating.getId())).thenReturn(Optional.of(rating));

        CreateAndUpdateRatingRequest dto = new CreateAndUpdateRatingRequest();
        dto.setComment("Y");
        dto.setRating(4);

        assertThrows(RuntimeException.class, () -> {
            ratingService.updateRating(rating.getId(), "wrong-user", dto);
        });
    }

    @Test
    void testDeleteRatingAsUser() {
        UUID id = UUID.randomUUID();
        Rating rating = Rating.builder()
                .id(id)
                .userId("user-x")
                .technicianId("tech-x")
                .comment("Nice")
                .rating(4)
                .deleted(false)
                .build();

        when(ratingRepository.findById(id)).thenReturn(Optional.of(rating));
        when(ratingRepository.save(any(Rating.class))).thenReturn(rating);

        ratingService.deleteRating(id, "user-x", false);

        verify(ratingRepository).save(argThat(r -> r.isDeleted()));
    }

    @Test
    void testDeleteRatingAsAdmin() {
        UUID id = UUID.randomUUID();
        Rating rating = Rating.builder()
                .id(id)
                .userId("any-user")
                .technicianId("any-tech")
                .comment("OK")
                .rating(5)
                .deleted(false)
                .build();

        when(ratingRepository.findById(id)).thenReturn(Optional.of(rating));
        when(ratingRepository.save(any(Rating.class))).thenReturn(rating);

        ratingService.deleteRating(id, "admin-id", true);

        verify(ratingRepository).save(argThat(r -> r.isDeleted()));
    }
}

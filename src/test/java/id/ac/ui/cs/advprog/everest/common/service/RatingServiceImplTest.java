package id.ac.ui.cs.advprog.everest.service;

import id.ac.ui.cs.advprog.everest.dto.CreateRatingRequest;
import id.ac.ui.cs.advprog.everest.dto.UpdateRatingRequest;
import id.ac.ui.cs.advprog.everest.model.Rating;
import id.ac.ui.cs.advprog.everest.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RatingServiceImplTest {

    private RatingRepository ratingRepository;
    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        ratingRepository = mock(RatingRepository.class);
        ratingService = new RatingServiceImpl(ratingRepository);
    }

    @Test
    void testCreateRatingSuccess() {
        // Arrange
        CreateRatingRequest request = new CreateRatingRequest();
        request.setUserId("user-01");
        request.setTechnicianId("tech-01");
        request.setComment("Mantap");
        request.setRating(4);

        ArgumentCaptor<Rating> captor = ArgumentCaptor.forClass(Rating.class);
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Rating result = ratingService.createRating(request);

        // Assert
        verify(ratingRepository).save(captor.capture());
        Rating saved = captor.getValue();

        assertNotNull(result.getId());
        assertEquals("user-01", saved.getUserId());
        assertEquals("tech-01", saved.getTechnicianId());
        assertEquals("Mantap", saved.getComment());
        assertEquals(4, saved.getRating());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertFalse(saved.isDeleted());
    }

    // Tests for Read
    @Test
    void testGetAllRatingsReturnsCorrectList() {
        Rating rating1 = Rating.builder()
                .userId("user-01")
                .technicianId("tech-01")
                .comment("Bagus")
                .rating(5)
                .build();

        Rating rating2 = Rating.builder()
                .userId("user-02")
                .technicianId("tech-02")
                .comment("Kurang")
                .rating(2)
                .build();

        when(ratingRepository.findAll()).thenReturn(List.of(rating1, rating2));

        var ratings = ratingService.getAllRatings();

        assertEquals(2, ratings.size());
        assertTrue(ratings.contains(rating1));
        assertTrue(ratings.contains(rating2));
    }

    // Test for Update
    @Test
    void testUpdateRatingShouldModifyFields() {
        Rating rating = Rating.builder()
                .id(UUID.randomUUID())
                .userId("user-01")
                .technicianId("tech-01")
                .comment("Biasa aja")
                .rating(3)
                .build();

        when(ratingRepository.findById(rating.getId())).thenReturn(Optional.of(rating));
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateRatingRequest dto = new UpdateRatingRequest();
        dto.setComment("Sekarang bagus");
        dto.setRating(5);

        Rating updated = ratingService.updateRating(rating.getId(), dto);

        assertEquals("Sekarang bagus", updated.getComment());
        assertEquals(5, updated.getRating());
        assertTrue(updated.getUpdatedAt().isAfter(updated.getCreatedAt()));
    }

    // Test for Delete
    @Test
    void testDeleteRatingShouldSetDeletedToTrue() {
        UUID id = UUID.randomUUID();
        Rating rating = Rating.builder()
                .id(id)
                .userId("user-01")
                .technicianId("tech-01")
                .comment("Nice")
                .rating(4)
                .deleted(false)
                .build();

        when(ratingRepository.findById(id)).thenReturn(Optional.of(rating));

        ratingService.deleteRating(id);

        verify(ratingRepository).deleteById(id);
    }
}

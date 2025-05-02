package id.ac.ui.cs.advprog.everest.service;

import id.ac.ui.cs.advprog.everest.dto.CreateRatingRequest;
import id.ac.ui.cs.advprog.everest.model.Rating;
import id.ac.ui.cs.advprog.everest.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
}

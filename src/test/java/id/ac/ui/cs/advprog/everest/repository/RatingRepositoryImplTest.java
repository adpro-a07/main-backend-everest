package id.ac.ui.cs.advprog.everest.repository;

import id.ac.ui.cs.advprog.everest.model.Rating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RatingRepositoryImplTest {

    private RatingRepository ratingRepository;

    @BeforeEach
    void setUp() {
        ratingRepository = new RatingRepositoryImpl();
    }

    @Test
    void testSaveAndFindById() {
        Rating rating = Rating.builder()
                .userId("user-001")
                .technicianId("tech-001")
                .comment("Bagus")
                .rating(5)
                .build();

        ratingRepository.save(rating);
        Optional<Rating> found = ratingRepository.findById(rating.getId());

        assertTrue(found.isPresent());
        assertEquals(rating.getId(), found.get().getId());
        assertEquals("Bagus", found.get().getComment());
    }

    @Test
    void testFindByIdNotFound() {
        Optional<Rating> found = ratingRepository.findById(UUID.randomUUID());
        assertTrue(found.isEmpty());
    }

    @Test
    void testFindAllShouldReturnAllSavedRatings() {
        Rating rating1 = Rating.builder()
                .userId("user-001")
                .technicianId("tech-001")
                .comment("Mantap")
                .rating(5)
                .build();

        Rating rating2 = Rating.builder()
                .userId("user-002")
                .technicianId("tech-002")
                .comment("Kurang oke")
                .rating(3)
                .build();

        ratingRepository.save(rating1);
        ratingRepository.save(rating2);

        List<Rating> ratings = ratingRepository.findAll();
        assertEquals(2, ratings.size());
        assertTrue(ratings.contains(rating1));
        assertTrue(ratings.contains(rating2));
    }
}

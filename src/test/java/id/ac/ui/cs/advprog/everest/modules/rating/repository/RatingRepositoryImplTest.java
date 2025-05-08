package id.ac.ui.cs.advprog.everest.modules.rating.repository;

import id.ac.ui.cs.advprog.everest.modules.rating.model.Rating;
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

    @Test
    void testFindAllWhenNoRatingsExist() {
        List<Rating> ratings = ratingRepository.findAll();
        assertTrue(ratings.isEmpty(), "Rating list should be empty when no data is saved.");
    }

    @Test
    void testSaveAndFindConcurrently() throws InterruptedException {
        Rating rating1 = Rating.builder()
                .userId("user-001")
                .technicianId("tech-001")
                .comment("Great service")
                .rating(5)
                .build();

        Rating rating2 = Rating.builder()
                .userId("user-002")
                .technicianId("tech-002")
                .comment("Average service")
                .rating(3)
                .build();

        // Thread 1: Save rating1
        Thread thread1 = new Thread(() -> ratingRepository.save(rating1));
        // Thread 2: Save rating2
        Thread thread2 = new Thread(() -> ratingRepository.save(rating2));

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        // Ensure both ratings exist
        assertNotNull(ratingRepository.findById(rating1.getId()).orElse(null));
        assertNotNull(ratingRepository.findById(rating2.getId()).orElse(null));
    }
}

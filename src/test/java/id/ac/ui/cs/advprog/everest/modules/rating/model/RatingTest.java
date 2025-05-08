package id.ac.ui.cs.advprog.everest.modules.rating.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RatingTest {
    @Test
    void testBuildRatingWithAllFields() {
        UUID id = UUID.randomUUID();
        String userId = "user-001";
        String technicianId = "tech-001";
        String comment = "Pelayanan sangat memuaskan.";
        int ratingValue = 4;
        LocalDateTime now = LocalDateTime.now();

        Rating rating = Rating.builder()
                .id(id)
                .userId(userId)
                .technicianId(technicianId)
                .comment(comment)
                .rating(ratingValue)
                .createdAt(now)
                .updatedAt(now)
                .deleted(false)
                .build();

        assertEquals(userId, rating.getUserId());
        assertEquals(technicianId, rating.getTechnicianId());
        assertEquals(comment, rating.getComment());
        assertEquals(ratingValue, rating.getRating());
        assertEquals(id, rating.getId());
        assertEquals(now, rating.getCreatedAt());
        assertEquals(now, rating.getUpdatedAt());
        assertFalse(rating.isDeleted());
    }

    @Test
    void testDefaultValuesWhenOptionalFieldsAreNull() {
        Rating rating = Rating.builder()
                .userId("user-002")
                .technicianId("tech-002")
                .comment("Mantap")
                .rating(5)
                .build();

        assertNotNull(rating.getId());
        assertNotNull(rating.getCreatedAt());
        assertNotNull(rating.getUpdatedAt());
        assertFalse(rating.isDeleted());
    }

    @Test
    void testInvalidRatingThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Rating.builder()
                    .userId("user-003")
                    .technicianId("tech-003")
                    .comment("Buruk")
                    .rating(0)
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Rating.builder()
                    .userId("user-003")
                    .technicianId("tech-003")
                    .comment("Buruk")
                    .rating(6)
                    .build();
        });
    }

    @Test
    void testMissingFieldsThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Rating.builder()
                    .technicianId("tech-004")
                    .comment("Lumayan")
                    .rating(3)
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Rating.builder()
                    .userId("user-004")
                    .comment("Lumayan")
                    .rating(3)
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Rating.builder()
                    .userId("user-004")
                    .technicianId("tech-004")
                    .rating(3)
                    .build();
        });
    }

    @Test
    void testUpdateCommentAndRatingShouldChangeFieldsAndUpdateTimestamp() throws InterruptedException {
        Rating rating = Rating.builder()
                .userId("user-005")
                .technicianId("tech-005")
                .comment("Awalnya biasa saja")
                .rating(3)
                .build();

        LocalDateTime beforeUpdate = rating.getUpdatedAt();

        Thread.sleep(1); // 1 ms delay

        // Simulasikan update
        rating.update("Setelah diperbaiki, jadi bagus", 5);

        assertEquals("Setelah diperbaiki, jadi bagus", rating.getComment());
        assertEquals(5, rating.getRating());
        assertTrue(rating.getUpdatedAt().isAfter(beforeUpdate));
    }

    @Test
    void testUpdateWithBlankCommentShouldThrowException() {
        Rating rating = Rating.builder()
                .userId("user-001")
                .technicianId("tech-001")
                .comment("Bagus")
                .rating(4)
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            rating.update("   ", 4); // hanya spasi
        });
    }

    @Test
    void testBuildRatingWithBlankCommentShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Rating.builder()
                    .userId("user-001")
                    .technicianId("tech-001")
                    .comment("   ") // hanya spasi
                    .rating(4)
                    .build();
        });
    }

    @Test
    void testUpdateWithInvalidRatingShouldThrowException() {
        Rating rating = Rating.builder()
                .userId("user-001")
                .technicianId("tech-001")
                .comment("Bagus")
                .rating(4)
                .build();

        assertThrows(IllegalArgumentException.class, () -> rating.update("Masih bagus", 0));
        assertThrows(IllegalArgumentException.class, () -> rating.update("Masih bagus", 6));
    }
}

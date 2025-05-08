package id.ac.ui.cs.advprog.everest.modules.rating.model;

import org.junit.jupiter.api.Test;

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

        Rating rating = Rating.builder()
                .id(id)
                .userId(userId)
                .technicianId(technicianId)
                .comment(comment)
                .rating(ratingValue)
                .deleted(false)
                .build();

        assertEquals(userId, rating.getUserId());
        assertEquals(technicianId, rating.getTechnicianId());
        assertEquals(comment, rating.getComment());
        assertEquals(ratingValue, rating.getRating());
        assertEquals(id, rating.getId());
        assertFalse(rating.isDeleted());
        assertNull(rating.getCreatedAt());
        assertNull(rating.getUpdatedAt());
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
        assertFalse(rating.isDeleted());
        assertNull(rating.getCreatedAt());
        assertNull(rating.getUpdatedAt());
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
    void testUpdateCommentAndRatingShouldChangeFields() {
        Rating rating = Rating.builder()
                .userId("user-005")
                .technicianId("tech-005")
                .comment("Awalnya biasa saja")
                .rating(3)
                .build();

        rating.update("Setelah diperbaiki, jadi bagus", 5);

        assertEquals("Setelah diperbaiki, jadi bagus", rating.getComment());
        assertEquals(5, rating.getRating());
        // updatedAt akan diisi setelah persist dan flush oleh JPA
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
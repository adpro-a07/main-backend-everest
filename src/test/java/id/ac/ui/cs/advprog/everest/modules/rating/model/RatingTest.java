package id.ac.ui.cs.advprog.everest.modules.rating.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RatingTest {

    @Test
    void testBuildRatingWithAllFields() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID technicianId = UUID.randomUUID();
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
        UUID userId = UUID.randomUUID();
        UUID technicianId = UUID.randomUUID();

        Rating rating = Rating.builder()
                .userId(userId)
                .technicianId(technicianId)
                .comment("Mantap")
                .rating(5)
                .build();

        assertNotNull(rating.getId());
        assertEquals(userId, rating.getUserId());
        assertEquals(technicianId, rating.getTechnicianId());
        assertFalse(rating.isDeleted());
        assertNull(rating.getCreatedAt());
        assertNull(rating.getUpdatedAt());
    }

    @Test
    void testInvalidRatingThrowsException() {
        UUID userId = UUID.randomUUID();
        UUID technicianId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> {
            Rating.builder()
                    .userId(userId)
                    .technicianId(technicianId)
                    .comment("Buruk")
                    .rating(0)
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Rating.builder()
                    .userId(userId)
                    .technicianId(technicianId)
                    .comment("Buruk")
                    .rating(6)
                    .build();
        });
    }

    @Test
    void testMissingFieldsThrowsException() {
        UUID userId = UUID.randomUUID();
        UUID technicianId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> {
            Rating.builder()
                    .technicianId(technicianId)
                    .comment("Lumayan")
                    .rating(3)
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Rating.builder()
                    .userId(userId)
                    .comment("Lumayan")
                    .rating(3)
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Rating.builder()
                    .userId(userId)
                    .technicianId(technicianId)
                    .rating(3)
                    .build();
        });
    }

    @Test
    void testUpdateCommentAndRatingShouldChangeFields() {
        UUID userId = UUID.randomUUID();
        UUID technicianId = UUID.randomUUID();

        Rating rating = Rating.builder()
                .userId(userId)
                .technicianId(technicianId)
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
        UUID userId = UUID.randomUUID();
        UUID technicianId = UUID.randomUUID();

        Rating rating = Rating.builder()
                .userId(userId)
                .technicianId(technicianId)
                .comment("Bagus")
                .rating(4)
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            rating.update("   ", 4); // hanya spasi
        });
    }

    @Test
    void testBuildRatingWithBlankCommentShouldThrowException() {
        UUID userId = UUID.randomUUID();
        UUID technicianId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> {
            Rating.builder()
                    .userId(userId)
                    .technicianId(technicianId)
                    .comment("   ") // hanya spasi
                    .rating(4)
                    .build();
        });
    }

    @Test
    void testUpdateWithInvalidRatingShouldThrowException() {
        UUID userId = UUID.randomUUID();
        UUID technicianId = UUID.randomUUID();

        Rating rating = Rating.builder()
                .userId(userId)
                .technicianId(technicianId)
                .comment("Bagus")
                .rating(4)
                .build();

        assertThrows(IllegalArgumentException.class, () -> rating.update("Masih bagus", 0));
        assertThrows(IllegalArgumentException.class, () -> rating.update("Masih bagus", 6));
    }
}
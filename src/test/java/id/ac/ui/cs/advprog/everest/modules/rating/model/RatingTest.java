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
        UUID repairOrderId = UUID.randomUUID();
        String comment = "Pelayanan sangat memuaskan.";
        int score = 4;

        Rating rating = Rating.builder()
                .id(id)
                .userId(userId)
                .technicianId(technicianId)
                .repairOrderId(repairOrderId)
                .comment(comment)
                .score(score)
                .deleted(false)
                .build();

        assertEquals(userId, rating.getUserId());
        assertEquals(technicianId, rating.getTechnicianId());
        assertEquals(repairOrderId, rating.getRepairOrderId());
        assertEquals(comment, rating.getComment());
        assertEquals(score, rating.getScore());
        assertEquals(id, rating.getId());
        assertFalse(rating.isDeleted());
        assertNull(rating.getCreatedAt());
        assertNull(rating.getUpdatedAt());
    }

    @Test
    void testDefaultValuesWhenOptionalFieldsAreNull() {
        UUID userId = UUID.randomUUID();
        UUID technicianId = UUID.randomUUID();
        UUID repairOrderId = UUID.randomUUID();

        Rating rating = Rating.builder()
                .userId(userId)
                .technicianId(technicianId)
                .repairOrderId(repairOrderId)
                .comment("Mantap")
                .score(5)
                .build();

        assertNotNull(rating.getId());
        assertEquals(userId, rating.getUserId());
        assertEquals(technicianId, rating.getTechnicianId());
        assertEquals(repairOrderId, rating.getRepairOrderId());
        assertFalse(rating.isDeleted());
        assertNull(rating.getCreatedAt());
        assertNull(rating.getUpdatedAt());
    }

    @Test
    void testInvalidRatingThrowsException() {
        UUID userId = UUID.randomUUID();
        UUID technicianId = UUID.randomUUID();
        UUID repairOrderId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> Rating.builder()
                .userId(userId)
                .technicianId(technicianId)
                .repairOrderId(repairOrderId)
                .comment("Buruk")
                .score(0)
                .build());

        assertThrows(IllegalArgumentException.class, () -> Rating.builder()
                .userId(userId)
                .technicianId(technicianId)
                .repairOrderId(repairOrderId)
                .comment("Buruk")
                .score(6)
                .build());
    }

    @Test
    void testMissingFieldsThrowsException() {
        UUID userId = UUID.randomUUID();
        UUID technicianId = UUID.randomUUID();
        UUID repairOrderId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> Rating.builder()
                .technicianId(technicianId)
                .repairOrderId(repairOrderId)
                .comment("Lumayan")
                .score(3)
                .build());

        assertThrows(IllegalArgumentException.class, () -> Rating.builder()
                .userId(userId)
                .repairOrderId(repairOrderId)
                .comment("Lumayan")
                .score(3)
                .build());

        assertThrows(IllegalArgumentException.class, () -> Rating.builder()
                .userId(userId)
                .technicianId(technicianId)
                .comment("Lumayan")
                .score(3)
                .build());
    }

    @Test
    void testUpdateCommentAndRatingShouldChangeFields() {
        UUID userId = UUID.randomUUID();
        UUID technicianId = UUID.randomUUID();
        UUID repairOrderId = UUID.randomUUID();

        Rating rating = Rating.builder()
                .userId(userId)
                .technicianId(technicianId)
                .repairOrderId(repairOrderId)
                .comment("Awalnya biasa saja")
                .score(3)
                .build();

        rating.update("Setelah diperbaiki, jadi bagus", 5);

        assertEquals("Setelah diperbaiki, jadi bagus", rating.getComment());
        assertEquals(5, rating.getScore());
    }

    @Test
    void testUpdateWithBlankCommentShouldThrowException() {
        UUID userId = UUID.randomUUID();
        UUID technicianId = UUID.randomUUID();
        UUID repairOrderId = UUID.randomUUID();

        Rating rating = Rating.builder()
                .userId(userId)
                .technicianId(technicianId)
                .repairOrderId(repairOrderId)
                .comment("Bagus")
                .score(4)
                .build();

        assertThrows(IllegalArgumentException.class, () ->
                rating.update("   ", 4));
    }

    @Test
    void testBuildRatingWithBlankCommentShouldThrowException() {
        UUID userId = UUID.randomUUID();
        UUID technicianId = UUID.randomUUID();
        UUID repairOrderId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> Rating.builder()
                .userId(userId)
                .technicianId(technicianId)
                .repairOrderId(repairOrderId)
                .comment("   ")
                .score(4)
                .build());
    }

    @Test
    void testUpdateWithInvalidRatingShouldThrowException() {
        UUID userId = UUID.randomUUID();
        UUID technicianId = UUID.randomUUID();
        UUID repairOrderId = UUID.randomUUID();

        Rating rating = Rating.builder()
                .userId(userId)
                .technicianId(technicianId)
                .repairOrderId(repairOrderId)
                .comment("Bagus")
                .score(4)
                .build();

        assertThrows(IllegalArgumentException.class, () -> rating.update("Masih bagus", 0));
        assertThrows(IllegalArgumentException.class, () -> rating.update("Masih bagus", 6));
    }
}
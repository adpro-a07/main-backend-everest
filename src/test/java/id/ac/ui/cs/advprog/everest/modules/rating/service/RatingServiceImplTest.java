package id.ac.ui.cs.advprog.everest.modules.rating.service;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.modules.rating.dto.CreateAndUpdateRatingRequest;
import id.ac.ui.cs.advprog.everest.modules.rating.model.Rating;
import id.ac.ui.cs.advprog.everest.modules.rating.repository.RatingRepository;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.RepairOrder;
import id.ac.ui.cs.advprog.everest.modules.repairorder.model.enums.RepairOrderStatus;
import id.ac.ui.cs.advprog.everest.modules.repairorder.repository.RepairOrderRepository;
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
    private RepairOrderRepository repairOrderRepository;
    private RatingServiceImpl ratingService;
    private AuthenticatedUser user;
    private UUID userId;
    private UUID technicianId;
    private UUID repairOrderId;

    @BeforeEach
    void setUp() {
        ratingRepository = mock(RatingRepository.class);
        repairOrderRepository = mock(RepairOrderRepository.class);
        ratingService = new RatingServiceImpl(ratingRepository, repairOrderRepository);

        userId = UUID.randomUUID();
        technicianId = UUID.randomUUID();
        repairOrderId = UUID.randomUUID();
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
    void testCreateRatingFromCompletedRepairOrder_Success() {
        CreateAndUpdateRatingRequest request = new CreateAndUpdateRatingRequest();
        request.setComment("Bagus banget");
        request.setScore(5);

        RepairOrder repairOrder = RepairOrder.builder()
                .id(repairOrderId)
                .customerId(userId)
                .technicianId(technicianId)
                .status(RepairOrderStatus.COMPLETED)
                .build();

        when(repairOrderRepository.findById(repairOrderId)).thenReturn(Optional.of(repairOrder));
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rating result = ratingService.createRating(user, repairOrderId, request);

        assertEquals(userId, result.getUserId());
        assertEquals(technicianId, result.getTechnicianId());
        assertEquals("Bagus banget", result.getComment());
        assertEquals(5, result.getScore());
    }

    @Test
    void testCreateRating_FailsIfOrderNotCompleted() {
        RepairOrder repairOrder = RepairOrder.builder()
                .id(repairOrderId)
                .customerId(userId)
                .technicianId(technicianId)
                .status(RepairOrderStatus.IN_PROGRESS)
                .build();

        when(repairOrderRepository.findById(repairOrderId)).thenReturn(Optional.of(repairOrder));

        CreateAndUpdateRatingRequest request = new CreateAndUpdateRatingRequest();
        request.setComment("Invalid case");
        request.setScore(3);

        assertThrows(RuntimeException.class, () ->
                ratingService.createRating(user, repairOrderId, request)
        );
    }

    @Test
    void testGetRatingsByUserReturnsCorrectList() {
        Rating rating1 = Rating.builder()
                .userId(userId)
                .technicianId(UUID.randomUUID())
                .repairOrderId(UUID.randomUUID())
                .comment("A")
                .score(5)
                .deleted(false)
                .build();
        Rating rating2 = Rating.builder()
                .userId(userId)
                .technicianId(UUID.randomUUID())
                .repairOrderId(UUID.randomUUID())
                .comment("B")
                .score(4)
                .deleted(false)
                .build();

        when(ratingRepository.findAllByUserId(userId)).thenReturn(List.of(rating1, rating2));

        List<Rating> result = ratingService.getRatingsByUser(user);

        assertEquals(2, result.size());
        assertTrue(result.contains(rating1));
        assertTrue(result.contains(rating2));
    }

    @Test
    void testGetRatingsByTechnicianReturnsCorrectList() {
        Rating rating = Rating.builder()
                .userId(UUID.randomUUID())
                .technicianId(technicianId)
                .repairOrderId(UUID.randomUUID())
                .comment("C")
                .score(3)
                .deleted(false)
                .build();

        when(ratingRepository.findAllByTechnicianId(technicianId)).thenReturn(List.of(rating));

        List<Rating> result = ratingService.getRatingsByTechnician(technicianId);

        assertEquals(1, result.size());
        assertEquals("C", result.getFirst().getComment());
    }

    @Test
    void testUpdateRatingSuccess() {
        UUID ratingId = UUID.randomUUID();
        Rating rating = Rating.builder()
                .id(ratingId)
                .userId(userId)
                .technicianId(technicianId)
                .repairOrderId(repairOrderId)
                .comment("Before")
                .score(2)
                .deleted(false)
                .build();

        CreateAndUpdateRatingRequest request = new CreateAndUpdateRatingRequest();
        request.setComment("Updated");
        request.setScore(5);

        when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(rating));
        when(ratingRepository.save(any())).thenReturn(rating);

        Rating updated = ratingService.updateRating(ratingId, user, request);

        assertEquals("Updated", updated.getComment());
        assertEquals(5, updated.getScore());
    }

    @Test
    void testUpdateRatingFailsIfUserMismatch() {
        UUID ratingId = UUID.randomUUID();
        Rating rating = Rating.builder()
                .id(ratingId)
                .userId(UUID.randomUUID())
                .technicianId(technicianId)
                .repairOrderId(repairOrderId)
                .comment("Old")
                .score(3)
                .build();

        CreateAndUpdateRatingRequest request = new CreateAndUpdateRatingRequest();
        request.setComment("New");
        request.setScore(4);

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
                .repairOrderId(repairOrderId)
                .comment("Good")
                .score(4)
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
                .repairOrderId(repairOrderId)
                .comment("Admin delete")
                .score(5)
                .deleted(false)
                .build();

        when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(rating));
        when(ratingRepository.save(any())).thenReturn(rating);

        ratingService.deleteRating(ratingId, user, true);

        assertTrue(rating.isDeleted());
        verify(ratingRepository).save(argThat(Rating::isDeleted));
    }

    @Test
    void testCreateRatingFailsIfAlreadyRated() {
        UUID repairOrderId = UUID.randomUUID();

        when(ratingRepository.existsByUserIdAndRepairOrderId(user.id(), repairOrderId))
                .thenReturn(true);

        CreateAndUpdateRatingRequest dto = new CreateAndUpdateRatingRequest();
        dto.setComment("Double rating test");
        dto.setScore(4);

        assertThrows(RuntimeException.class, () ->
                ratingService.createRating(user, repairOrderId, dto));

        verify(ratingRepository, never()).save(any());
    }
}

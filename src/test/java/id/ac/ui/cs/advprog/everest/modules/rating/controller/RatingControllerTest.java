package id.ac.ui.cs.advprog.everest.modules.rating.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.modules.rating.dto.CreateAndUpdateRatingRequest;
import id.ac.ui.cs.advprog.everest.modules.rating.model.Rating;
import id.ac.ui.cs.advprog.everest.modules.rating.service.RatingService;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RatingControllerTest {

    private MockMvc mockMvc;
    private RatingService ratingService;
    private ObjectMapper objectMapper;

    @InjectMocks
    private RatingController ratingController;

    private CreateAndUpdateRatingRequest request;
    private Rating expectedRating;

    private UUID ratingId;
    private UUID userId;
    private UUID technicianId;
    private UUID repairOrderId;

    @BeforeEach
    void setUp() {
        ratingService = Mockito.mock(RatingService.class);
        ratingController = new RatingController(ratingService);
        mockMvc = MockMvcBuilders.standaloneSetup(ratingController).build();
        objectMapper = new ObjectMapper();

        ratingId = UUID.randomUUID();
        userId = UUID.randomUUID();
        technicianId = UUID.randomUUID();
        repairOrderId = UUID.randomUUID();

        AuthenticatedUser customer = new AuthenticatedUser(
                userId,
                "sisi@example.com",
                "sisi",
                UserRole.CUSTOMER,
                "0821123123",
                Instant.now(),
                Instant.now(),
                "Depok",
                null,
                0,
                0L
        );

        request = new CreateAndUpdateRatingRequest();
        request.setComment("Great service!");
        request.setScore(5);

        expectedRating = Rating.builder()
                .id(ratingId)
                .userId(userId)
                .technicianId(technicianId)
                .repairOrderId(repairOrderId)
                .comment("Great service!")
                .score(5)
                .deleted(false)
                .build();
    }

    @Test
    void testCreateRating_Success() throws Exception {
        UUID repairOrderId = UUID.randomUUID();

        Mockito.when(ratingService.createRating(any(AuthenticatedUser.class), eq(repairOrderId), any()))
                .thenReturn(expectedRating);

        mockMvc.perform(post("/api/v1/rating/ratings")
                        .param("repairOrderId", repairOrderId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.technicianId").value(technicianId.toString()))
                .andExpect(jsonPath("$.comment").value("Great service!"))
                .andExpect(jsonPath("$.score").value(5));

        Mockito.verify(ratingService).createRating(any(AuthenticatedUser.class), eq(repairOrderId), any());
    }

    @Test
    void testUpdateRating_Success() throws Exception {
        Rating updated = Rating.builder()
                .id(ratingId)
                .userId(userId)
                .technicianId(technicianId)
                .repairOrderId(repairOrderId)
                .comment("Updated comment")
                .score(4)
                .deleted(false)
                .build();

        CreateAndUpdateRatingRequest updateRequest = new CreateAndUpdateRatingRequest();
        updateRequest.setComment("Updated comment");
        updateRequest.setScore(4);

        Mockito.when(ratingService.updateRating(eq(ratingId), any(AuthenticatedUser.class), any()))
                .thenReturn(updated);

        mockMvc.perform(put("/api/v1/rating/ratings/{id}", ratingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        Mockito.verify(ratingService).updateRating(eq(ratingId), any(AuthenticatedUser.class), any());
    }

    @Test
    void testGetRatingsByTechnician_ReturnsList() throws Exception {
        List<Rating> ratings = List.of(expectedRating);
        Mockito.when(ratingService.getRatingsByTechnician(eq(technicianId))).thenReturn(ratings);

        mockMvc.perform(get("/api/v1/rating/technicians/{technicianId}/ratings", technicianId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].technicianId").value(technicianId.toString()))
                .andExpect(jsonPath("$[0].comment").value("Great service!"));
    }

    @Test
    void testGetRatingsByUser_ReturnsList() throws Exception {
        List<Rating> ratings = List.of(expectedRating);
        Mockito.when(ratingService.getRatingsByUser(any(AuthenticatedUser.class))).thenReturn(ratings);

        mockMvc.perform(get("/api/v1/rating/users/me/ratings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].technicianId").value(technicianId.toString()))
                .andExpect(jsonPath("$[0].comment").value("Great service!"));
    }

    @Test
    void testDeleteRating_Success() throws Exception {
        Mockito.doNothing().when(ratingService).deleteRating(eq(ratingId), any(AuthenticatedUser.class), eq(false));

        mockMvc.perform(delete("/api/v1/rating/ratings/{id}", ratingId)
                        .param("admin", "false"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteRatingAsAdmin_Success() throws Exception {
        Mockito.doNothing().when(ratingService).deleteRating(eq(ratingId), any(AuthenticatedUser.class), eq(true));

        mockMvc.perform(delete("/api/v1/rating/ratings/{id}", ratingId)
                        .param("admin", "true"))
                .andExpect(status().isNoContent());
    }
}

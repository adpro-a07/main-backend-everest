package id.ac.ui.cs.advprog.everest.modules.rating.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.everest.modules.rating.dto.CreateAndUpdateRatingRequest;
import id.ac.ui.cs.advprog.everest.modules.rating.model.Rating;
import id.ac.ui.cs.advprog.everest.modules.rating.service.RatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
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
    private final UUID ratingId = UUID.randomUUID();
    private final String userId = "user-1";
    private final String technicianId = "tech-1";

    // RequestPostProcessor untuk autentikasi
    private RequestPostProcessor userAuth() {
        return request -> {
            request.addHeader("X-User-ID", userId);
            return request;
        };
    }

    @BeforeEach
    void setUp() {
        ratingService = Mockito.mock(RatingService.class);
        ratingController = new RatingController(ratingService);
        mockMvc = MockMvcBuilders.standaloneSetup(ratingController)
                .alwaysDo(result -> {
                    System.out.println(result.getResponse().getContentAsString());
                })
                .build();
        objectMapper = new ObjectMapper();

        request = new CreateAndUpdateRatingRequest();
        request.setComment("Great service!");
        request.setRating(5);

        expectedRating = Rating.builder()
                .id(ratingId)
                .userId(userId)
                .technicianId(technicianId)
                .comment("Great service!")
                .rating(5)
                .deleted(false)
                .build();
    }

    @Test
    void testCreateRating_Success() throws Exception {
        // Setup mock
        Mockito.when(ratingService.createRating(any(), any(), any())).thenReturn(expectedRating);

        // Panggil API dan verifikasi
        mockMvc.perform(post("/api/v1/rating/ratings")
                        .param("technicianId", technicianId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(userAuth()))
                .andExpect(status().isOk());

        // Verifikasi service dipanggil
        Mockito.verify(ratingService).createRating(any(), eq(technicianId), any());
    }

    @Test
    void testUpdateRating_Success() throws Exception {
        // Setup rating yang diupdate
        Rating updatedRating = Rating.builder()
                .id(ratingId)
                .userId(userId)
                .technicianId(technicianId)
                .comment("Service improved!")
                .rating(4)
                .deleted(false)
                .build();

        CreateAndUpdateRatingRequest updateRequest = new CreateAndUpdateRatingRequest();
        updateRequest.setComment("Service improved!");
        updateRequest.setRating(4);

        // Setup mock
        Mockito.when(ratingService.updateRating(any(), any(), any())).thenReturn(updatedRating);

        // Panggil API dan verifikasi
        mockMvc.perform(put("/api/v1/rating/ratings/{id}", ratingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .with(userAuth()))
                .andExpect(status().isOk());

        // Verifikasi service dipanggil
        Mockito.verify(ratingService).updateRating(eq(ratingId), any(), any());
    }

    @Test
    void testGetRatingsByTechnician_ReturnsList() throws Exception {
        List<Rating> ratings = Arrays.asList(expectedRating);

        Mockito.when(ratingService.getRatingsByTechnician(technicianId))
                .thenReturn(ratings);

        mockMvc.perform(get("/api/v1/rating/technicians/{technicianId}/ratings", technicianId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(userId))
                .andExpect(jsonPath("$[0].technicianId").value(technicianId))
                .andExpect(jsonPath("$[0].comment").value("Great service!"));
    }

    @Test
    void testGetRatingsByUser_ReturnsList() throws Exception {
        List<Rating> ratings = Arrays.asList(expectedRating);

        Mockito.when(ratingService.getRatingsByUser(userId))
                .thenReturn(ratings);

        mockMvc.perform(get("/api/v1/rating/users/{userId}/ratings", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(userId))
                .andExpect(jsonPath("$[0].technicianId").value(technicianId))
                .andExpect(jsonPath("$[0].comment").value("Great service!"));
    }

    @Test
    void testDeleteRating_Success() throws Exception {
        Mockito.doNothing().when(ratingService).deleteRating(ratingId, userId, false);

        mockMvc.perform(
                        delete("/api/v1/rating/ratings/{id}", ratingId)
                                .principal(() -> userId)
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteRatingAsAdmin_Success() throws Exception {
        Mockito.doNothing().when(ratingService).deleteRating(ratingId, userId, true);

        mockMvc.perform(
                        delete("/api/v1/rating/ratings/{id}", ratingId)
                                .param("admin", "true")
                                .principal(() -> userId)
                )
                .andExpect(status().isNoContent());
    }
}
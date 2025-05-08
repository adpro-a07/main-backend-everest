package id.ac.ui.cs.advprog.everest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.everest.dto.CreateRatingRequest;
import id.ac.ui.cs.advprog.everest.dto.UpdateRatingRequest;
import id.ac.ui.cs.advprog.everest.model.Rating;
import id.ac.ui.cs.advprog.everest.common.service.RatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RatingControllerTest {

    private MockMvc mockMvc;
    private RatingService ratingService;
    private ObjectMapper objectMapper;

    @InjectMocks
    private RatingController ratingController;

    private CreateRatingRequest request;
    private Rating expectedRating;

    @BeforeEach
    void setUp() {
        ratingService = Mockito.mock(RatingService.class);
        ratingController = new RatingController(ratingService);
        mockMvc = MockMvcBuilders.standaloneSetup(ratingController).build();
        objectMapper = new ObjectMapper();

        request = new CreateRatingRequest();
        request.setUserId("user-1");
        request.setTechnicianId("tech-1");
        request.setComment("Great service!");
        request.setRating(5);

        expectedRating = Rating.builder()
                .id(UUID.randomUUID())
                .userId("user-1")
                .technicianId("tech-1")
                .comment("Great service!")
                .rating(5)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(false)
                .build();
    }

    @Test
    void testCreateRating_ReturnsCreatedRating() throws Exception {
        Mockito.when(ratingService.createRating(Mockito.any(CreateRatingRequest.class)))
                .thenReturn(expectedRating);

        mockMvc.perform(post("/api/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-1"))
                .andExpect(jsonPath("$.technicianId").value("tech-1"))
                .andExpect(jsonPath("$.comment").value("Great service!"))
                .andExpect(jsonPath("$.rating").value(5));
    }

    // Test for Update
    @Test
    void testUpdateRating_ReturnsUpdatedRating() throws Exception {
        UUID ratingId = expectedRating.getId();

        // DTO Update
        var updateRequest = new UpdateRatingRequest();
        updateRequest.setComment("Service improved!");
        updateRequest.setRating(4);

        Rating updatedRating = Rating.builder()
                .id(ratingId)
                .userId("user-1")
                .technicianId("tech-1")
                .comment("Service improved!")
                .rating(4)
                .createdAt(expectedRating.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .deleted(false)
                .build();

        Mockito.when(ratingService.updateRating(Mockito.eq(ratingId), Mockito.any(UpdateRatingRequest.class)))
                .thenReturn(updatedRating);

        mockMvc.perform(
                        put("/api/ratings/{id}", ratingId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("Service improved!"))
                .andExpect(jsonPath("$.rating").value(4));
    }

    // Test for Delete
    @Test
    void testDeleteRating_Success() throws Exception {
        UUID ratingId = expectedRating.getId();

        Mockito.doNothing().when(ratingService).deleteRating(ratingId);

        mockMvc.perform(
                        delete("/api/ratings/{id}", ratingId)
                )
                .andExpect(status().isNoContent());
    }
}

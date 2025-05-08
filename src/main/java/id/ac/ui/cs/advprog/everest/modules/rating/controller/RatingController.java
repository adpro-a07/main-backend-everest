package id.ac.ui.cs.advprog.everest.modules.rating.controller;

import id.ac.ui.cs.advprog.everest.modules.rating.dto.CreateAndUpdateRatingRequest;
import id.ac.ui.cs.advprog.everest.modules.rating.model.Rating;
import id.ac.ui.cs.advprog.everest.modules.rating.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rating")
public class RatingController {

    private final RatingService ratingService;

    @Autowired
    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    // POST /ratings
    @PostMapping("/ratings")
    public ResponseEntity<Rating> createRating(

            // TODO Sementara
            @AuthenticationPrincipal String userId,
            @RequestParam("technicianId") String technicianId,
            @RequestBody @Valid CreateAndUpdateRatingRequest dto) {
        Rating createdRating = ratingService.createRating(userId, technicianId, dto);
        return ResponseEntity.ok(createdRating);
    }

    // GET /technicians/{technicianId}/ratings
    @GetMapping("/technicians/{technicianId}/ratings")
    public ResponseEntity<List<Rating>> getRatingsByTechnician(@PathVariable String technicianId) {
        return ResponseEntity.ok(ratingService.getRatingsByTechnician(technicianId));
    }

    // GET /users/<id>/ratings
    @GetMapping("/users/{userId}/ratings")
    public ResponseEntity<List<Rating>> getRatingsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(ratingService.getRatingsByUser(userId));
    }

    // PUT /ratings/<id>
    @PutMapping("/ratings/{id}")
    public ResponseEntity<Rating> updateRating(
            @PathVariable UUID id,
            @AuthenticationPrincipal String userId,
            @RequestBody @Valid CreateAndUpdateRatingRequest dto) {
        return ResponseEntity.ok(ratingService.updateRating(id, userId, dto));
    }

    // DELETE /ratings/<id>
    @DeleteMapping("/ratings/{id}")
    public ResponseEntity<Void> deleteRating(
            @PathVariable UUID id,
            @AuthenticationPrincipal String userId,
            @RequestParam(name = "admin", defaultValue = "false") boolean isAdmin) {
        ratingService.deleteRating(id, userId, isAdmin);
        return ResponseEntity.noContent().build();
    }
}

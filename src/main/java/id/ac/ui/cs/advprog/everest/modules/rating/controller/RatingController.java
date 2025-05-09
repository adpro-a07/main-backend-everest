package id.ac.ui.cs.advprog.everest.modules.rating.controller;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.authentication.CurrentUser;
import id.ac.ui.cs.advprog.everest.modules.rating.dto.CreateAndUpdateRatingRequest;
import id.ac.ui.cs.advprog.everest.modules.rating.model.Rating;
import id.ac.ui.cs.advprog.everest.modules.rating.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/ratings")
    public ResponseEntity<Rating> createRating(
            @CurrentUser AuthenticatedUser customer,
            @RequestParam("repairOrderId") UUID repairOrderId,
            @RequestBody @Valid CreateAndUpdateRatingRequest dto) {
        Rating createdRating = ratingService.createRating(customer, repairOrderId, dto);
        return ResponseEntity.ok(createdRating);
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN')")
    @GetMapping("/technicians/{technicianId}/ratings")
    public ResponseEntity<List<Rating>> getRatingsByTechnician(@PathVariable UUID technicianId) {
        return ResponseEntity.ok(ratingService.getRatingsByTechnician(technicianId));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/users/me/ratings")
    public ResponseEntity<List<Rating>> getRatingsByUser(@CurrentUser AuthenticatedUser customer) {
        return ResponseEntity.ok(ratingService.getRatingsByUser(customer));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/ratings/{id}")
    public ResponseEntity<Rating> updateRating(
            @PathVariable UUID id,
            @CurrentUser AuthenticatedUser customer,
            @RequestBody @Valid CreateAndUpdateRatingRequest dto) {
        return ResponseEntity.ok(ratingService.updateRating(id, customer, dto));
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @DeleteMapping("/ratings/{id}")
    public ResponseEntity<Void> deleteRating(
            @PathVariable UUID id,
            @CurrentUser AuthenticatedUser customer,
            @RequestParam(name = "admin", defaultValue = "false") boolean isAdmin) {
        ratingService.deleteRating(id, customer, isAdmin);
        return ResponseEntity.noContent().build();
    }
}

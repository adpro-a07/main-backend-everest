package id.ac.ui.cs.advprog.everest.controller;

import id.ac.ui.cs.advprog.everest.dto.CreateRatingRequest;
import id.ac.ui.cs.advprog.everest.dto.UpdateRatingRequest;
import id.ac.ui.cs.advprog.everest.model.Rating;
import id.ac.ui.cs.advprog.everest.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;

    @Autowired
    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    public ResponseEntity<Rating> createRating(@RequestBody CreateRatingRequest dto) {
        Rating createdRating = ratingService.createRating(dto);
        return ResponseEntity.ok(createdRating);
    }

    @GetMapping
    public ResponseEntity<List<Rating>> getAllRatings() {
        return ResponseEntity.ok(ratingService.getAllRatings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rating> getRatingById(@PathVariable UUID id) {
        return ResponseEntity.ok(ratingService.getRatingById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rating> updateRating(@PathVariable UUID id, @RequestBody UpdateRatingRequest dto) {
        return ResponseEntity.ok(ratingService.updateRating(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable UUID id) {
        ratingService.deleteRating(id);
        return ResponseEntity.noContent().build();
    }
}

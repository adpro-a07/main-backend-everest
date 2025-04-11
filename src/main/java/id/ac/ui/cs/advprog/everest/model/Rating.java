package id.ac.ui.cs.advprog.everest.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
public class Rating {
    UUID id;
    String userId;
    String technicianId;
    String comment;
    int rating;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    boolean deleted;

    @Builder
    public Rating(UUID id,
                  String userId,
                  String technicianId,
                  String comment,
                  int rating,
                  LocalDateTime createdAt,
                  LocalDateTime updatedAt,
                  Boolean deleted) {

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating harus antara 1 dan 5.");
        }
        if (userId == null || technicianId == null || comment == null) {
            throw new IllegalArgumentException("Semua field wajib diisi.");
        }

        this.id = (id == null) ? UUID.randomUUID() : id;
        this.userId = userId;
        this.technicianId = technicianId;
        this.comment = comment;
        this.rating = rating;
        this.createdAt = (createdAt == null) ? LocalDateTime.now() : createdAt;
        this.updatedAt = (updatedAt == null) ? this.createdAt : updatedAt;
        this.deleted = (deleted == null) ? false : deleted;
    }
}

package id.ac.ui.cs.advprog.everest.modules.rating.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@Entity
@Table(name = "ratings")
@EntityListeners(AuditingEntityListener.class)
public class Rating {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String technicianId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    private int rating;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean deleted;

    public Rating() {
        // default constructor for JPA
    }

    @Builder
    public Rating(UUID id,
                  String userId,
                  String technicianId,
                  String comment,
                  int rating,
                  Boolean deleted) {

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating harus antara 1 dan 5.");
        }
        if (userId == null || technicianId == null || comment == null || comment.isBlank()) {
            throw new IllegalArgumentException("Semua field wajib diisi.");
        }

        this.id = (id == null) ? UUID.randomUUID() : id;
        this.userId = userId;
        this.technicianId = technicianId;
        this.comment = comment;
        this.rating = rating;
        this.deleted = (deleted == null) ? false : deleted;
    }

    public void update(String comment, int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating harus antara 1 dan 5.");
        }
        if (comment == null || comment.isBlank()) {
            throw new IllegalArgumentException("Komentar tidak boleh null.");
        }

        this.comment = comment;
        this.rating = rating;
    }
}

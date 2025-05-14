package id.ac.ui.cs.advprog.everest.modules.rating.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID userId;

    @NotNull
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID technicianId;

    @NotNull
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID repairOrderId;

    @NotBlank
    @Size(max = 1000)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Min(value = 1)
    @Max(value = 5)
    @Column(nullable = false)
    private int score;

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
                  UUID userId,
                  UUID technicianId,
                  UUID repairOrderId,
                  String comment,
                  int score,
                  Boolean deleted) {

        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("Rating harus antara 1 dan 5.");
        }
        if (userId == null || technicianId == null || repairOrderId == null || comment == null || comment.isBlank()) {
            throw new IllegalArgumentException("Semua field wajib diisi.");
        }

        this.id = (id == null) ? UUID.randomUUID() : id;
        this.userId = userId;
        this.technicianId = technicianId;
        this.repairOrderId = repairOrderId;
        this.comment = comment;
        this.score = score;
        this.deleted = deleted != null && deleted;
    }

    public void update(String comment, int score) {
        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("Rating harus antara 1 dan 5.");
        }
        if (comment == null || comment.isBlank()) {
            throw new IllegalArgumentException("Komentar tidak boleh null.");
        }

        this.comment = comment;
        this.score = score;
    }
}

package id.ac.ui.cs.advprog.everest.modules.rating.dto;

import jakarta.validation.constraints.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAndUpdateRatingRequest {

    @NotBlank(message = "Komentar tidak boleh kosong")
    @Size(max = 1000, message = "Komentar maksimal 1000 karakter")
    private String comment;

    @Min(value = 1, message = "Rating minimal 1")
    @Max(value = 5, message = "Rating maksimal 5")
    private int score;
}

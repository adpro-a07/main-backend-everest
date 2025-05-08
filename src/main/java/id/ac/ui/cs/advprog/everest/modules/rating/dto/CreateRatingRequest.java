package id.ac.ui.cs.advprog.everest.modules.rating.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRatingRequest {
    private String userId;
    private String technicianId;
    private String comment;
    private int rating;
}

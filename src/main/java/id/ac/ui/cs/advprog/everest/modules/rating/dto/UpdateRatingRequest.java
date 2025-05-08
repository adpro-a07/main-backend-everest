package id.ac.ui.cs.advprog.everest.modules.rating.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRatingRequest {
    private String comment;
    private int rating;
}

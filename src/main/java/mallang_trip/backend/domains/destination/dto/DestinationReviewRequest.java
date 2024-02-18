package mallang_trip.backend.domains.destination.dto;

import java.util.List;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DestinationReviewRequest {

    @NotNull
    @Max(value = 5)
    @Min(value = 0)
    private Double rate;

    @NotBlank
    private String content;

    private List<String> images;
}

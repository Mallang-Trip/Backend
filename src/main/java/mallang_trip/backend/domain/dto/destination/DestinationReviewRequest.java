package mallang_trip.backend.domain.dto.destination;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationReviewRequest {

    @NotNull
    private Double rate;

    @NotBlank
    private String content;

    private List<String> images;
}

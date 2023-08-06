package mallang_trip.backend.domain.dto.destination;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DestinationReviewRequest {

    private Double rate;
    private String content;
}

package mallang_trip.backend.domain.dto.course;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DestinationResponse {

    private Long destinationId;
    private String name;
    private String address;
}

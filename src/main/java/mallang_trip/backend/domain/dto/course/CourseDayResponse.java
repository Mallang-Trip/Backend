package mallang_trip.backend.domain.dto.course;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.dto.destination.DestinationResponse;

@Getter
@Builder
public class CourseDayResponse {

    private Integer day;
    private String startTime;
    private String endTime;
    private Integer hours;
    private Integer price;
    private List<DestinationResponse> destinations;

}

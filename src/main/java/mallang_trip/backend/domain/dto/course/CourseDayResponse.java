package mallang_trip.backend.domain.dto.course;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseDayResponse {

    private int day;
    private String startTime;
    private String endTime;
    private int hours;
    private int price;
    private List<DestinationResponse> destinations;

}

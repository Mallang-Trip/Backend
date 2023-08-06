package mallang_trip.backend.domain.dto.course;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseDetailsResponse {

    private Long courseId;
    private List<String> images;
    private int totalDays;
    private String name;
    private int capacity;
    private int totalPrice;
    private List<CourseDayResponse> days;

}

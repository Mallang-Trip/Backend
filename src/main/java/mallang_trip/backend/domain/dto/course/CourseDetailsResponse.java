package mallang_trip.backend.domain.dto.course;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseDetailsResponse {

    private Long courseId;
    private List<String> images;
    private Integer totalDays;
    private String name;
    private Integer capacity;
    private Integer totalPrice;
    private Integer discountPrice;
    private List<CourseDayResponse> days;

}

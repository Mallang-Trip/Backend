package mallang_trip.backend.domain.dto.Party;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mallang_trip.backend.domain.dto.course.CourseRequest;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartyRequest {

    private int headcount;
    private String startDate;
    private String endDate;
    private Long driverId;
    private String content;
    private Long courseId;

    // 코스 제안
    private Boolean changeCourse;
    private CourseRequest newCourse;
}

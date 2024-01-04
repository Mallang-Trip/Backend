package mallang_trip.backend.domain.dto.party;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.dto.course.CourseRequest;

@Getter
@Builder
public class JoinPartyRequest {

    private int headcount;
    private String content;

    // 코스 제안
    private Boolean changeCourse;
    private CourseRequest newCourse;
}

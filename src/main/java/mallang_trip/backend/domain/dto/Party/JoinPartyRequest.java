package mallang_trip.backend.domain.dto.Party;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.dto.course.CourseRequest;

@Getter
@Builder
public class JoinPartyRequest {

    private Long partyId;
    private int headcount;
    private String content;

    // 코스 제안
    private Boolean changeCourse;
    private CourseRequest newCourse;
}

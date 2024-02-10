package mallang_trip.backend.domain.dto.party;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.dto.course.CourseRequest;

@Getter
@Builder
public class JoinPartyRequest {

    private int headcount;
    private List<PartyMemberCompanionRequest> companions;
    private String content;
    private String cardId;
    // 코스 제안
    private Boolean changeCourse;
    private CourseRequest newCourse;
}

package mallang_trip.backend.domain.party.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.course.dto.CourseRequest;

@Getter
@Builder
public class JoinPartyRequest {

    private int headcount;
    private List<PartyMemberCompanionRequest> companions;
    private String content;
    // 코스 제안
    private Boolean changeCourse;
    private CourseRequest newCourse;
    private Long userPromotionCodeId; //없으면 -1
}

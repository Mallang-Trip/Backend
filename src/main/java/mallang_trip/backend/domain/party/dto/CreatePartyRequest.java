package mallang_trip.backend.domain.party.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mallang_trip.backend.domain.course.dto.CourseRequest;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePartyRequest {

	private int headcount;
	private List<PartyMemberCompanionRequest> companions;
	private LocalDate startDate;
	private LocalDate endDate;
	private Long driverId;
	private String content;
	private CourseRequest course;
	private Boolean monopoly;
	private Long userPromotionCodeId; // 유저 프로모션 코드 아이디 없으면 -1
}

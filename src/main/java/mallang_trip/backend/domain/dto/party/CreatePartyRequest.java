package mallang_trip.backend.domain.dto.party;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mallang_trip.backend.domain.dto.course.CourseRequest;

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
	private String cardId;
}

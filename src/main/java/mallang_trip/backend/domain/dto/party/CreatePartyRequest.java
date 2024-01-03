package mallang_trip.backend.domain.dto.party;

import java.time.LocalDate;
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
	private LocalDate startDate;
	private LocalDate endDate;
	private Long driverId;
	private String content;
	private CourseRequest course;
}

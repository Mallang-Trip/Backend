package mallang_trip.backend.domain.dto.party;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.dto.course.CourseRequest;

@Getter
@Builder
public class ChangeCourseRequest {

	private CourseRequest course;
	private String content;
}

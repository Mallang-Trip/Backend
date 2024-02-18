package mallang_trip.backend.domains.party.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domains.course.dto.CourseRequest;

@Getter
@Builder
public class ChangeCourseRequest {

	private CourseRequest course;
	private String content;
}

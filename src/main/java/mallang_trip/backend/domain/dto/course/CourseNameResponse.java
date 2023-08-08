package mallang_trip.backend.domain.dto.course;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.entity.party.Course;

@Getter
@Builder
public class CourseNameResponse {

    private Long courseId;
    private String courseName;

    public static CourseNameResponse of(Course course){
        return CourseNameResponse.builder()
            .courseId(course.getId())
            .courseName(course.getName())
            .build();
    }
}

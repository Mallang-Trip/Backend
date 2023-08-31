package mallang_trip.backend.domain.dto.course;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.entity.course.Course;

@Getter
@Builder
public class CourseIdResponse {

    private Long courseId;

    public static CourseIdResponse of(Course course){
        return CourseIdResponse.builder()
            .courseId(course.getId())
            .build();
    }
}

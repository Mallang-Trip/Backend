package mallang_trip.backend.domain.course.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.course.entity.Course;

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

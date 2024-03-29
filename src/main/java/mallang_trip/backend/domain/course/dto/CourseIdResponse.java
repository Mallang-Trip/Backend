package mallang_trip.backend.domain.course.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.course.entity.Course;

@Getter
@Builder
public class CourseIdResponse {

    private Long courseId;

    /**
     * Course 객체로 CourseIdResponse 객체를 생성합니다.
     * @param course Course 객체
     * @return CourseIdResponse 객체
     */
    public static CourseIdResponse of(Course course){
        return CourseIdResponse.builder()
            .courseId(course.getId())
            .build();
    }
}

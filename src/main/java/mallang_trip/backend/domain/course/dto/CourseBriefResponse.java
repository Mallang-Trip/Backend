package mallang_trip.backend.domain.course.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.course.entity.Course;

@Getter
@Builder
public class CourseBriefResponse {

    private Long courseId;
    private String courseName;
    private String courseImg;

    /**
     * Course 객체로 CourseBriefResponse 객체를 생성합니다.
     * @param course Course 객체
     * @return CourseBriefResponse 객체
     */
    public static CourseBriefResponse of(Course course){
        List<String> images = course.getImages();
        return CourseBriefResponse.builder()
            .courseId(course.getId())
            .courseName(course.getName())
            .courseImg(images.isEmpty() ? null : images.get(0))
            .build();
    }
}

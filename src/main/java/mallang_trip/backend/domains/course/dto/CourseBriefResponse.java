package mallang_trip.backend.domains.course.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domains.course.entity.Course;

@Getter
@Builder
public class CourseBriefResponse {

    private Long courseId;
    private String courseName;
    private String courseImg;

    public static CourseBriefResponse of(Course course){
        List<String> images = course.getImages();
        return CourseBriefResponse.builder()
            .courseId(course.getId())
            .courseName(course.getName())
            .courseImg(images.isEmpty() ? null : images.get(0))
            .build();
    }
}

package mallang_trip.backend.domain.dto.course;

import java.time.LocalTime;
import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.entity.course.Course;
import mallang_trip.backend.domain.entity.course.CourseDay;

@Getter
@Builder
public class CourseDayRequest {

    @NotNull
    @Min(value = 1)
    private Integer day;

    @NotNull
    private String startTime;

    @NotNull
    private String endTime;

    @NotNull
    @Min(value = 1)
    private Integer hours;

    @NotNull
    @Min(value = 0)
    private Integer price;
    private List<Long> destinations;

    public CourseDay toCourseDay(Course course){
        return CourseDay.builder()
            .course(course)
            .day(day)
            .startTime(LocalTime.parse(startTime))
            .endTime(LocalTime.parse(endTime))
            .hours(hours)
            .price(price)
            .destinations(destinations)
            .build();
    }
}

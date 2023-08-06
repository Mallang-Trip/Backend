package mallang_trip.backend.domain.dto.course;

import java.time.LocalTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.entity.party.Course;
import mallang_trip.backend.domain.entity.party.CourseDay;

@Getter
@Builder
public class CourseDayRequest {

    private int day;
    private String startTime;
    private String endTime;
    private int hours;
    private int price;
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

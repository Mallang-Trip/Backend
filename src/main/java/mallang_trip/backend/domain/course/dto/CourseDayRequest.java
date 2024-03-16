package mallang_trip.backend.domain.course.dto;

import java.time.LocalTime;
import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.course.entity.Course;
import mallang_trip.backend.domain.course.entity.CourseDay;

@Getter
@Builder
public class CourseDayRequest {

    @NotNull
    @Min(value = 1)
    @ApiModelProperty(value = "코스 일자", required = true)
    private Integer day;

    @NotNull
    @ApiModelProperty(value = "시작 시간", required = true)
    private String startTime;

    @NotNull
    @ApiModelProperty(value = "종료 시간", required = true)
    private String endTime;

    @NotNull
    @Min(value = 1)
    @ApiModelProperty(value = "시간", required = true)
    private Integer hours;

    @NotNull
    @Min(value = 0)
    @ApiModelProperty(value = "가격", required = true)
    private Integer price;
    @ApiModelProperty(value = "목적지 ID 배열", required = false)
    private List<Long> destinations;

    /**
     * Course로 CourseDay 객체 생성
     * @param course
     * @return CourseDay
     */
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

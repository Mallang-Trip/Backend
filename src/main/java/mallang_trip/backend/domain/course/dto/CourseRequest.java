package mallang_trip.backend.domain.course.dto;

import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseRequest {

    private List<String> images;

    @NotNull
    @Min(value = 1)
    private Integer totalDays;

    @NotBlank
    private String name;

    @NotNull
    @Min(value = 1)
    private Integer capacity;

    @NotNull
    @Min(value = 0)
    private Integer totalPrice;

    private List<CourseDayRequest> days;

}

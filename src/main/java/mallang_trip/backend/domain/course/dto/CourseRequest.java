package mallang_trip.backend.domain.course.dto;

import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseRequest {

    @ApiModelProperty(value = "코스 이미지 URL 배열", required = false)
    private List<String> images;

    @NotNull
    @Min(value = 1)
    @ApiModelProperty(value = "총 일수", required = true)
    private Integer totalDays;

    @NotBlank
    @ApiModelProperty(value = "코스 이름", required = true)
    private String name;

    @NotNull
    @Min(value = 1)
    @ApiModelProperty(value = "최대 수용인원", required = true)
    private Integer capacity;

    @NotNull
    @Min(value = 0)
    @ApiModelProperty(value = "총 가격", required = true)
    private Integer totalPrice;

    @ApiModelProperty(value = "코스 일자 배열", required = false)
    private List<CourseDayRequest> days;

}

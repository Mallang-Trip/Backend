package mallang_trip.backend.domain.admin.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter
@Builder
public class ObjectionRequest {

    @NotBlank
    @ApiModelProperty(value = "이의제기 내용", required = true)
    private String content;

    //private Long reportId;
}

package mallang_trip.backend.global.config.swagger;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomPageable {

    @ApiModelProperty(value = "페이지 번호 (0..N)")
    private Integer page;

    @ApiModelProperty(value = "페이지 크기")
    private Integer size;
}

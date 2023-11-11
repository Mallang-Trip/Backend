package mallang_trip.backend.domain.dto.driver;

import java.util.List;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class DriverReviewRequest {

    @NotNull
    @Max(value = 5)
    @Min(value = 0)
    private Double rate;

    @NotBlank
    private String content;

    private List<String> images;
}

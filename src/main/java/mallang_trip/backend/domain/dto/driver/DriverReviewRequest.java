package mallang_trip.backend.domain.dto.driver;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverReviewRequest {

    private Double rate;
    private String content;
    private List<String> images;
}

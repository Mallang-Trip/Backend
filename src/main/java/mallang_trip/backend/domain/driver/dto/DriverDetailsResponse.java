package mallang_trip.backend.domain.driver.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.course.dto.CourseBriefResponse;

@Getter
@Builder
public class DriverDetailsResponse {

    private Long driverId;
    private String name;
    private String profileImg;
    private Integer reservationCount;
    private Double avgRate;
    private String introduction;
    private String region;
    private List<DriverReviewResponse> reviews;
    private List<CourseBriefResponse> courses;
}

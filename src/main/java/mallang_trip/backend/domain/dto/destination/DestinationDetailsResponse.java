package mallang_trip.backend.domain.dto.destination;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DestinationDetailsResponse {

    private Long destinationId;
    private String name;
    private String address;
    private Double lon;
    private Double lat;
    private String content;
    private List<String> images;
    private Integer views;
    private List<DestinationReviewResponse> reviews;
    private Double avgRate;
    private boolean dibs;
}

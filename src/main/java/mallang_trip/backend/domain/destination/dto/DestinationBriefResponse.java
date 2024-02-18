package mallang_trip.backend.domain.destination.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.destination.entity.Destination;

@Getter
@Builder
public class DestinationBriefResponse {
//평점, 조회수
    private Long destinationId;
    private String name;
    private String address;
    private Double lon;
    private Double lat;
    private String image;
    private Double rate;
    private Integer views;
    private Boolean dibs;

    public static DestinationBriefResponse of(Destination destination, Boolean dibs, Double rate){
        return DestinationBriefResponse.builder()
            .destinationId(destination.getId())
            .image(destination.getImages().isEmpty() ? null : destination.getImages().get(0))
            .rate(rate)
            .views(destination.getViews())
            .name(destination.getName())
            .address(destination.getAddress())
            .lat(destination.getLat())
            .lon(destination.getLon())
            .dibs(dibs)
            .build();
    }
}

package mallang_trip.backend.domain.destination.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.destination.entity.Destination;

@Getter
@Builder
public class DestinationResponse {

    private Long destinationId;
    private String name;
    private String address;
    private Double lon;
    private Double lat;

    public static DestinationResponse of(Destination destination){
        return DestinationResponse.builder()
            .destinationId(destination.getId())
            .name(destination.getName())
            .address(destination.getAddress())
            .lon(destination.getLon())
            .lat(destination.getLat())
            .build();
    }
}

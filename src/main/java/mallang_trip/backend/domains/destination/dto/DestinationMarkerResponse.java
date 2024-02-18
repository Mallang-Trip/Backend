package mallang_trip.backend.domains.destination.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domains.destination.entity.Destination;

@Getter
@Builder
public class DestinationMarkerResponse {

    private Long destinationId;
    private Double lat;
    private Double lon;

    public static DestinationMarkerResponse of(Destination destination){
        return DestinationMarkerResponse.builder()
            .destinationId(destination.getId())
            .lat(destination.getLat())
            .lon(destination.getLon())
            .build();
    }
}

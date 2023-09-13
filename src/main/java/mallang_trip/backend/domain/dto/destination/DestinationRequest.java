package mallang_trip.backend.domain.dto.destination;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.entity.destination.Destination;

@Getter
@Builder
public class DestinationRequest {

    private String name;
    private String address;
    private Double lon;
    private Double lat;
    private String content;
    private List<String> images;

    public Destination toDestination(){
        return Destination.builder()
            .name(name)
            .address(address)
            .lon(lon)
            .lat(lat)
            .content(content)
            .images(images)
            .build();
    }
}

package mallang_trip.backend.domain.destination.dto;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.destination.constant.DestinationType;
import mallang_trip.backend.domain.destination.entity.Destination;

@Getter
@Builder
public class DestinationRequest {

    @NotEmpty
    private String name;
    @NotEmpty
    private String address;
    @NotNull
    private Double lon;
    @NotNull
    private Double lat;
    private String content;
    private List<String> images;

    public Destination toDestination(DestinationType type){
        return Destination.builder()
            .name(name)
            .address(address)
            .lon(lon)
            .lat(lat)
            .content(content)
            .images(images)
            .type(type)
            .build();
    }
}

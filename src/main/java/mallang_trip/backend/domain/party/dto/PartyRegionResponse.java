package mallang_trip.backend.domain.party.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.party.entity.PartyRegion;

@Getter
@Builder
public class PartyRegionResponse {

    private Long partyRegionId;

    private String region;

    private String regionImg;

    public static PartyRegionResponse of(PartyRegion partyRegion){
        return PartyRegionResponse.builder()
            .partyRegionId(partyRegion.getId())
            .region(partyRegion.getRegion())
            .regionImg(partyRegion.getRegionImg())
            .build();
    }
}

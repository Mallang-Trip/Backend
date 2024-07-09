package mallang_trip.backend.domain.region.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.region.entity.Region;

@Getter
@Builder
public class RegionResponse {

	private Long regionId;
	private String name;
	private String image;

	public static RegionResponse of(Region region) {
		return RegionResponse.builder()
			.regionId(region.getId())
			.name(region.getName())
			.image(region.getImage())
			.build();
	}
}

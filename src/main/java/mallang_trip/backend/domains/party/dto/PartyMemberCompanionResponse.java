package mallang_trip.backend.domains.party.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domains.party.entity.PartyMemberCompanion;

@Builder
@Getter
public class PartyMemberCompanionResponse {

	private String name;
	private String phoneNumber;

	public static PartyMemberCompanionResponse of(PartyMemberCompanion companion) {
		return PartyMemberCompanionResponse.builder()
			.name(companion.getName())
			.phoneNumber(companion.getPhoneNumber())
			.build();
	}
}

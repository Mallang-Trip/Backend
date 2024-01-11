package mallang_trip.backend.domain.dto.party;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.entity.party.PartyMemberCompanion;

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

package mallang_trip.backend.domain.dto.party;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.entity.party.PartyMemberCompanion;

@Builder
@Getter
public class PartyMemberCompanionRequest {

	private String name;
	private String phoneNumber;

	public static PartyMemberCompanionRequest of(PartyMemberCompanion companion){
		return PartyMemberCompanionRequest.builder()
			.name(companion.getName())
			.phoneNumber(companion.getPhoneNumber())
			.build();
	}
}

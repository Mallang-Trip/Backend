package mallang_trip.backend.domain.dto.Party;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.entity.party.PartyMembers;
import mallang_trip.backend.domain.entity.user.User;

@Getter
@Builder
public class PartyMemberResponse {

    private Long userId;
    private String nickname;
    private Integer headcount;

    public static PartyMemberResponse of(PartyMembers members){
        User user = members.getUser();
        return PartyMemberResponse.builder()
            .userId(user.getId())
            .nickname(user.getNickname())
            .headcount(members.getHeadcount())
            .build();
    }
}

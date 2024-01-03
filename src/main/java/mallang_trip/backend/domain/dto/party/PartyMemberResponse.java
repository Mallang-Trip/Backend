package mallang_trip.backend.domain.dto.party;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.entity.party.PartyMember;
import mallang_trip.backend.domain.entity.user.User;

@Getter
@Builder
public class PartyMemberResponse {

    private Long userId;
    private String nickname;
    private String profileImg;
    private Integer headcount;
    private Boolean ready;

    public static PartyMemberResponse of(PartyMember members){
        User user = members.getUser();
        return PartyMemberResponse.builder()
            .userId(user.getId())
            .nickname(user.getNickname())
            .profileImg(user.getProfileImage())
            .headcount(members.getHeadcount())
            .ready(members.getReady())
            .build();
    }
}

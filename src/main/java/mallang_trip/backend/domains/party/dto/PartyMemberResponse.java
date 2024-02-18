package mallang_trip.backend.domains.party.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domains.user.constant.Gender;
import mallang_trip.backend.domains.party.entity.PartyMember;
import mallang_trip.backend.domains.user.entity.User;

@Getter
@Builder
public class PartyMemberResponse {

    private Long userId;
    private String nickname;
    private Integer ageRange;
    private Gender gender;
    private String profileImg;
    private String introduction;
    private Integer headcount;
    private List<PartyMemberCompanionResponse> companions;
    private Boolean ready;

    public static PartyMemberResponse of(PartyMember members, List<PartyMemberCompanionResponse> companions){
        User user = members.getUser();
        return PartyMemberResponse.builder()
            .userId(user.getId())
            .nickname(user.getNickname())
            .ageRange(user.getAgeRange())
            .gender(user.getGender())
            .profileImg(user.getProfileImage())
            .introduction(user.getIntroduction())
            .headcount(members.getHeadcount())
            .companions(companions)
            .ready(members.getReady())
            .build();
    }
}

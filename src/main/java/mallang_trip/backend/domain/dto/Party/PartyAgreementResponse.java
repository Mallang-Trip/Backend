package mallang_trip.backend.domain.dto.Party;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.constant.AgreementStatus;
import mallang_trip.backend.domain.entity.party.PartyAgreement;
import mallang_trip.backend.domain.entity.user.User;

@Getter
@Builder
public class PartyAgreementResponse {

    private Long userId;
    private String nickname;
    private AgreementStatus status;

    public static PartyAgreementResponse of(PartyAgreement agreement){
        User user = agreement.getMembers().getUser();
        return PartyAgreementResponse.builder()
            .userId(user.getId())
            .nickname(user.getNickname())
            .status(agreement.getStatus())
            .build();
    }
}

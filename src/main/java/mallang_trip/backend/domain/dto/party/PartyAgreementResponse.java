package mallang_trip.backend.domain.dto.party;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.constant.AgreementStatus;
import mallang_trip.backend.domain.entity.party.PartyProposalAgreement;
import mallang_trip.backend.domain.entity.user.User;

@Getter
@Builder
public class PartyAgreementResponse {

    private Long userId;
    private String nickname;
    private AgreementStatus status;

    public static PartyAgreementResponse of(PartyProposalAgreement agreement){
        User user = agreement.getMember().getUser();
        return PartyAgreementResponse.builder()
            .userId(user.getId())
            .nickname(user.getNickname())
            .status(agreement.getStatus())
            .build();
    }
}

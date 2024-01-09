package mallang_trip.backend.domain.dto.party;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.constant.AgreementStatus;
import mallang_trip.backend.domain.entity.party.PartyProposalAgreement;
import mallang_trip.backend.domain.entity.user.User;

@Getter
@Builder
public class PartyProposalAgreementResponse {

    private Long userId;
    private AgreementStatus status;

    public static PartyProposalAgreementResponse of(PartyProposalAgreement agreement){
        User user = agreement.getMember().getUser();
        return PartyProposalAgreementResponse.builder()
            .userId(user.getId())
            .status(agreement.getStatus())
            .build();
    }
}

package mallang_trip.backend.domains.party.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domains.party.constant.AgreementStatus;
import mallang_trip.backend.domains.party.entity.PartyProposalAgreement;
import mallang_trip.backend.domains.user.entity.User;

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

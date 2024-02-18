package mallang_trip.backend.domains.party.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domains.party.constant.AgreementStatus;
import mallang_trip.backend.domains.user.constant.Gender;
import mallang_trip.backend.domains.party.constant.ProposalStatus;
import mallang_trip.backend.domains.party.constant.ProposalType;
import mallang_trip.backend.domains.course.dto.CourseDetailsResponse;

@Getter
@Builder
public class PartyProposalResponse {

    private Long proposalId;
    private Long proposerId;
    private String proposerNickname;
    private Integer proposerAgeRange;
    private Gender proposerGender;
    private String proposerProfileImg;
    private Integer proposerHeadcount;
    private List<PartyMemberCompanionResponse> proposerCompanions;
    private ProposalType type;
    private ProposalStatus status;
    private AgreementStatus driverAgreement;
    private CourseDetailsResponse course;
    private String content;
    private List<PartyProposalAgreementResponse> memberAgreement;
    private LocalDateTime createdAt;
}

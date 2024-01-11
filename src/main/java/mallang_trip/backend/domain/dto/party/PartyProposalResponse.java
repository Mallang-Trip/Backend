package mallang_trip.backend.domain.dto.party;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.constant.AgreementStatus;
import mallang_trip.backend.constant.Gender;
import mallang_trip.backend.constant.ProposalStatus;
import mallang_trip.backend.constant.ProposalType;
import mallang_trip.backend.domain.dto.course.CourseDetailsResponse;

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

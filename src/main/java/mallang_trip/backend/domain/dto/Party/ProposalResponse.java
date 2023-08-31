package mallang_trip.backend.domain.dto.Party;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.constant.AgreementStatus;
import mallang_trip.backend.constant.ProposalStatus;
import mallang_trip.backend.constant.ProposalType;
import mallang_trip.backend.domain.dto.course.CourseDetailsResponse;

@Getter
@Builder
public class ProposalResponse {

    private Long proposalId;
    private Long proposerId;
    private String proposerNickname;
    private ProposalType type;
    private ProposalStatus status;
    private AgreementStatus driverAgreement;
    private CourseDetailsResponse course;
    private Integer headcount;
    private String content;
    private List<PartyAgreementResponse> memberAgreement;
}

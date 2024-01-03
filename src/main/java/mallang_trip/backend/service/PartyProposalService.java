package mallang_trip.backend.service;

import static mallang_trip.backend.constant.ProposalType.JOIN_WITH_COURSE_CHANGE;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.dto.party.JoinPartyRequest;
import mallang_trip.backend.domain.entity.course.Course;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.party.PartyProposal;
import mallang_trip.backend.repository.party.PartyAgreementRepository;
import mallang_trip.backend.repository.party.PartyProposalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyProposalService {

	private final UserService userService;
	private final CourseService courseService;
	private final PartyProposalRepository partyProposalRepository;
	private final PartyAgreementRepository partyAgreementRepository;

	/** 파티 가입 with 코스 변경 신청 */
	public void createJoinWithCourseChange(Party party, JoinPartyRequest request){
		Course course = courseService.createCourse(request.getNewCourse());
		partyProposalRepository.save(PartyProposal.builder()
			.course(course)
			.party(party)
			.proposer(userService.getCurrentUser())
			.headcount(request.getHeadcount())
			.content(request.getContent())
			.type(JOIN_WITH_COURSE_CHANGE)
			.build());
	}
	/** 코스 변경 신청 */

	/** 신청 취소 */

	/** Proposal 수락 */

	/** Proposal 거절 */

	/** 만장일치 수락 확인 */
}

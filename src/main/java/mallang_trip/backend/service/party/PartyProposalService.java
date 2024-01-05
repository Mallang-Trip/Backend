package mallang_trip.backend.service.party;

import static mallang_trip.backend.constant.PartyStatus.SEALED;
import static mallang_trip.backend.constant.ProposalType.COURSE_CHANGE;
import static mallang_trip.backend.constant.ProposalType.JOIN_WITH_COURSE_CHANGE;
import static mallang_trip.backend.controller.io.BaseResponseStatus.EXPIRED_PROPOSAL;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.controller.io.BaseResponseStatus.NOT_PARTY_MEMBER;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.constant.AgreementStatus;
import mallang_trip.backend.constant.PartyStatus;
import mallang_trip.backend.constant.ProposalStatus;
import mallang_trip.backend.constant.Role;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.party.ChangeCourseRequest;
import mallang_trip.backend.domain.dto.party.JoinPartyRequest;
import mallang_trip.backend.domain.entity.course.Course;
import mallang_trip.backend.domain.entity.driver.Driver;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.party.PartyMember;
import mallang_trip.backend.domain.entity.party.PartyProposal;
import mallang_trip.backend.domain.entity.party.PartyProposalAgreement;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.party.PartyProposalAgreementRepository;
import mallang_trip.backend.repository.party.PartyMemberRepository;
import mallang_trip.backend.repository.party.PartyProposalRepository;
import mallang_trip.backend.service.CourseService;
import mallang_trip.backend.service.DriverService;
import mallang_trip.backend.service.UserService;
import mallang_trip.backend.service.party.PartyMemberService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyProposalService {

	private final UserService userService;
	private final DriverService driverService;
	private final CourseService courseService;
	private final PartyMemberService partyMemberService;
	private final PartyProposalRepository partyProposalRepository;
	private final PartyProposalAgreementRepository partyProposalAgreementRepository;
	private final PartyMemberRepository partyMemberRepository;

	/**
	 * PartyProposal (Type: JOIN_WITH_COURSE_CHANGE) 생성
	 */
	public void createJoinWithCourseChange(Party party, JoinPartyRequest request) {
		Course course = courseService.createCourse(request.getNewCourse());
		PartyProposal proposal = partyProposalRepository.save(PartyProposal.builder()
			.course(course)
			.party(party)
			.proposer(userService.getCurrentUser())
			.headcount(request.getHeadcount())
			.content(request.getContent())
			.type(JOIN_WITH_COURSE_CHANGE)
			.build());
		createPartyProposalAgreements(proposal);
	}

	/**
	 * PartyProposal (Type: COURSE_CHANGE) 생성
	 */
	public void createCourseChange(Party party, ChangeCourseRequest request) {
		Course course = courseService.createCourse(request.getCourse());
		PartyProposal proposal = partyProposalRepository.save(PartyProposal.builder()
			.course(course)
			.party(party)
			.proposer(userService.getCurrentUser())
			.headcount(null)
			.content(request.getContent())
			.type(COURSE_CHANGE)
			.build());
		createPartyProposalAgreements(proposal);
		voteProposalByMember(proposal, true);
	}

	/**
	 * PartyProposal Status -> CANCELED
	 */
	public void cancelProposal(PartyProposal proposal) {
		// 권한 CHECK
		if (!userService.getCurrentUser().equals(proposal.getProposer())) {
			throw new BaseException(Forbidden);
		}
		// proposal status CHECK
		if (!proposal.getStatus().equals(ProposalStatus.WAITING)) {
			throw new BaseException(EXPIRED_PROPOSAL);
		}
		proposal.setStatus(ProposalStatus.CANCELED);
	}

	/**
	 * 제안 수락 or 거절 투표
	 */
	public void voteProposal(PartyProposal proposal, Boolean accept) {
		if (userService.getCurrentUser().getRole().equals(Role.ROLE_DRIVER)) {
			voteProposalByDriver(proposal, accept);
		}
		if (userService.getCurrentUser().getRole().equals(Role.ROLE_USER)) {
			voteProposalByMember(proposal, accept);
		}
	}

	/**
	 * (드라이버) Proposal 수락 or 거절 투표
	 */
	private void voteProposalByDriver(PartyProposal proposal, Boolean accept) {
		Driver driver = driverService.getCurrentDriver();
		if (!proposal.getParty().getDriver().equals(driver)) {
			throw new BaseException(NOT_PARTY_MEMBER);
		}
		if (accept) {
			proposal.setDriverAgreement(AgreementStatus.ACCEPT);
		} else {
			proposal.setDriverAgreement(AgreementStatus.REFUSE);
			refuseProposal(proposal);
		}
	}

	/**
	 * (파티 멤버) Proposal 수락 or 거절 투표
	 */
	private void voteProposalByMember(PartyProposal proposal, Boolean accept) {
		User user = userService.getCurrentUser();
		PartyMember member = partyMemberRepository.findByPartyAndUser(proposal.getParty(),
				user)
			.orElseThrow(() -> new BaseException(NOT_PARTY_MEMBER));
		PartyProposalAgreement agreement = partyProposalAgreementRepository.findByMemberAndProposal(
				member, proposal)
			.orElseThrow(() -> new BaseException(Forbidden));
		if (accept) {
			agreement.setStatus(AgreementStatus.ACCEPT);
		} else {
			agreement.setStatus(AgreementStatus.REFUSE);
			refuseProposal(proposal);
		}
	}

	/**
	 * 제안 한 명이라도 거절했을 경우 Proposal Status -> REFUSED Party Status -> 이전 Status
	 */
	private void refuseProposal(PartyProposal proposal) {
		proposal.setStatus(ProposalStatus.REFUSED);
		if (proposal.getType().equals(JOIN_WITH_COURSE_CHANGE)) {
			proposal.getParty().setStatus(PartyStatus.RECRUITING);
		}
		if (proposal.getType().equals(COURSE_CHANGE)) {
			proposal.getParty().setStatus(SEALED);
		}
	}

	/**
	 * 제안 만장일치 수락 확인
	 */
	public Boolean isUnanimity(PartyProposal proposal) {
		return partyProposalRepository.isUnanimity(proposal.getId());
	}

	/**
	 * 모든 파티 멤버에 대한 party_proposal_agreement 생성
	 */
	private void createPartyProposalAgreements(PartyProposal proposal) {
		partyMemberService.getMembers(proposal.getParty()).stream()
			.forEach(member -> {
				partyProposalAgreementRepository.save(PartyProposalAgreement.builder()
					.proposal(proposal)
					.member(member)
					.build());
			});
	}

	/**
	 * 만료된 제안 처리
	 * 응답하지 않은 agreement status -> refused
	 * proposal status -> refused
	 */
	public void handleExpiredProposal(PartyProposal proposal) {
		// 만료된 PartyProposalAgreement 거절 처리
		partyProposalAgreementRepository.findByProposal(proposal).stream()
			.forEach(agreement -> {
				if (agreement.getStatus().equals(AgreementStatus.WAITING)) {
					agreement.setStatus(AgreementStatus.REFUSE);
				}
			});
		if (proposal.getDriverAgreement().equals(AgreementStatus.WAITING)) {
			proposal.setDriverAgreement(AgreementStatus.REFUSE);
		}
		proposal.setStatus(ProposalStatus.REFUSED);
	}

	/**
	 * 진행중인 파티의 코스 변경 제안을 종료 (거절 처리)
	 */
	public void expireWaitingProposalByParty(Party party) {
		PartyProposal proposal = partyProposalRepository.findByPartyAndStatus(party,
				ProposalStatus.WAITING).orElse(null);
		if (proposal != null) {
			handleExpiredProposal(proposal);
		}
	}

	/**
	 * party_proposal_agreement 삭제
	 */
	public void deleteAgreement(PartyProposal proposal, PartyMember member){
		partyProposalAgreementRepository.deleteByProposalAndMember(proposal, member);
	}
}

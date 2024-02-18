package mallang_trip.backend.domains.party.service;

import static mallang_trip.backend.domains.party.constant.PartyStatus.RECRUITING;
import static mallang_trip.backend.domains.party.constant.PartyStatus.SEALED;
import static mallang_trip.backend.domains.party.constant.ProposalType.COURSE_CHANGE;
import static mallang_trip.backend.domains.party.constant.ProposalType.JOIN_WITH_COURSE_CHANGE;
import static mallang_trip.backend.domains.global.io.BaseResponseStatus.EXPIRED_PROPOSAL;
import static mallang_trip.backend.domains.global.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.domains.global.io.BaseResponseStatus.NOT_PARTY_MEMBER;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domains.party.constant.AgreementStatus;
import mallang_trip.backend.domains.party.constant.PartyStatus;
import mallang_trip.backend.domains.party.constant.ProposalStatus;
import mallang_trip.backend.domains.user.constant.Role;
import mallang_trip.backend.domains.global.io.BaseException;
import mallang_trip.backend.domains.party.dto.ChangeCourseRequest;
import mallang_trip.backend.domains.party.dto.JoinPartyRequest;
import mallang_trip.backend.domains.party.dto.PartyMemberCompanionRequest;
import mallang_trip.backend.domains.party.dto.PartyMemberCompanionResponse;
import mallang_trip.backend.domains.party.dto.PartyProposalAgreementResponse;
import mallang_trip.backend.domains.party.dto.PartyProposalResponse;
import mallang_trip.backend.domains.course.entity.Course;
import mallang_trip.backend.domains.driver.entity.Driver;
import mallang_trip.backend.domains.party.entity.Party;
import mallang_trip.backend.domains.party.entity.PartyMember;
import mallang_trip.backend.domains.party.entity.PartyMemberCompanion;
import mallang_trip.backend.domains.party.entity.PartyProposal;
import mallang_trip.backend.domains.party.entity.PartyProposalAgreement;
import mallang_trip.backend.domains.user.entity.User;
import mallang_trip.backend.domains.course.service.CourseService;
import mallang_trip.backend.domains.driver.service.DriverService;
import mallang_trip.backend.domains.party.repository.PartyMemberCompanionRepository;
import mallang_trip.backend.domains.party.repository.PartyMemberRepository;
import mallang_trip.backend.domains.party.repository.PartyProposalAgreementRepository;
import mallang_trip.backend.domains.party.repository.PartyProposalRepository;
import mallang_trip.backend.domains.user.service.UserService;
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
	private final PartyNotificationService partyNotificationService;
	private final PartyProposalRepository partyProposalRepository;
	private final PartyProposalAgreementRepository partyProposalAgreementRepository;
	private final PartyMemberRepository partyMemberRepository;
	private final PartyMemberCompanionRepository partyMemberCompanionRepository;

	/**
	 * PartyProposal, PartyProposalAgreement 생성 (Type: JOIN_WITH_COURSE_CHANGE)
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
		createPartyMemberCompanions(proposal, request.getCompanions());
		createPartyProposalAgreements(proposal);
	}

	/**
	 * PartyProposal, PartyProposalAgreement 생성 (Type: COURSE_CHANGE)
	 */
	public PartyProposal createCourseChange(Party party, ChangeCourseRequest request) {
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
		// 생성자는 자동 수락
		voteProposal(proposal, true);
		return proposal;
	}

	/**
	 * PartyProposal Status -> CANCELED.
	 * Party Status 이전 상태로 복구.
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
		if(proposal.getType().equals(JOIN_WITH_COURSE_CHANGE)){
			proposal.getParty().setStatus(RECRUITING);
		} else if (proposal.getType().equals(COURSE_CHANGE)){
			proposal.getParty().setStatus(SEALED);
		}
	}

	/**
	 * 제안 수락 or 거절 투표
	 */
	public void voteProposal(PartyProposal proposal, Boolean accept) {
		Role role = userService.getCurrentUser().getRole();
		if (role.equals(Role.ROLE_DRIVER)) {
			voteProposalByDriver(proposal, accept);
		}
		if (role.equals(Role.ROLE_USER)) {
			voteProposalByMember(proposal, accept);
		}
	}

	/**
	 * (드라이버) Proposal 수락 or 거절
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
	 * (파티 멤버) Proposal 수락 or 거절
	 */
	private void voteProposalByMember(PartyProposal proposal, Boolean accept) {
		User user = userService.getCurrentUser();
		PartyMember member = partyMemberRepository.findByPartyAndUser(proposal.getParty(), user)
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
	 * 제안 한 명이라도 거절했을 경우 : Proposal Status -> REFUSED. Party Status -> 이전 Status.
	 */
	private void refuseProposal(PartyProposal proposal) {
		proposal.setStatus(ProposalStatus.REFUSED);
		if (proposal.getType().equals(JOIN_WITH_COURSE_CHANGE)) {
			proposal.getParty().setStatus(PartyStatus.RECRUITING);
			partyNotificationService.joinRefused(proposal);
		}
		if (proposal.getType().equals(COURSE_CHANGE)) {
			proposal.getParty().setStatus(SEALED);
			partyNotificationService.courseChangeRefused(proposal);
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
	 * 가입 신청 시 동행자 저장
	 */
	private void createPartyMemberCompanions(PartyProposal proposal, List<PartyMemberCompanionRequest> requests){
		if(requests == null){
			return;
		}
		requests.stream().forEach(companionRequest -> {
			partyMemberCompanionRepository.save(PartyMemberCompanion.builder()
				.proposal(proposal)
				.name(companionRequest.getName())
				.phoneNumber(companionRequest.getPhoneNumber())
				.build());
		});
	}

	/**
	 * 제안 만료 처리. 응답하지 않은 agreement status -> refuse. proposal status -> refused.
	 */
	public void expireProposal(PartyProposal proposal) {
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
		if(proposal.getType().equals(JOIN_WITH_COURSE_CHANGE)){
			proposal.getParty().setStatus(RECRUITING);
			partyNotificationService.joinRefused(proposal);
		} else if (proposal.getType().equals(COURSE_CHANGE)){
			proposal.getParty().setStatus(SEALED);
			partyNotificationService.courseChangeRefused(proposal);
		}
	}

	/**
	 * 파티에 진행중인 파티가입신청 or 코스변경제안 조회. 없으면 null 반환.
	 */
	public PartyProposal getWaitingProposalByParty(Party party) {
		return partyProposalRepository.findByPartyAndStatus(party, ProposalStatus.WAITING)
			.orElse(null);
	}

	/**
	 * 파티에 진행중인 파티가입신청 or 코스변경제안이 있다면, 만료 처리.
	 */
	public void expireWaitingProposalByParty(Party party) {
		PartyProposal proposal = getWaitingProposalByParty(party);
		if (proposal != null) {
			expireProposal(proposal);
		}
	}

	/**
	 * party_proposal_agreement 삭제
	 */
	public void deleteAgreement(PartyProposal proposal, PartyMember member) {
		partyProposalAgreementRepository.deleteByProposalAndMember(proposal, member);
	}

	/**
	 * PartyProposal -> PartyProposalResponse 변환
	 */
	public PartyProposalResponse toPartyProposalResponse(PartyProposal proposal) {
		if (proposal == null) {
			return null;
		}
		List<PartyMemberCompanionResponse> companions = partyMemberCompanionRepository.findByProposal(
				proposal).stream().map(PartyMemberCompanionResponse::of)
			.collect(Collectors.toList());
		List<PartyProposalAgreementResponse> memberAgreement = partyProposalAgreementRepository.findByProposal(
				proposal).stream().map(PartyProposalAgreementResponse::of)
			.collect(Collectors.toList());
		User proposer = proposal.getProposer();
		return PartyProposalResponse.builder()
			.proposalId(proposal.getId())
			.proposerId(proposer.getId())
			.proposerNickname(proposer.getNickname())
			.proposerAgeRange(proposer.getAgeRange())
			.proposerGender(proposer.getGender())
			.proposerProfileImg(proposer.getProfileImage())
			.proposerHeadcount(proposal.getHeadcount())
			.proposerCompanions(companions)
			.type(proposal.getType())
			.status(proposal.getStatus())
			.driverAgreement(proposal.getDriverAgreement())
			.course(courseService.getCourseDetails(proposal.getCourse()))
			.content(proposal.getContent())
			.memberAgreement(memberAgreement)
			.createdAt(proposal.getCreatedAt())
			.build();
	}
}

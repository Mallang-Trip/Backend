package mallang_trip.backend.service;

import static mallang_trip.backend.constant.PartyStatus.DRIVER_REFUSED;
import static mallang_trip.backend.constant.PartyStatus.RECRUITING;
import static mallang_trip.backend.constant.PartyStatus.SEALED;
import static mallang_trip.backend.constant.PartyStatus.WAITING_COURSE_CHANGE_APPROVAL;
import static mallang_trip.backend.constant.PartyStatus.WAITING_DRIVER_APPROVAL;
import static mallang_trip.backend.constant.PartyStatus.WAITING_JOIN_APPROVAL;
import static mallang_trip.backend.constant.ProposalType.COURSE_CHANGE;
import static mallang_trip.backend.constant.ProposalType.JOIN_WITH_COURSE_CHANGE;
import static mallang_trip.backend.controller.io.BaseResponseStatus.ALREADY_PARTY_MEMBER;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Bad_Request;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_CHANGE_COURSE;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_DRIVER;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_PARTY;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Conflict;
import static mallang_trip.backend.controller.io.BaseResponseStatus.EXCEED_PARTY_CAPACITY;
import static mallang_trip.backend.controller.io.BaseResponseStatus.EXPIRED_PROPOSAL;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.controller.io.BaseResponseStatus.NOT_PARTY_MEMBER;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;
import static mallang_trip.backend.controller.io.BaseResponseStatus.PARTY_CONFLICTED;
import static mallang_trip.backend.controller.io.BaseResponseStatus.PARTY_NOT_RECRUITING;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Unauthorized;

import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.constant.PartyStatus;
import mallang_trip.backend.constant.ProposalStatus;
import mallang_trip.backend.constant.ProposalType;
import mallang_trip.backend.constant.Role;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.course.CourseRequest;
import mallang_trip.backend.domain.dto.party.ChangeCourseRequest;
import mallang_trip.backend.domain.dto.party.CreatePartyRequest;
import mallang_trip.backend.domain.dto.party.JoinPartyRequest;
import mallang_trip.backend.domain.dto.party.PartyIdResponse;
import mallang_trip.backend.domain.entity.course.Course;
import mallang_trip.backend.domain.entity.driver.Driver;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.party.PartyMember;
import mallang_trip.backend.domain.entity.party.PartyProposal;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.driver.DriverRepository;
import mallang_trip.backend.repository.party.PartyMemberRepository;
import mallang_trip.backend.repository.party.PartyProposalRepository;
import mallang_trip.backend.repository.party.PartyRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyServiceV2 {

	private final UserService userService;
	private final PartyMemberService partyMemberService;
	private final PartyProposalService partyProposalService;
	private final DriverService driverService;
	private final CourseService courseService;
	private final DriverRepository driverRepository;
	private final PartyRepository partyRepository;
	private final PartyMemberRepository partyMemberRepository;
	private final PartyProposalRepository partyProposalRepository;

	/**
	 * 파티 생성 신청
	 */
	public PartyIdResponse createParty(CreatePartyRequest request) {
		Driver driver = driverRepository.findById(request.getDriverId())
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_DRIVER));
		User user = userService.getCurrentUser();
		// 드라이버가 가능한 시간인지 + 사용자가 당일 잡힌 파티가 있는지 CHECK
		String startDate = request.getStartDate().toString();
		if (!driverService.isDatePossible(driver, startDate)
			|| partyRepository.existsValidPartyByUserAndStartDate(user.getId(), startDate)) {
			throw new BaseException(PARTY_CONFLICTED);
		}
		// 코스 생성
		Course course = courseService.createCourse(request.getCourse());
		// 파티 생성
		Party party = partyRepository.save(Party.builder()
			.driver(driver)
			.course(course)
			.region(driver.getRegion())
			.capacity(driver.getVehicleCapacity())
			.headcount(request.getHeadcount())
			.startDate(request.getStartDate())
			.endDate(request.getEndDate())
			.content(request.getContent())
			.build());
		// 자신을 멤버로 추가
		partyMemberService.createMember(party, user, request.getHeadcount());
		return PartyIdResponse.builder().partyId(party.getId()).build();
	}

	/**
	 * (드라이버) 파티 생성 수락 or 거절
	 */
	public void acceptCreateParty(Long partyId, Boolean accept) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
		// 권한 CHECK
		if (!party.getDriver().getUser().equals(userService.getCurrentUser())) {
			throw new BaseException(Forbidden);
		}
		// STATUS CHECK
		if (!party.getStatus().equals(WAITING_DRIVER_APPROVAL)) {
			throw new BaseException(Bad_Request);
		}
		// STATUS 변경
		party.setStatus(accept ? RECRUITING : DRIVER_REFUSED);
	}

	/**
	 * 파티 가입 신청
	 */
	public void requestPartyJoin(Long partyId, JoinPartyRequest request) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
		// STATUS CHECK
		if (!party.getStatus().equals(RECRUITING)) {
			throw new BaseException(PARTY_NOT_RECRUITING);
		}
		// 인원수 CHECK
		if (request.getHeadcount() + party.getHeadcount() > party.getCapacity()) {
			throw new BaseException(EXCEED_PARTY_CAPACITY);
		}
		// 이미 가입된 파티인지 CHECK
		if (isMyParty(userService.getCurrentUser(), party)) {
			throw new BaseException(ALREADY_PARTY_MEMBER);
		}
		if (request.getChangeCourse()) {
			joinPartyWithCourseChange(party, request);
		} else {
			joinParty(party, userService.getCurrentUser(), request.getHeadcount());
		}
	}

	/**
	 * 코스 변경과 함께 파티 가입 신청
	 */
	private void joinPartyWithCourseChange(Party party, JoinPartyRequest request) {
		partyProposalService.createJoinWithCourseChange(party, request);
		party.setStatus(WAITING_JOIN_APPROVAL);
	}

	/**
	 * 파티 가입
	 */
	private void joinParty(Party party, User user, Integer headcount) {
		// 멤버 추가
		partyMemberService.createMember(party, user, headcount);
		// 가입으로 정원이 다 찼을 경우
		if (party.getHeadcount() == party.getCapacity()) {
			// TODO: 1/N원 자동결제

			// 전원 레디 처리
			partyMemberService.readyAllMembers(party);
			// status 변경
			party.setStatus(SEALED);
		} else {
			// 파티원 전원 레디 해제
			partyMemberService.cancelReadyAllMembers(party);
		}
	}

	/**
	 * 코스 변경 제안
	 */
	public void requestCourseChange(Long partyId, ChangeCourseRequest request) {
		User user = userService.getCurrentUser();
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
		// Party Status CHECK
		if (!party.getStatus().equals(SEALED)) {
			throw new BaseException(CANNOT_CHANGE_COURSE);
		}
		// 파티 멤버인지 CHECK
		if (!isMyParty(user, party)) {
			throw new BaseException(NOT_PARTY_MEMBER);
		}
		partyProposalService.createCourseChange(party, request);
		party.setStatus(WAITING_COURSE_CHANGE_APPROVAL);
	}

	/**
	 * 제안 취소
	 */
	public void cancelProposal(Long proposalId) {
		PartyProposal proposal = partyProposalRepository.findById(proposalId)
			.orElseThrow(() -> new BaseException(EXPIRED_PROPOSAL));
		partyProposalService.cancelProposal(proposal);
	}

	/**
	 * 제안 수락 or 거절
	 */
	public void voteProposal(Long proposalId, Boolean accept) {
		PartyProposal proposal = partyProposalRepository.findById(proposalId)
			.orElseThrow(() -> new BaseException(EXPIRED_PROPOSAL));
		partyProposalService.voteProposal(proposal, accept);
		if (accept) {
			acceptProposal(proposal);
		}
	}

	/**
	 * 제안 모두 수락했는지 확인 후 모두 수락했다면 제안 수용
	 */
	private void acceptProposal(PartyProposal proposal) {
		if (!partyProposalService.isUnanimity(proposal)) {
			return;
		}
		proposal.setStatus(ProposalStatus.ACCEPTED);
		Party party = proposal.getParty();
		party.setCourse(proposal.getCourse());
		if (proposal.getType().equals(JOIN_WITH_COURSE_CHANGE)) {
			joinParty(party, proposal.getProposer(), proposal.getHeadcount());
		}
		if (proposal.getType().equals(COURSE_CHANGE)) {
			proposal.getParty().setStatus(SEALED);
		}
	}

	/**
	 * 현재 유저 파티 레디
	 */
	public void ready(Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
		partyMemberService.ready(party);
		if (partyMemberService.isEveryoneReady(party)) {
			handleEveryoneReady(party);
		}
	}

	/**
	 * 파티 전원 레디 시 결제 후 SEALED 처리
	 */
	public void handleEveryoneReady(Party party) {
		// TODO: 1/N원 자동결제

		party.setStatus(SEALED);
	}

	/**
	 * 현재 유저 파티 레디 취소
	 */
	public void cancelReady(Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
		partyMemberService.cancelReady(party);
	}

	/**
	 * 유저가 속한 파티인지 아닌지 조회
	 */
	private Boolean isMyParty(User user, Party party) {
		if (user == null) {
			return false;
		}
		if (user.equals(party.getDriver().getUser())) {
			return true;
		}
		return partyMemberRepository.existsByPartyAndUser(party, user);
	}


}

package mallang_trip.backend.service.party;

import static mallang_trip.backend.constant.PartyStatus.CANCELED_BY_ALL_QUIT;
import static mallang_trip.backend.constant.PartyStatus.CANCELED_BY_DRIVER_QUIT;
import static mallang_trip.backend.constant.PartyStatus.CANCELED_BY_DRIVER_REFUSED;
import static mallang_trip.backend.constant.PartyStatus.CANCELED_BY_PROPOSER;
import static mallang_trip.backend.constant.PartyStatus.RECRUITING;
import static mallang_trip.backend.constant.PartyStatus.SEALED;
import static mallang_trip.backend.constant.PartyStatus.WAITING_COURSE_CHANGE_APPROVAL;
import static mallang_trip.backend.constant.PartyStatus.WAITING_DRIVER_APPROVAL;
import static mallang_trip.backend.constant.PartyStatus.WAITING_JOIN_APPROVAL;
import static mallang_trip.backend.constant.ProposalType.COURSE_CHANGE;
import static mallang_trip.backend.constant.ProposalType.JOIN_WITH_COURSE_CHANGE;
import static mallang_trip.backend.controller.io.BaseResponseStatus.ALREADY_PARTY_MEMBER;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_CHANGE_COURSE;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_DRIVER;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_PARTY;
import static mallang_trip.backend.controller.io.BaseResponseStatus.EXCEED_PARTY_CAPACITY;
import static mallang_trip.backend.controller.io.BaseResponseStatus.EXPIRED_PROPOSAL;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.controller.io.BaseResponseStatus.NOT_PARTY_MEMBER;
import static mallang_trip.backend.controller.io.BaseResponseStatus.PARTY_CONFLICTED;
import static mallang_trip.backend.controller.io.BaseResponseStatus.PARTY_NOT_RECRUITING;

import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.constant.PartyStatus;
import mallang_trip.backend.constant.ProposalStatus;
import mallang_trip.backend.constant.Role;
import mallang_trip.backend.controller.io.BaseException;
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
import mallang_trip.backend.service.CourseService;
import mallang_trip.backend.service.DriverService;
import mallang_trip.backend.service.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyService {

	private final UserService userService;
	private final PartyMemberService partyMemberService;
	private final PartyProposalService partyProposalService;
	private final DriverService driverService;
	private final CourseService courseService;
	private final ReservationService reservationService;
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
	 * 파티 생성 신청 취소
	 */
	private void cancelCreateParty(Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
		// 권한 CHECK
		if (!isMyParty(userService.getCurrentUser(), party)) {
			throw new BaseException(Forbidden);
		}
		// status CHECK
		if (!party.getStatus().equals(WAITING_DRIVER_APPROVAL)) {
			throw new BaseException(Forbidden);
		}
		party.setStatus(CANCELED_BY_PROPOSER);
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
			throw new BaseException(Forbidden);
		}
		// STATUS 변경
		party.setStatus(accept ? RECRUITING : CANCELED_BY_DRIVER_REFUSED);
	}

	/**
	 * 파티 가입 신청 :
	 * 코스 변경이 있으면, PartyProposal 생성.
	 * 코스 변경이 없으면, 바로 가입.
	 */
	public void requestPartyJoin(Long partyId, JoinPartyRequest request) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
		// STATUS CHECK
		if (!party.getStatus().equals(RECRUITING)) {
			throw new BaseException(PARTY_NOT_RECRUITING);
		}
		// 인원수 CHECK
		if (!party.isHeadcountAvailable(request.getHeadcount())) {
			throw new BaseException(EXCEED_PARTY_CAPACITY);
		}
		// 이미 가입된 파티인지 CHECK
		if (isMyParty(userService.getCurrentUser(), party)) {
			throw new BaseException(ALREADY_PARTY_MEMBER);
		}
		if (request.getChangeCourse()) {
			partyProposalService.createJoinWithCourseChange(party, request);
			party.setStatus(WAITING_JOIN_APPROVAL);
		} else {
			joinParty(party, userService.getCurrentUser(), request.getHeadcount());
		}
	}

	/**
	 * 파티 가입 (멤버 추가) :
	 * 최대인원 모집 완료 시 전원 레디처리, 자동결제 후 파티확정.
	 * 최대인원 모집 미완료 시, 전원 레디 취소 처리.
	 */
	private void joinParty(Party party, User user, Integer headcount) {
		partyMemberService.createMember(party, user, headcount);
		if (party.getHeadcount() == party.getCapacity()) {
			partyMemberService.setReadyAllMembers(party, true);
			reservationService.reserveParty(party);
			party.setStatus(SEALED);
		} else {
			partyMemberService.setReadyAllMembers(party, false);
			party.setStatus(RECRUITING);
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
	 * 제안 수락 or 거절 투표
	 * 수락 시, 만장일치가 이루어졌는지 확인.
	 * 거절 시, 제안 거절 처리.
	 */
	public void voteProposal(Long proposalId, Boolean accept) {
		PartyProposal proposal = partyProposalRepository.findById(proposalId)
			.orElseThrow(() -> new BaseException(EXPIRED_PROPOSAL));
		partyProposalService.voteProposal(proposal, accept);
		if (accept) {
			checkUnanimityAndAcceptProposal(proposal);
		}
	}

	/**
	 * 제안 모두 수락했는지 확인 후, 모두 수락했다면 제안 수용.
	 */
	private void checkUnanimityAndAcceptProposal(PartyProposal proposal) {
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
			party.setStatus(SEALED);
		}
	}

	/**
	 * 파티 레디 or 레디 취소.
	 * 전원 레디 시, 예약 진행.
	 */
	public void setReady(Long partyId, Boolean ready) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
		// status CHECK
		if (!party.getStatus().equals(RECRUITING)) {
			throw new BaseException(Forbidden);
		}
		partyMemberService.setReady(party, ready);
		if(ready){
			checkEveryoneReady(party);
		}
	}

	/**
	 * 파티 전원 레디 시, 자동결제 후 파티확정.
	 */
	public void checkEveryoneReady(Party party) {
		if (!partyMemberService.isEveryoneReady(party)) {
			return;
		}
		reservationService.reserveParty(party);
		party.setStatus(SEALED);
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

	/**
	 * 예약 전(RECRUITING, WAITING_JOIN_APPROVAL) 파티 탈퇴
	 */
	public void quitPartyBeforeReservation(Long partyId){
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
		// status CHECK
		PartyStatus status = party.getStatus();
		if(!(status.equals(RECRUITING) || status.equals(WAITING_JOIN_APPROVAL))){
			throw new BaseException(Forbidden);
		}
		// ROLE CHECK
		Role role = userService.getCurrentUser().getRole();
		if (role.equals(Role.ROLE_DRIVER)) {
			quitPartyBeforeReservationByDriver(party);
		} else if (role.equals(Role.ROLE_USER)) {
			quitPartyBeforeReservationByMember(party);
		}
	}

	/**
	 * (드라이버) 예약 전(RECRUITING, WAITING_JOIN_APPROVAL) 파티 탈퇴
	 */
	private void quitPartyBeforeReservationByDriver(Party party){
		Driver driver = driverService.getCurrentDriver();
		// 권한 CHECK
		if(!party.getDriver().equals(driver)){
			throw new BaseException(NOT_PARTY_MEMBER);
		}
		// 진행중인 가입 신청이 있을 경우, 거절 처리
		partyProposalService.expireWaitingProposalByParty(party);
		party.setStatus(CANCELED_BY_DRIVER_QUIT);
	}

	/**
	 * (멤버) 예약 전(RECRUITING, WAITING_JOIN_APPROVAL) 파티 탈퇴.
	 */
	private void quitPartyBeforeReservationByMember(Party party){
		// 권한 CHECK
		if(!isMyParty(userService.getCurrentUser(), party)){
			throw new BaseException(NOT_PARTY_MEMBER);
		}
		// 탈퇴 진행
		if(partyMemberService.isLastMember(party)){
			quitPartyBeforeReservationByLastMember(party);
		} else {
			quitPartyBeforeReservationByNotLastMember(party);
		}
	}

	/**
	 * 예약 전 파티 탈퇴 시, 마지막 멤버일 경우.
	 * 진행 중인 가입 신청이 있다면, 해당 proposal 만료 처리.
	 * CANCELED 상태로 변경 후, 마지막 멤버 정보는 delete 하지 않움.
	 */
	private void quitPartyBeforeReservationByLastMember(Party party){
		partyProposalService.expireWaitingProposalByParty(party);
		party.setStatus(CANCELED_BY_ALL_QUIT);
	}

	/**
	 * 예약 전 파티 탈퇴 시, 마지막 멤버가 아닐 경우.
	 * 진행 중인 가입 신청이 있다면, 해당 party_proposal_agreement 삭제 후 만장일치 확인.
	 */
	private void quitPartyBeforeReservationByNotLastMember(Party party){
		PartyProposal proposal = partyProposalRepository.findByPartyAndStatus(party,
			ProposalStatus.WAITING).orElse(null);
		PartyMember member = partyMemberRepository.findByPartyAndUser(party,
			userService.getCurrentUser()).orElseThrow(() -> new BaseException(NOT_PARTY_MEMBER));
		if (proposal != null) {
			partyProposalService.deleteAgreement(proposal, member);
			checkUnanimityAndAcceptProposal(proposal);
		}
		partyMemberService.deleteMemberAndDecreaseHeadcount(party, member);
	}

	/**
	 * (드라이버) 예약 취소
	 */


	/**
	 * (멤버) 예약 취소
	 */
	public void cancelReservationByMember(Long partyId){
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
		PartyMember member = partyMemberRepository.findByPartyAndUser(party, userService.getCurrentUser())
			.orElseThrow(() -> new BaseException(NOT_PARTY_MEMBER));
		// status CHECK
		PartyStatus status = party.getStatus();
		if(!(status.equals(SEALED) || status.equals(WAITING_COURSE_CHANGE_APPROVAL))){
			throw new BaseException(Forbidden);
		}
		// 진행중인 코스 변경 신청이 있을 경우 제안 종료
		if(status.equals(WAITING_COURSE_CHANGE_APPROVAL)){
			partyProposalService.expireWaitingProposalByParty(party);
		}
		// 환불 및 탈퇴 진행
		if(partyMemberService.isLastMember(party)){
			cancelReservationByLastMember(member);
		} else {
			cancelReservationByNotLastMember(member);
		}
	}

	/**
	 * 예약 취소 시, 마지막 멤버인 경우.
	 */
	private void cancelReservationByLastMember(PartyMember member){
		reservationService.refund(member);
		member.getParty().setStatus(CANCELED_BY_ALL_QUIT);
	}

	/**
	 * 예약 취소 시, 마지막 멤버가 아닌 경우.
	 * 환불 및 멤버 삭제 후, RECRUITING 상태로 돌아감.
	 * 환불 위약금이 100%일 때는 SEALED 상태 유지.
	 */
	private void cancelReservationByNotLastMember(PartyMember member){
		Party party = member.getParty();
		int refundAmount = reservationService.refund(member);
		partyMemberService.deleteMemberAndDecreaseHeadcount(party, member);
		if(refundAmount != 0){
			party.getCourse().discountPrice(refundAmount);
			party.setStatus(RECRUITING);
		} else {
			party.setStatus(SEALED);
		}
	}
}

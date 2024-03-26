package mallang_trip.backend.domain.party.service;

import static mallang_trip.backend.domain.admin.exception.AdminExceptionStatus.SUSPENDING;
import static mallang_trip.backend.domain.driver.exception.DriverExceptionStatus.CANNOT_FOUND_DRIVER;
import static mallang_trip.backend.domain.party.constant.PartyStatus.CANCELED_BY_ALL_QUIT;
import static mallang_trip.backend.domain.party.constant.PartyStatus.CANCELED_BY_DRIVER_QUIT;
import static mallang_trip.backend.domain.party.constant.PartyStatus.CANCELED_BY_DRIVER_REFUSED;
import static mallang_trip.backend.domain.party.constant.PartyStatus.CANCELED_BY_PROPOSER;
import static mallang_trip.backend.domain.party.constant.PartyStatus.RECRUITING;
import static mallang_trip.backend.domain.party.constant.PartyStatus.SEALED;
import static mallang_trip.backend.domain.party.constant.PartyStatus.WAITING_COURSE_CHANGE_APPROVAL;
import static mallang_trip.backend.domain.party.constant.PartyStatus.WAITING_DRIVER_APPROVAL;
import static mallang_trip.backend.domain.party.constant.PartyStatus.WAITING_JOIN_APPROVAL;
import static mallang_trip.backend.domain.party.constant.ProposalType.COURSE_CHANGE;
import static mallang_trip.backend.domain.party.constant.ProposalType.JOIN_WITH_COURSE_CHANGE;
import static mallang_trip.backend.global.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.domain.party.exception.PartyExceptionStatus.ALREADY_PARTY_MEMBER;
import static mallang_trip.backend.domain.party.exception.PartyExceptionStatus.CANNOT_CHANGE_COURSE;
import static mallang_trip.backend.domain.party.exception.PartyExceptionStatus.CANNOT_FOUND_PARTY;
import static mallang_trip.backend.domain.party.exception.PartyExceptionStatus.EXCEED_PARTY_CAPACITY;
import static mallang_trip.backend.domain.party.exception.PartyExceptionStatus.EXPIRED_PROPOSAL;
import static mallang_trip.backend.domain.party.exception.PartyExceptionStatus.NOT_PARTY_MEMBER;
import static mallang_trip.backend.domain.party.exception.PartyExceptionStatus.PARTY_CONFLICTED;
import static mallang_trip.backend.domain.party.exception.PartyExceptionStatus.PARTY_NOT_RECRUITING;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.gmail.service.GmailService;
import mallang_trip.backend.domain.party.constant.PartyStatus;
import mallang_trip.backend.domain.party.constant.ProposalStatus;
import mallang_trip.backend.domain.user.constant.Role;
import mallang_trip.backend.domain.user.service.CurrentUserService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.party.dto.ChangeCourseRequest;
import mallang_trip.backend.domain.party.dto.CreatePartyRequest;
import mallang_trip.backend.domain.party.dto.JoinPartyRequest;
import mallang_trip.backend.domain.party.dto.PartyIdResponse;
import mallang_trip.backend.domain.party.dto.PartyMemberCompanionRequest;
import mallang_trip.backend.domain.course.entity.Course;
import mallang_trip.backend.domain.driver.entity.Driver;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.party.entity.PartyMember;
import mallang_trip.backend.domain.party.entity.PartyProposal;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.driver.repository.DriverRepository;
import mallang_trip.backend.domain.course.service.CourseService;
import mallang_trip.backend.domain.admin.service.SuspensionService;
import mallang_trip.backend.domain.driver.service.DriverService;
import mallang_trip.backend.domain.party.repository.PartyMemberCompanionRepository;
import mallang_trip.backend.domain.party.repository.PartyMemberRepository;
import mallang_trip.backend.domain.party.repository.PartyProposalRepository;
import mallang_trip.backend.domain.party.repository.PartyRepository;
import mallang_trip.backend.domain.reservation.service.ReservationService;
import mallang_trip.backend.domain.chat.service.ChatService;
import org.springframework.stereotype.Service;
import static java.time.temporal.ChronoUnit.DAYS;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyService {

    private final CurrentUserService currentUserService;
    private final PartyMemberService partyMemberService;
    private final PartyProposalService partyProposalService;
    private final DriverService driverService;
    private final CourseService courseService;
    private final ReservationService reservationService;
    private final ChatService chatService;
    private final SuspensionService suspensionService;
    private final PartyNotificationService partyNotificationService;
    private final DriverRepository driverRepository;
    private final PartyRepository partyRepository;
    private final PartyMemberRepository partyMemberRepository;
    private final PartyMemberCompanionRepository partyMemberCompanionRepository;
    private final PartyProposalRepository partyProposalRepository;

    private final GmailService gmailService;

    /**
     * 파티 생성 신청
     */
    public PartyIdResponse createParty(CreatePartyRequest request) {
        Driver driver = driverRepository.findById(request.getDriverId())
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_DRIVER));
        User user = currentUserService.getCurrentUser();
        if (suspensionService.isSuspending(user)) {
            throw new BaseException(SUSPENDING);
        }
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
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .content(request.getContent())
            .build());
        // 자신을 멤버로 추가
        partyMemberService.createMember(party, user, request.getHeadcount(),
            request.getCompanions());
        // 드라이버 파티 신청 알림
        partyNotificationService.newParty(driver.getUser(), party);
        return PartyIdResponse.builder().partyId(party.getId()).build();
    }

    /**
     * 파티 생성 신청 취소
     */
    public void cancelCreateParty(Long partyId) {
        Party party = partyRepository.findById(partyId)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
        // 권한 CHECK
        if (!isMyParty(currentUserService.getCurrentUser(), party)) {
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
        if (!party.getDriver().getUser().equals(currentUserService.getCurrentUser())) {
            throw new BaseException(Forbidden);
        }
        // STATUS CHECK
        if (!party.getStatus().equals(WAITING_DRIVER_APPROVAL)) {
            throw new BaseException(Forbidden);
        }
        // STATUS 변경 및 신청자에게 알림
        if (accept) {
            party.setStatus(RECRUITING);
            chatService.startPartyChat(party);
            partyNotificationService.creationAccepted(party);
        } else {
            party.setStatus(CANCELED_BY_DRIVER_REFUSED);
            partyNotificationService.creationRefused(party);
        }

    }

    /**
     * 파티 가입 신청 : 코스 변경이 있으면, PartyProposal 생성. 코스 변경이 없으면, 바로 가입.
     */
    public void requestPartyJoin(Long partyId, JoinPartyRequest request) {
        User user = currentUserService.getCurrentUser();
        Party party = partyRepository.findById(partyId)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
        if (suspensionService.isSuspending(user)) {
            throw new BaseException(SUSPENDING);
        }
        // STATUS CHECK
        if (!party.getStatus().equals(RECRUITING)) {
            throw new BaseException(PARTY_NOT_RECRUITING);
        }
        // 인원수 CHECK
        if (!party.isHeadcountAvailable(request.getHeadcount())) {
            throw new BaseException(EXCEED_PARTY_CAPACITY);
        }
        // 이미 가입된 파티인지 CHECK
        if (isMyParty(user, party)) {
            throw new BaseException(ALREADY_PARTY_MEMBER);
        }
        if (request.getChangeCourse()) {
            partyProposalService.createJoinWithCourseChange(party, request);
            party.setStatus(WAITING_JOIN_APPROVAL);
            partyNotificationService.newJoinRequest(party);
        } else {
            partyNotificationService.newMember(user, party);
            joinParty(party, user, request.getHeadcount(), request.getCompanions());
        }
    }

    /**
     * 파티 가입 (멤버 추가) : 최대인원 모집 완료 시 전원 레디처리, 자동결제 후 파티확정. 최대인원 모집 미완료 시, 전원 레디 취소 처리.
     */
    private void joinParty(Party party, User user, Integer headcount,
        List<PartyMemberCompanionRequest> requests) {
        partyMemberService.createMember(party, user, headcount, requests);
        if (party.getHeadcount() == party.getCapacity()) {
            partyMemberService.setReadyAllMembers(party, true);
            partyNotificationService.partyFulled(party);
            reservationService.reserveParty(party);
            party.setStatus(SEALED);
        } else {
            partyMemberService.setReadyAllMembers(party, false);
            party.setStatus(RECRUITING);
        }
        chatService.joinPartyChat(user, party);
    }

    /**
     * 코스 변경 제안
     */
    public void requestCourseChange(Long partyId, ChangeCourseRequest request) {
        User user = currentUserService.getCurrentUser();
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
        PartyProposal proposal = partyProposalService.createCourseChange(party, request);
        party.setStatus(WAITING_COURSE_CHANGE_APPROVAL);
        partyNotificationService.newCourseChange(proposal);
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
     * 제안 수락 or 거절 투표 수락 시, 만장일치가 이루어졌는지 확인. 거절 시, 제안 거절 처리.
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
        Party party = proposal.getParty();
        User proposer = proposal.getProposer();
        proposal.setStatus(ProposalStatus.ACCEPTED);
        party.setCourse(proposal.getCourse());
        if (proposal.getType().equals(JOIN_WITH_COURSE_CHANGE)) {
            List<PartyMemberCompanionRequest> companionRequests = partyMemberCompanionRepository.findByProposal(
                    proposal).stream().map(PartyMemberCompanionRequest::of)
                .collect(Collectors.toList());
            partyNotificationService.joinAcceptedAndCourseChanged(proposer, party);
            joinParty(party, proposer, proposal.getHeadcount(), companionRequests);
            partyNotificationService.joinAccepted(proposer, party);
        }
        if (proposal.getType().equals(COURSE_CHANGE)) {
            partyMemberService.setReadyAllMembers(party, false);
            party.setStatus(SEALED);
            partyNotificationService.courseChangeAccepted(proposal);
        }
    }

    /**
     * 파티 레디 or 레디 취소. 전원 레디 시, 예약 진행.
     */
    public void setReady(Long partyId, Boolean ready) {
        Party party = partyRepository.findById(partyId)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
        // status CHECK
        if (!party.getStatus().equals(RECRUITING)) {
            throw new BaseException(Forbidden);
        }
        partyMemberService.setReady(party, ready);
        if (ready) {
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
        partyNotificationService.allReady(party);
        reservationService.reserveParty(party);
        gmailService.sendEmailParty(party, true);
        party.setStatus(SEALED);
    }

    /**
     * 유저가 속한 파티인지 아닌지 조회
     */
    public Boolean isMyParty(User user, Party party) {
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
    public void quitPartyBeforeReservation(Long partyId) {
        Party party = partyRepository.findById(partyId)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
        // status CHECK
        PartyStatus status = party.getStatus();
        if (!(status.equals(RECRUITING) || status.equals(WAITING_JOIN_APPROVAL))) {
            throw new BaseException(Forbidden);
        }
        // ROLE CHECK
        Role role = currentUserService.getCurrentUser().getRole();
        if (role.equals(Role.ROLE_DRIVER)) {
            quitPartyBeforeReservationByDriver(party);
        } else if (role.equals(Role.ROLE_USER)) {
            quitPartyBeforeReservationByMember(party);
        }
        chatService.leavePrivateChatWhenLeavingParty(currentUserService.getCurrentUser(), party);
        partyMemberService.setReadyAllMembers(party, false);
    }

    /**
     * (드라이버) 예약 전(RECRUITING, WAITING_JOIN_APPROVAL) 파티 탈퇴
     */
    private void quitPartyBeforeReservationByDriver(Party party) {
        Driver driver = driverService.getCurrentDriver();
        // 권한 CHECK
        if (!party.getDriver().equals(driver)) {
            throw new BaseException(NOT_PARTY_MEMBER);
        }
        // 진행중인 가입 신청이 있을 경우, 거절 처리
        partyProposalService.expireWaitingProposalByParty(party);
        party.setStatus(CANCELED_BY_DRIVER_QUIT);
        // 드라이버 탈퇴로 인한 파티 취소 알림
        partyNotificationService.cancelByDriverQuit(party);
    }

    /**
     * (멤버) 예약 전(RECRUITING, WAITING_JOIN_APPROVAL) 파티 탈퇴.
     */
    private void quitPartyBeforeReservationByMember(Party party) {
        User user = currentUserService.getCurrentUser();
        // 권한 CHECK
        if (!isMyParty(user, party)) {
            throw new BaseException(NOT_PARTY_MEMBER);
        }
        // 탈퇴 진행
        if (partyMemberService.isLastMember(party)) {
            quitPartyBeforeReservationByLastMember(party);
        } else {
            quitPartyBeforeReservationByNotLastMember(party);
        }
    }

    /**
     * 예약 전 파티 탈퇴 시, 마지막 멤버일 경우. 진행 중인 가입 신청이 있다면, 해당 proposal 만료 처리. CANCELED 상태로 변경 후, 마지막 멤버 정보는
     * delete 하지 않움.
     */
    private void quitPartyBeforeReservationByLastMember(Party party) {
        partyProposalService.expireWaitingProposalByParty(party);
        party.setStatus(CANCELED_BY_ALL_QUIT);
        partyNotificationService.lastMemberQuit(currentUserService.getCurrentUser(), party);
        partyNotificationService.cancelByAllQuit(party);
    }

    /**
     * 예약 전 파티 탈퇴 시, 마지막 멤버가 아닐 경우. 진행 중인 가입 신청이 있다면, 해당 party_proposal_agreement 삭제 후 만장일치 확인.
     */
    private void quitPartyBeforeReservationByNotLastMember(Party party) {
        User user = currentUserService.getCurrentUser();
        PartyProposal proposal = partyProposalRepository.findByPartyAndStatus(party,
            ProposalStatus.WAITING).orElse(null);
        PartyMember member = partyMemberRepository.findByPartyAndUser(party, user)
            .orElseThrow(() -> new BaseException(NOT_PARTY_MEMBER));
        if (proposal != null) {
            partyProposalService.deleteAgreement(proposal, member);
            checkUnanimityAndAcceptProposal(proposal);
        }
        partyMemberService.deleteMemberAndDecreaseHeadcount(party, member);
        partyNotificationService.memberQuit(user, party);
    }

    /**
     * 예약 취소 (SEALED, WAITING_COURSE_CHANGE_APPROVAL 상태)
     */
    public void cancelReservation(Long partyId) {
        Party party = partyRepository.findById(partyId)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
        // 권한 CHECK
        if (!isMyParty(currentUserService.getCurrentUser(), party)) {
            throw new BaseException(NOT_PARTY_MEMBER);
        }
        // status CHECK
        PartyStatus status = party.getStatus();
        if (!(status.equals(SEALED) || status.equals(WAITING_COURSE_CHANGE_APPROVAL))) {
            throw new BaseException(Forbidden);
        }
        // 진행중인 코스 변경 신청이 있을 경우 제안 종료
        if (status.equals(WAITING_COURSE_CHANGE_APPROVAL)) {
            partyProposalService.expireWaitingProposalByParty(party);
        }
        // ROLE CHECK
        Role role = currentUserService.getCurrentUser().getRole();
        if (role.equals(Role.ROLE_DRIVER)) {
            cancelReservationByDriver(party);
        } else if (role.equals(Role.ROLE_USER)) {
            cancelReservationByMember(party);
        }
        chatService.leavePrivateChatWhenLeavingParty(currentUserService.getCurrentUser(), party);
        partyMemberService.setReadyAllMembers(party, false);
        gmailService.sendEmailParty(party, false);
    }

    /**
     * (드라이버) 예약 취소
     */
    public void cancelReservationByDriver(Party party) {
        partyNotificationService.cancelReservationByDriver(party);
        reservationService.savePenaltyToDriver(party);
        reservationService.refundAllMembers(party);
        party.setStatus(CANCELED_BY_DRIVER_QUIT);
    }

    /**
     * (멤버) 예약 취소
     */
    public void cancelReservationByMember(Party party) {
        PartyMember member = partyMemberRepository.findByPartyAndUser(party,
                currentUserService.getCurrentUser())
            .orElseThrow(() -> new BaseException(NOT_PARTY_MEMBER));
        if (partyMemberService.isLastMember(party)) {
            cancelReservationByLastMember(member);
        } else {
            cancelReservationByNotLastMember(member);
        }
    }

    /**
     * (멤버) 예약 취소 시, 마지막 멤버인 경우.
     */
    private void cancelReservationByLastMember(PartyMember member) {
        partyNotificationService.cancelReservationByLastMember(member.getUser(), member.getParty());
        Integer penaltyAmount = reservationService.refund(member);
        member.getParty().getCourse().increaseDiscountPrice(penaltyAmount);
        member.getParty().setStatus(CANCELED_BY_ALL_QUIT);
        // TODO: 위약금 드라이버에게 송금 후 알림
    }

    /**
     * (멤버) 예약 취소 시, 마지막 멤버가 아닌 경우. 1. 위약금이 발생하지 않은 경우 2. 전액 위약금이 발생한 경우 3. 위약금이 일부 발생한 경우
     */
    private void cancelReservationByNotLastMember(PartyMember member) {
        Party party = member.getParty();
        long dDay = DAYS.between(LocalDate.now(), party.getStartDate());
        // 취소자 환불 진행
        Integer penaltyAmount = reservationService.refund(member);
        // 멤버 삭제
        partyMemberService.deleteMemberAndDecreaseHeadcount(party, member);
        if (dDay > 7) { // 위약금이 발생하지 않은 경우
            partyNotificationService.cancelReservation(member.getUser(), party);
            reservationService.refundAllMembers(party);
            party.setStatus(RECRUITING);
        } else if (dDay < 3) { // 전액 위약금이 발생한 경우
            partyNotificationService.cancelReservationWithFullPenalty(member.getUser(), party);
            party.setStatus(SEALED);
        } else { // 일부 위약금이 발생한 경우
            partyNotificationService.cancelReservation(member.getUser(), party);
            reservationService.refundAllMembers(party);
            party.setStatus(RECRUITING);
            party.getCourse().increaseDiscountPrice(penaltyAmount);
        }
    }
}

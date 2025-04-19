package mallang_trip.backend.domain.party.service;

import static mallang_trip.backend.domain.admin.exception.AdminExceptionStatus.SUSPENDING;
import static mallang_trip.backend.domain.driver.exception.DriverExceptionStatus.CANNOT_FOUND_DRIVER;
import static mallang_trip.backend.domain.kakao.constant.AlimTalkTemplate.DRIVER_COURSE_CHANGE;
import static mallang_trip.backend.domain.kakao.constant.AlimTalkTemplate.DRIVER_NEW_PARTY;
import static mallang_trip.backend.domain.kakao.constant.AlimTalkTemplate.DRIVER_RESERVATION_CANCELED;
import static mallang_trip.backend.domain.kakao.constant.AlimTalkTemplate.DRIVER_RESERVATION_CONFIRM;
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
import static mallang_trip.backend.domain.party.exception.PartyExceptionStatus.*;
import static mallang_trip.backend.domain.reservation.constant.UserPromotionCodeStatus.CANCEL;
import static mallang_trip.backend.domain.reservation.constant.UserPromotionCodeStatus.USE;
import static mallang_trip.backend.global.io.BaseResponseStatus.Forbidden;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.income.service.IncomeService;
import mallang_trip.backend.domain.kakao.service.AlimTalkService;
import mallang_trip.backend.domain.mail.constant.MailStatus;
import mallang_trip.backend.domain.mail.service.MailService;
import mallang_trip.backend.domain.party.constant.PartyStatus;
import mallang_trip.backend.domain.party.constant.PartyType;
import mallang_trip.backend.domain.party.constant.ProposalStatus;
import mallang_trip.backend.domain.party.repository.*;
import mallang_trip.backend.domain.reservation.entity.UserPromotionCode;
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
    private final MailService mailService;
    private final IncomeService incomeService;
    private final AlimTalkService alimTalkService;
    private final String MallangTripReservationUrl = "https://mallangtrip.com/my/reservation";

    /**
     * 파티 생성 신청
     * @param request 파티 생성dto
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


        /*단독예약 여부 확인(모집인원 = 현재 인원)*/
        PartyType partyType = PartyType.PUBLIC;// 기본은 단독예약X
        if(request.getHeadcount() == request.getCourse().getCapacity()) {
            partyType = PartyType.PRIVATE;
        }

        // 파티 생성
        Party party = partyRepository.save(Party.builder()
            .driver(driver)
            .course(course)
            .region(course.getRegion())
            .capacity(request.getCourse().getCapacity())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .content(request.getContent())
            .monopoly(request.getMonopoly())
            .partyType(partyType)
            .build());

        // 자신을 멤버로 추가
        partyMemberService.createMember(party, user, request.getHeadcount(),
            request.getCompanions(),request.getUserPromotionCodeId());
        // 드라이버 파티 신청 알림
        partyNotificationService.newParty(driver.getUser(), party);
        alimTalkService.sendDriverAlimTalk(DRIVER_NEW_PARTY, party);
        /**
         * TODO: 알림 메시지 전송
         * 기사님: 승객 정보
         * 승객: 기사님 정보
         * 푸시알림 외에 카톡알림, 메일알림 3중으로 동작하도록 수정
         * */
        alimTalkService.sendUserAlimTalk(user, party);

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
        PartyMember member = partyMemberRepository.findByPartyAndUser(party, currentUserService.getCurrentUser())
            .orElseThrow(() -> new BaseException(NOT_PARTY_MEMBER));

        UserPromotionCode userPromotionCode = member.getUserPromotionCode();
        if (userPromotionCode != null && userPromotionCode.getStatus().equals(USE))
        {
            userPromotionCode.changeStatus(CANCEL);
            userPromotionCode.getCode().cancel();
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
        // headcount == capacity 인 경우
        if (party.getHeadcount() == party.getCapacity()) {
            partyMemberService.setReadyAllMembers(party, true);
            partyNotificationService.partyFulled(party);
            reservationService.reserveParty(party);
            party.setStatus(SEALED);
            mailService.sendEmailParty(party, MailStatus.SEALED,null,MallangTripReservationUrl);
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
        if (!party.getStatus().equals(RECRUITING) || party.getMonopoly()) {
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
            alimTalkService.sendDriverAlimTalk(DRIVER_COURSE_CHANGE, party);
        } else {
            partyNotificationService.newMember(user, party);
            joinParty(party, user, request.getHeadcount(), request.getCompanions(),request.getUserPromotionCodeId());
        }
    }

    /**
     * 파티 가입 (멤버 추가) : 최대인원 모집 완료 시 전원 레디처리, 자동결제 후 파티확정. 최대인원 모집 미완료 시, 전원 레디 취소 처리.
     */
    private void joinParty(Party party, User user, Integer headcount,
        List<PartyMemberCompanionRequest> requests,Long userPromotionCodeId) {
        partyMemberService.createMember(party, user, headcount, requests,userPromotionCodeId);
        if (party.getHeadcount() == party.getCapacity()) {
            partyMemberService.setReadyAllMembers(party, true);
            partyNotificationService.partyFulled(party);
            reservationService.reserveParty(party);
            party.setStatus(SEALED);
            mailService.sendEmailParty(party, MailStatus.SEALED,null,MallangTripReservationUrl);
            alimTalkService.sendDriverAlimTalk(DRIVER_RESERVATION_CONFIRM, party);
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
        // 프로모션 코드 사용 여부 확인
        PartyMember member = partyMemberRepository.findByPartyAndUser(party, user)
            .orElseThrow(() -> new BaseException(NOT_PARTY_MEMBER));

        List<PartyMember> members = partyMemberRepository.findByParty(party);
        members.forEach(m->{
            if(m.getUserPromotionCode() != null && m.getUserPromotionCode().getStatus().equals(USE)){
                if(m.getUserPromotionCode().getCode().getMaximumPrice() < request.getCourse().getTotalPrice()){
                    throw new BaseException(CANNOT_CHANGE_COURSE_PROMOTION_CODE);
                }
            }
        });
        PartyProposal proposal = partyProposalService.createCourseChange(party, request,member.getUserPromotionCode());

        party.setStatus(WAITING_COURSE_CHANGE_APPROVAL);
        partyNotificationService.newCourseChange(proposal);
        alimTalkService.sendDriverAlimTalk(DRIVER_COURSE_CHANGE, party);
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
        proposal.getCourse().increaseDiscountPrice(party.getCourse().getDiscountPrice());
        party.setCourse(proposal.getCourse());
        if (proposal.getType().equals(JOIN_WITH_COURSE_CHANGE)) {
            List<PartyMemberCompanionRequest> companionRequests = partyMemberCompanionRepository.findByProposal(
                    proposal).stream().map(PartyMemberCompanionRequest::of)
                .collect(Collectors.toList());
            partyNotificationService.joinAcceptedAndCourseChanged(proposer, party);
            joinParty(party, proposer, proposal.getHeadcount(), companionRequests,proposal.getUserPromotionCode() == null ? -1 : proposal.getUserPromotionCode().getId());
            partyNotificationService.joinAccepted(proposer, party);
            mailService.sendEmailParty(party, MailStatus.MODIFIED_JOIN,"새 파티원의 코스 변경 신청을 모두 수락하였습니다.",MallangTripReservationUrl);
        }
        if (proposal.getType().equals(COURSE_CHANGE)) {
            partyMemberService.setReadyAllMembers(party, false);
            party.setStatus(SEALED);
            partyNotificationService.courseChangeAccepted(proposal);
            mailService.sendEmailParty(party, MailStatus.MODIFIED,"코스 변경을 모두 수락했습니다.",MallangTripReservationUrl);
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
        boolean success = partyMemberService.setReady(party, ready);
        if (ready && success) {
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
        mailService.sendEmailParty(party, MailStatus.SEALED,null,MallangTripReservationUrl);
        alimTalkService.sendDriverAlimTalk(DRIVER_RESERVATION_CONFIRM, party);
        party.setStatus(SEALED);
        partyNotificationService.dayBeforeTravelToMembers(party);
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
        // 위약금 수익 등록
        incomeService.createPenaltyIncome(party);
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
        PartyMember partyMember = partyMemberRepository.findByPartyAndUser(party, user)
            .orElseThrow(() -> new BaseException(NOT_PARTY_MEMBER));
        UserPromotionCode userPromotionCode = partyMember.getUserPromotionCode();
        if (userPromotionCode != null && userPromotionCode.getStatus().equals(USE))
        {
            userPromotionCode.changeStatus(CANCEL);
            userPromotionCode.getCode().cancel();
        }

        if (partyMemberService.isLastMember(party)) {
            quitPartyBeforeReservationByLastMember(party);
        } else {
            quitPartyBeforeReservationByNotLastMember(party);
        }
        // 모든 멤버 레디 해제
        partyMemberService.setReadyAllMembers(party, false);
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
        // 위약금 수익 등록
        incomeService.createPenaltyIncome(party);
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
        StringBuilder reason = new StringBuilder();
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
            reason.append("드라이버가 예약을 취소했습니다.");
        } else if (role.equals(Role.ROLE_USER)) {
            cancelReservationByMember(party);
            reason.append("파티원이 예약을 취소했습니다.");
        }
        chatService.leavePrivateChatWhenLeavingParty(currentUserService.getCurrentUser(), party);
        partyMemberService.setReadyAllMembers(party, false);
        mailService.sendEmailParty(party, MailStatus.CANCELLED,reason.toString(),MallangTripReservationUrl);
        alimTalkService.sendDriverAlimTalk(DRIVER_RESERVATION_CANCELED, party);
    }

    /**
     * (드라이버) 예약 취소
     */
    public void cancelReservationByDriver(Party party) {
        partyNotificationService.cancelReservationByDriver(party);
        reservationService.savePenaltyToDriver(party);
        reservationService.refundAllMembers(party);
        party.setStatus(CANCELED_BY_DRIVER_QUIT);
        // 위약금 수익 등록
        incomeService.createPenaltyIncome(party);
    }

    /**
     * (멤버) 예약 취소
     */
    public void cancelReservationByMember(Party party) {
        PartyMember member = partyMemberRepository.findByPartyAndUser(party,
                currentUserService.getCurrentUser())
            .orElseThrow(() -> new BaseException(NOT_PARTY_MEMBER));

        UserPromotionCode userPromotionCode = member.getUserPromotionCode();
        if (userPromotionCode != null && userPromotionCode.getStatus().equals(USE))
        {
            userPromotionCode.changeStatus(CANCEL);
            userPromotionCode.getCode().cancel();
        }

        if (partyMemberService.isLastMember(party)) {
            cancelReservationByLastMember(member);
        } else {
            cancelReservationByNotLastMember(member);
        }
        // 모든 멤버 레디 해제
        partyMemberService.setReadyAllMembers(party, false);
    }

    /**
     * (멤버) 예약 취소 시, 마지막 멤버인 경우.
     */
    private void cancelReservationByLastMember(PartyMember member) {
        partyNotificationService.cancelReservationByLastMember(member.getUser(), member.getParty());
        Integer penaltyAmount = reservationService.refund(member);
        member.getParty().getCourse().increaseDiscountPrice(penaltyAmount);
        member.getParty().setStatus(CANCELED_BY_ALL_QUIT);
        // 위약금 수익 등록
        incomeService.createPenaltyIncome(member.getParty());
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
        if (dDay > 7 || penaltyAmount==0) { // 위약금이 발생하지 않은 경우 + 0원 환불
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

    /**
     * (관리자) 드라이버 레디 조작
     */
    public void changeDriverReady(Long partyId, boolean ready){
        Party party = partyRepository.findById(partyId)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));

        if(ready){
            party.setDriverReady(true);
        } else {
            party.setDriverReady(false);
        }
    }

    /**
     * (관리자) 파티 취소 멤버용
     */
    public void cancelReservationMember(Party party) {
        List<PartyMember> members = partyMemberRepository.findByParty(party);

        members.forEach(member->{
            UserPromotionCode userPromotionCode = member.getUserPromotionCode();
            if (userPromotionCode != null && userPromotionCode.getStatus().equals(USE))
            {
                userPromotionCode.changeStatus(CANCEL);
                userPromotionCode.getCode().cancel();
            }

            if (partyMemberService.isLastMember(party)) {
                cancelReservationByLastMember(member);
            } else {
                cancelReservationByNotLastMember(member);
            }
        });

        // 모든 멤버 레디 해제
        partyMemberService.setReadyAllMembers(party, false);
    }

    /**
     * (관리자) 파티 취소
     *
     */
    public void cancelParty(Long partyId, String reason) {

        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
        // status CHECK
        PartyStatus status = party.getStatus();
        if (!(status.equals(SEALED) || status.equals(WAITING_COURSE_CHANGE_APPROVAL))) {
            throw new BaseException(Forbidden);
        }
        // 진행중인 코스 변경 신청이 있을 경우 제안 종료
        if (status.equals(WAITING_COURSE_CHANGE_APPROVAL)) {
            partyProposalService.expireWaitingProposalByParty(party);
        }
        //cancelReservationByDriver(party);
        cancelReservationMember(party);
        chatService.leavePrivateChatWhenLeavingParty(currentUserService.getCurrentUser(), party);
        partyMemberService.setReadyAllMembers(party, false);
        mailService.sendEmailParty(party, MailStatus.CANCELLED, reason, MallangTripReservationUrl);
        alimTalkService.sendDriverAlimTalk(DRIVER_RESERVATION_CANCELED, party);
    }

}

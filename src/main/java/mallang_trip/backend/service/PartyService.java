package mallang_trip.backend.service;

import static mallang_trip.backend.constant.AgreementStatus.ACCEPT;
import static mallang_trip.backend.constant.AgreementStatus.REFUSE;
import static mallang_trip.backend.constant.AgreementStatus.WAITING;
import static mallang_trip.backend.constant.PartyStatus.COURSE_CHANGE_APPROVAL_WAITING;
import static mallang_trip.backend.constant.PartyStatus.DRIVER_REFUSED;
import static mallang_trip.backend.constant.PartyStatus.JOIN_APPROVAL_WAITING;
import static mallang_trip.backend.constant.PartyStatus.RECRUITING;
import static mallang_trip.backend.constant.PartyStatus.RECRUIT_COMPLETED;
import static mallang_trip.backend.constant.ProposalStatus.ACCEPTED;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_USER;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;
import static mallang_trip.backend.controller.io.BaseResponseStatus.PARTY_NOT_RECRUITING;
import static mallang_trip.backend.controller.io.BaseResponseStatus.PROPOSAL_END;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Unauthorized;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.constant.AgreementStatus;
import mallang_trip.backend.constant.PartyStatus;
import mallang_trip.backend.constant.ProposalStatus;
import mallang_trip.backend.constant.ProposalType;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.Party.JoinPartyRequest;
import mallang_trip.backend.domain.dto.Party.PartyAgreementResponse;
import mallang_trip.backend.domain.dto.Party.PartyBriefResponse;
import mallang_trip.backend.domain.dto.Party.PartyDetailsResponse;
import mallang_trip.backend.domain.dto.Party.PartyIdResponse;
import mallang_trip.backend.domain.dto.Party.PartyMemberResponse;
import mallang_trip.backend.domain.dto.Party.PartyRequest;
import mallang_trip.backend.domain.dto.Party.ProposalResponse;
import mallang_trip.backend.domain.dto.course.CourseRequest;
import mallang_trip.backend.domain.entity.driver.Driver;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.domain.entity.course.Course;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.party.PartyAgreement;
import mallang_trip.backend.domain.entity.party.PartyMembers;
import mallang_trip.backend.domain.entity.party.PartyProposal;
import mallang_trip.backend.repository.driver.DriverRepository;
import mallang_trip.backend.repository.course.CourseRepository;
import mallang_trip.backend.repository.party.PartyAgreementRepository;
import mallang_trip.backend.repository.party.PartyMembersRepository;
import mallang_trip.backend.repository.party.PartyProposalRepository;
import mallang_trip.backend.repository.party.PartyRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyService {

    private final UserService userService;
    private final CourseService courseService;
    private final CourseRepository courseRepository;
    private final DriverRepository driverRepository;
    private final PartyRepository partyRepository;
    private final PartyMembersRepository partyMembersRepository;
    private final PartyProposalRepository partyProposalRepository;
    private final PartyAgreementRepository partyAgreementRepository;

    // 파티 생성 신청
    public PartyIdResponse createParty(PartyRequest request) {
        Driver driver = driverRepository.findById(request.getDriverId())
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
        // 코스 변경 유무 Check
        Course course;
        if (request.getChangeCourse()) {
            course = courseService.createCourse(request.getNewCourse());
        } else {
            course = courseService.copyCourse(
                courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new BaseException(Not_Found)));
        }

        Party party = partyRepository.save(Party.builder()
            .driver(driver)
            .course(course)
            .region(driver.getRegion())
            .capacity(driver.getVehicleCapacity())
            .headcount(request.getHeadcount())
            .startDate(LocalDate.parse(request.getStartDate()))
            .endDate(LocalDate.parse(request.getEndDate()))
            .build());

        partyMembersRepository.save(PartyMembers.builder()
            .party(party)
            .user(userService.getCurrentUser())
            .headcount(request.getHeadcount())
            .build());

        return PartyIdResponse.builder()
            .partyId(party.getId())
            .build();
    }

    // (드라이버) 파티 생성 수락 or 거절
    public void acceptCreateParty(Long partyId, Boolean accept) {
        Party party = partyRepository.findById(partyId)
            .orElseThrow(() -> new BaseException(Not_Found));
        if (!party.getDriver().getUser().equals(userService.getCurrentUser())) {
            throw new BaseException(Unauthorized);
        }
        if (accept) {
            party.setStatus(RECRUITING);
            // 알림 전송
        } else {
            party.setStatus(DRIVER_REFUSED);
            // 알림 전송
        }
    }

    public void joinParty(JoinPartyRequest request){
        // Exception Check (인원수 초과 check 추가 필요)
        Party party = partyRepository.findById(request.getPartyId())
            .orElseThrow(() -> new BaseException(Not_Found));
        if (!party.getStatus().equals(RECRUITING)) {
            throw new BaseException(PARTY_NOT_RECRUITING);
        }

        if(request.getChangeCourse()){
            joinPartyWithCourseChange(request, party);
        } else {
            joinPartyWithoutCourseChange(request, party);
        }
    }

    private void joinPartyWithoutCourseChange(JoinPartyRequest request, Party party) {
        User user = userService.getCurrentUser();
        PartyProposal proposal = partyProposalRepository.save(PartyProposal.builder()
            .course(party.getCourse())
            .party(party)
            .proposer(user)
            .headcount(request.getHeadcount())
            .content(request.getContent())
            .type(ProposalType.JOIN)
            .build());

        partyMembersRepository.findByParty(party)
            .forEach(member -> {
                partyAgreementRepository.save(PartyAgreement.builder()
                    .proposal(proposal)
                    .members(member)
                    .build());
            });

        party.setStatus(JOIN_APPROVAL_WAITING);
    }

    private void joinPartyWithCourseChange(JoinPartyRequest request, Party party) {
        User user = userService.getCurrentUser();
        Course course = courseService.createCourse(request.getNewCourse());

        PartyProposal proposal = partyProposalRepository.save(PartyProposal.builder()
            .course(course)
            .party(party)
            .proposer(user)
            .headcount(request.getHeadcount())
            .content(request.getContent())
            .type(ProposalType.JOIN_WITH_COURSE_CHANGE)
            .build());

        partyMembersRepository.findByParty(party)
            .forEach(member -> {
                partyAgreementRepository.save(PartyAgreement.builder()
                    .proposal(proposal)
                    .members(member)
                    .build());
            });

        party.setStatus(JOIN_APPROVAL_WAITING);
    }

    // 코스 변경 제안
    public void changeCourse(Long partyId, CourseRequest request) {
        User user = userService.getCurrentUser();
        Party party = partyRepository.findById(partyId)
            .orElseThrow(() -> new BaseException(Not_Found));
        Course course = courseService.createCourse(request);

        //
        // party status check
        //

        PartyProposal proposal = partyProposalRepository.save(PartyProposal.builder()
            .course(course)
            .party(party)
            .proposer(user)
            .type(ProposalType.COURSE_CHANGE)
            .build());

        partyMembersRepository.findByParty(party)
            .forEach(member -> {
                AgreementStatus status = WAITING;
                if (member.getUser().equals(user)) {
                    status = ACCEPT;
                }
                partyAgreementRepository.save(PartyAgreement.builder()
                    .proposal(proposal)
                    .members(member)
                    .status(status)
                    .build());
            });

        party.setStatus(PartyStatus.COURSE_CHANGE_APPROVAL_WAITING);
    }

    // 제안 수락 or 거절
    public void acceptProposal(Long agreementId, boolean accept) {
        PartyAgreement agreement = partyAgreementRepository.findById(agreementId)
            .orElseThrow(() -> new BaseException(Not_Found));
        PartyProposal proposal = agreement.getProposal();
        if (proposal.getType().equals(ProposalType.COURSE_CHANGE)) {
            acceptCourseChange(agreement, accept);
        } else {
            acceptPartyJoin(agreement, accept);
        }
    }

    // (드라이버) 제안 수락 or 거절
    public void acceptProposalByDriver(Long proposalId, boolean accept) {
        PartyProposal proposal = partyProposalRepository.findById(proposalId)
            .orElseThrow(() -> new BaseException(Not_Found));
        if (proposal.getType().equals(ProposalType.COURSE_CHANGE)) {
            acceptCourseChangeByDriver(proposal, accept);
        } else {
            acceptPartyJoinByDriver(proposal, accept);
        }
    }

    // 모집중인 파티 조회 By 지역, 인원수, 날짜
    public List<PartyBriefResponse> findParties(String region, Integer headcount,
        String startDate) {
        return partyRepository.findParties(region, headcount, LocalDate.parse(startDate))
            .stream()
            .map(PartyBriefResponse::of)
            .collect(Collectors.toList());
    }

    // 내 파티 목록 조회
    public List<PartyBriefResponse> getMyParties() {
        List<Party> parties = partyMembersRepository.findByUser(userService.getCurrentUser())
            .stream()
            .map(partyMembers -> partyMembers.getParty())
            .collect(Collectors.toList());
        return parties.stream().map(PartyBriefResponse::of).collect(Collectors.toList());
    }

    // 파티 상세조회
    public PartyDetailsResponse getPartyDetails(Long partyId) {
        Party party = partyRepository.findById(partyId)
            .orElseThrow(() -> new BaseException(Not_Found));
        PartyDetailsResponse response = PartyDetailsResponse.builder()
            .partyId(party.getId())
            .myParty(true)
            .partyStatus(party.getStatus())
            .driverId(party.getDriver().getId())
            .driverName(party.getDriver().getUser().getName())
            .capacity(party.getCapacity())
            .headcount(party.getHeadcount())
            .region(party.getRegion())
            .startDate(party.getStartDate())
            .endDate(party.getEndDate())
            .course(courseService.getCourseDetails(party.getCourse()))
            .content(party.getContent())
            .build();
        // 내가 속한 파티일 경우: 멤버, 제안 정보 추가
        if (isMyParty(party)) {
            List<PartyMemberResponse> members = partyMembersRepository.findByParty(party)
                .stream()
                .map(PartyMemberResponse::of)
                .collect(Collectors.toList());
            response.setMembers(members);
            response.setProposal(getProposalDetails(party));
        }
        return response;
    }

    // 내 제안 조회

    // (드라이버) 새 파티 신청 조회

    // (드라이버) 제안조회

    // 파티 나가기

    private void acceptPartyJoin(PartyAgreement agreement, boolean accept) {
        PartyProposal proposal = agreement.getProposal();
        PartyStatus partyStatus = proposal.getParty().getStatus();
        if (!partyStatus.equals(JOIN_APPROVAL_WAITING)) {
            throw new BaseException(PROPOSAL_END);
        }
        if (accept) {
            agreement.setStatus(ACCEPT);
            if (partyProposalRepository.isUnanimity(proposal.getId())) {
                addMember(proposal);
            }
        } else {
            refuseProposal(proposal);
        }
    }

    private void acceptCourseChange(PartyAgreement agreement, boolean accept) {
        PartyProposal proposal = agreement.getProposal();
        PartyStatus partyStatus = proposal.getParty().getStatus();
        if (!partyStatus.equals(COURSE_CHANGE_APPROVAL_WAITING)) {
            throw new BaseException(PROPOSAL_END);
        }
        if (accept) {
            agreement.setStatus(ACCEPT);
            if (partyProposalRepository.isUnanimity(proposal.getId())) {
                changeCourse(proposal);
            }
        } else {
            refuseProposal(proposal);
        }
    }

    private void acceptPartyJoinByDriver(PartyProposal proposal, boolean accept) {
        PartyStatus partyStatus = proposal.getParty().getStatus();
        if (!partyStatus.equals(JOIN_APPROVAL_WAITING)) {
            throw new BaseException(PROPOSAL_END);
        }
        if (accept) {
            proposal.setDriverAgreement(ACCEPT);
            if (partyProposalRepository.isUnanimity(proposal.getId())) {
                addMember(proposal);
            }
        } else {
            refuseProposal(proposal);
        }
    }

    private void acceptCourseChangeByDriver(PartyProposal proposal, boolean accept) {
        PartyStatus partyStatus = proposal.getParty().getStatus();
        if (!partyStatus.equals(COURSE_CHANGE_APPROVAL_WAITING)) {
            throw new BaseException(PROPOSAL_END);
        }
        if (accept) {
            proposal.setDriverAgreement(ACCEPT);
            if (partyProposalRepository.isUnanimity(proposal.getId())) {
                changeCourse(proposal);
            }
        } else {
            refuseProposal(proposal);
        }
    }

    // 가입 신청 모두 동의했을 때
    private void addMember(PartyProposal proposal) {
        Party party = proposal.getParty();
        // proposal status 변경
        proposal.setStatus(ACCEPTED);
        // 멤버 추가
        partyMembersRepository.save(PartyMembers.builder()
            .party(party)
            .user(proposal.getProposer())
            .headcount(proposal.getHeadcount())
            .build());
        // party headcount 추가
        party.setHeadcount(party.getHeadcount() + proposal.getHeadcount());
        // 코스 변경
        party.setCourse(proposal.getCourse());
        // Party status 변경
        if (party.getCapacity() == party.getHeadcount()) {
            party.setStatus(RECRUIT_COMPLETED);
        } else {
            party.setStatus(RECRUITING);
        }
    }

    // 코스 변경 모두 동의했을 때
    private void changeCourse(PartyProposal proposal) {
        Party party = proposal.getParty();

        proposal.setStatus(ACCEPTED);
        party.setCourse(proposal.getCourse());
        party.setStatus(RECRUITING);
    }

    // 제안 거절 시
    private void refuseProposal(PartyProposal proposal) {
        proposal.setStatus(ProposalStatus.REFUSED);
        proposal.getParty().setStatus(RECRUITING);
        partyAgreementRepository.deleteByProposal(proposal);
        //거절 알림 전송
    }

    private List<PartyAgreementResponse> getAgreement(PartyProposal proposal) {
        return partyAgreementRepository.findByProposal(proposal)
            .stream()
            .map(PartyAgreementResponse::of)
            .collect(Collectors.toList());
    }

    private ProposalResponse getProposalDetails(Party party) {
        PartyProposal proposal = partyProposalRepository.findWaitingProposal(party.getId());
        return ProposalResponse.builder()
            .proposalId(proposal.getId())
            .proposerId(proposal.getProposer().getId())
            .proposerNickname(proposal.getProposer().getNickname())
            .type(proposal.getType())
            .status(proposal.getStatus())
            .driverAgreement(proposal.getDriverAgreement())
            .course(courseService.getCourseDetails(proposal.getCourse()))
            .headcount(proposal.getHeadcount())
            .content(proposal.getContent())
            .memberAgreement(getAgreement(proposal))
            .build();
    }

    private Boolean isMyParty(Party party) {
        if (userService.getCurrentUser().equals(party.getDriver().getUser())) {
            return true;
        }
        return partyMembersRepository.existsByPartyAndUser(party, userService.getCurrentUser());
    }
}

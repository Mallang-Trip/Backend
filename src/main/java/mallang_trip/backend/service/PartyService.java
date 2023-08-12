package mallang_trip.backend.service;

import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;

import java.util.List;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.constant.AgreementStatus;
import mallang_trip.backend.constant.PartyStatus;
import mallang_trip.backend.constant.ProposalStatus;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.Party.JoinPartyRequest;
import mallang_trip.backend.domain.dto.Party.PartyIdResponse;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.domain.entity.course.Course;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.party.PartyAgreement;
import mallang_trip.backend.domain.entity.party.PartyMembers;
import mallang_trip.backend.domain.entity.party.PartyProposal;
import mallang_trip.backend.repository.user.UserRepository;
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
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PartyRepository partyRepository;
    private final PartyMembersRepository partyMembersRepository;
    private final PartyProposalRepository partyProposalRepository;
    private final PartyAgreementRepository partyAgreementRepository;

    // 파티 생성
/*    public PartyIdResponse createParty(PartyRequest request) {
        User driver = userRepository.findById(request.getDriverId())
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
        Course course = courseRepository.findById(request.getCourseId())
            .orElseThrow(() -> new BaseException(Not_Found));

        Party newParty = partyRepository.save(Party.builder()
            .driver(driver)
            .course(courseService.copyCourse(course))
            .region(course.getRegion())
            .headcount(request.getHeadcount())
            .startDate(LocalDate.parse(request.getStartDate()))
            .endDate(LocalDate.parse(request.getEndDate()))
            .build());

        partyMembersRepository.save(PartyMembers.builder()
            .party(newParty)
            .member(userService.getCurrentUser())
            .headcount(request.getHeadcount())
            .build());

        return PartyIdResponse.builder()
            .partyId(newParty.getId())
            .build();
    }*/

    // 파티 참가
    public void joinParty(JoinPartyRequest request){
        User user = userService.getCurrentUser();
        Party party = partyRepository.findById(request.getPartyId())
            .orElseThrow(() -> new BaseException(Not_Found));
        Course newCourse = courseRepository.findById(request.getNewCourseId())
            .orElseThrow(() -> new BaseException(Not_Found));

        PartyProposal proposal = partyProposalRepository.save(PartyProposal.builder()
            .course(newCourse)
            .party(party)
            .proposer(user)
            .headcount(request.getHeadcount())
            .agreementNeed(party.getHeadcount())
            .build());

        List<PartyMembers> members = partyMembersRepository.findByParty(party);
        for(PartyMembers member : members){
            partyAgreementRepository.save(PartyAgreement.builder()
                .proposal(proposal)
                .members(member)
                .build());
            // 기존 멤버들에게 알림 전송
        }
        party.setStatus(PartyStatus.APPROVAL_WAITING);
    }

    // 파티 수락 or 거절
    public void AcceptPartyProposal(Long agreementId, boolean accept){
        PartyAgreement agreement = partyAgreementRepository.findById(agreementId)
            .orElseThrow(() -> new BaseException(Not_Found));
        PartyProposal proposal = agreement.getProposal();
        Party party = proposal.getParty();
        PartyMembers member = agreement.getMembers();

        if(accept) {
            // Agreement 수락으로 변경 및 agreement count 증가
            agreement.setStatus(AgreementStatus.ACCEPT);
            proposal.setAgreementCount(proposal.getAgreementCount() + member.getHeadcount());
            // 수락 개수 확인 후 전체 수락 시
            if(proposal.getAgreementCount() == proposal.getAgreementNeed()){
                // 멤버 추가
                partyMembersRepository.save(PartyMembers.builder()
                        .party(party)
                        .member(proposal.getProposer())
                        .headcount(proposal.getHeadcount())
                        .build());
                // party headcount 추가
                party.setHeadcount(party.getHeadcount() + proposal.getHeadcount());
                // 코스 변경
                party.setCourse(proposal.getCourse());
                // Party status 변경
                proposal.getParty().setStatus(PartyStatus.RECRUIT_COMPLETED);
            }
        } else {
            // PartyProposal status 변경
            proposal.setStatus(ProposalStatus.REFUSED);
            // Party status 변경
            proposal.getParty().setStatus(PartyStatus.RECRUITING);
            // PartyAgreement 삭제
            partyAgreementRepository.deleteByProposal(proposal);
            // 거절 알림 전송
        }
    }
}

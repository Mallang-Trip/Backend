package mallang_trip.backend.service.party;

import static mallang_trip.backend.constant.PartyStatus.RECRUITING;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_PARTY;
import static mallang_trip.backend.controller.io.BaseResponseStatus.NOT_PARTY_MEMBER;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.party.PartyBriefResponse;
import mallang_trip.backend.domain.dto.party.PartyDetailsResponse;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.party.PartyHistory;
import mallang_trip.backend.domain.entity.party.PartyProposal;
import mallang_trip.backend.repository.party.PartyMemberRepository;
import mallang_trip.backend.repository.party.PartyRepository;
import mallang_trip.backend.service.CourseService;
import mallang_trip.backend.service.DriverService;
import mallang_trip.backend.service.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PartySearchService {

    private final PartyRepository partyRepository;
    private final PartyMemberRepository partyMemberRepository;
    private final PartyService partyService;
    private final PartyMemberService partyMemberService;
    private final PartyProposalService partyProposalService;
    private final CourseService courseService;
    private final UserService userService;
    private final DriverService driverService;
    private final ReservationService reservationService;
    private final PartyHistoryService partyHistoryService;

    /**
     * 모집중인 파티 검색 :
     * region == "all" -> 지역 전체 검색.
     */
    public List<PartyBriefResponse> searchRecruitingParties(String region, Integer headcount,
        String startDate, String endDate, Integer maxPrice) {
        List<Party> parties = region.equals("all") ? partyRepository.findByStatus(RECRUITING)
            : partyRepository.findByRegionAndStatus(region, RECRUITING);
        return parties.stream()
            .filter(party -> party.isHeadcountAvailable(headcount))
            .filter(party -> party.checkDate(startDate, endDate))
            .filter(party -> party.checkMaxPrice(maxPrice))
            .map(PartyBriefResponse::of)
            .collect(Collectors.toList());
    }

    /**
     * 대기중이거나 거절된 내 제안 목록 조회
     */

    /**
     * 대기중이거나 거절된 내 제안 상세조회
     */

    /**
     * (멤버) 내 파티 목록 조회
     */
    public List<PartyBriefResponse> getMyPartiesByMember(){
        return partyMemberRepository.findByUser(userService.getCurrentUser()).stream()
            .map(member -> PartyBriefResponse.of(member.getParty()))
            .collect(Collectors.toList());
    }

    /**
     * (드라이버) 내 파티 목록 조회
     */
    public List<PartyBriefResponse> getMyPartiesByDriver(){
        return partyRepository.findByDriver(driverService.getCurrentDriver()).stream()
            .map(PartyBriefResponse::of)
            .collect(Collectors.toList());
    }

    /**
     * 파티 상세 조회
     */
    public PartyDetailsResponse getPartyDetails(Long partyId){
        Party party = partyRepository.findById(partyId)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
        partyHistoryService.createPartyHistory(party);
        if(!partyService.isMyParty(userService.getCurrentUser(), party)){
            return getPartyDetailsByOutsider(party);
        } else{
            return getPartyDetails(party);
        }
    }

    /**
     * 내가 속하지 않은 파티 상세 조회
     */
    private PartyDetailsResponse getPartyDetailsByOutsider(Party party){
        if(!party.getStatus().equals(RECRUITING)){
            throw new BaseException(NOT_PARTY_MEMBER);
        }
        return partyToPartyDetailsResponse(party);
    }

    /**
     * 내가 속한 파티 상세 조회
     */
    private PartyDetailsResponse getPartyDetails(Party party){
        return MyPartyToPartyDetailsResponse(party);
    }

    /**
     * 내가 속하지 않은 Party -> PartyDetailsResponse 변환
     */
    private PartyDetailsResponse partyToPartyDetailsResponse(Party party){
        return PartyDetailsResponse.builder()
            .partyId(party.getId())
            .myParty(false)
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
            .members(partyMemberService.getMembersDetails(party))
            .build();
    }

    /**
     * 내가 속한 Party -> PartyDetailsResponse 변환.
     * Party에 진행중인 제안이 있을 경우, 제안 정보 추가.
     * 진행한 결제가 있을 경우, 결제 정보 추가.
     */
    private PartyDetailsResponse MyPartyToPartyDetailsResponse(Party party){
        PartyProposal proposal = partyProposalService.getWaitingProposalByParty(party);
        return PartyDetailsResponse.builder()
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
            .members(partyMemberService.getMembersDetails(party))
            .proposalExists(proposal == null ? false : true)
            .proposal(partyProposalService.toPartyProposalResponse(proposal))
            .reservation(reservationService.getReservationResponse(party))
            .build();
    }
}

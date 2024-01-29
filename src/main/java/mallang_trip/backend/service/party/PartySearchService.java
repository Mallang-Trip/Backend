package mallang_trip.backend.service.party;

import static mallang_trip.backend.constant.PartyStatus.RECRUITING;
import static mallang_trip.backend.constant.ProposalType.JOIN_WITH_COURSE_CHANGE;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_PARTY;
import static mallang_trip.backend.controller.io.BaseResponseStatus.NOT_PARTY_MEMBER;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.constant.ProposalStatus;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.party.PartyBriefResponse;
import mallang_trip.backend.domain.dto.party.PartyDetailsResponse;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.party.PartyMember;
import mallang_trip.backend.domain.entity.party.PartyProposal;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.party.PartyMemberRepository;
import mallang_trip.backend.repository.party.PartyProposalRepository;
import mallang_trip.backend.repository.party.PartyRepository;
import mallang_trip.backend.service.CourseService;
import mallang_trip.backend.service.driver.DriverService;
import mallang_trip.backend.service.ReservationService;
import mallang_trip.backend.service.user.UserService;
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
	private final PartyDibsService partyDibsService;
	private final PartyProposalRepository partyProposalRepository;

	/**
	 * 모집중인 파티 검색 : region == "all" -> 지역 전체 검색.
	 */
	public List<PartyBriefResponse> searchRecruitingParties(String region, Integer headcount,
		String startDate, String endDate, Integer maxPrice) {
		List<Party> parties = region.equals("all") ? partyRepository.findByStatus(RECRUITING)
			: partyRepository.findByRegionAndStatus(region, RECRUITING);
		return parties.stream()
			.filter(party -> party.isHeadcountAvailable(headcount))
			.filter(party -> party.checkDate(startDate, endDate))
			.filter(party -> party.checkMaxPrice(maxPrice))
			.sorted(Comparator
				.comparing(Party::getHeadcount).reversed()
				.thenComparing(Party::getStartDate))
			.map(PartyBriefResponse::of)
			.collect(Collectors.toList());
	}

	/**
	 * (멤버) 내 파티 목록 조회
	 */
	public List<PartyBriefResponse> getMyPartiesByMember() {
		User user = userService.getCurrentUser();
		List<PartyBriefResponse> partyResponses = Stream.concat(
				getMyProposingParties(user).stream(),
				partyMemberRepository.findByUser(user).stream().map(PartyMember::getParty)
			)
			.sorted(Comparator.comparing(Party::getStartDate).reversed())
			.map(PartyBriefResponse::of)
			.collect(Collectors.toList());
		return partyResponses;
	}

	/**
	 * (드라이버) 내 파티 목록 조회
	 */
	public List<PartyBriefResponse> getMyPartiesByDriver() {
		return partyRepository.findByDriver(driverService.getCurrentDriver()).stream()
			.map(PartyBriefResponse::of)
			.collect(Collectors.toList());
	}

	/**
	 * 파티 상세 조회
	 */
	public PartyDetailsResponse getPartyDetails(Long partyId) {
		User user = userService.getCurrentUser();
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
		partyHistoryService.createPartyHistory(party);
		if (isMyProposingParty(user, party)) {
			return getPartyDetails(party);
		} else if (!partyService.isMyParty(user, party)) {
			return getPartyDetailsByOutsider(party);
		} else {
			return getPartyDetails(party);
		}
	}

	/**
	 * 내가 속하지 않은 파티 상세 조회
	 */
	private PartyDetailsResponse getPartyDetailsByOutsider(Party party) {
		if (!party.getStatus().equals(RECRUITING)) {
			throw new BaseException(NOT_PARTY_MEMBER);
		}
		return partyToPartyDetailsResponse(party);
	}

	/**
	 * 내가 속한 파티 상세 조회
	 */
	private PartyDetailsResponse getPartyDetails(Party party) {
		return MyPartyToPartyDetailsResponse(party);
	}

	/**
	 * 내가 가입 제안 중인 파티 조회
	 */
	private List<Party> getMyProposingParties(User user) {
		return partyProposalRepository.findByProposerAndTypeAndStatus(user, JOIN_WITH_COURSE_CHANGE,
				ProposalStatus.WAITING).stream()
			.map(proposal -> proposal.getParty())
			.collect(Collectors.toList());
	}

	/**
	 * 내가 가입 제안 중인 파티인지 유무
	 */
	private boolean isMyProposingParty(User user, Party party) {
		return partyProposalRepository.existsByPartyAndProposerAndTypeAndStatus(party, user,
			JOIN_WITH_COURSE_CHANGE, ProposalStatus.WAITING);
	}

	/**
	 * 내가 속하지 않은 Party -> PartyDetailsResponse 변환
	 */
	private PartyDetailsResponse partyToPartyDetailsResponse(Party party) {
		return PartyDetailsResponse.builder()
			.partyId(party.getId())
			.myParty(false)
			.dibs(partyDibsService.checkPartyDibs(party))
			.partyStatus(party.getStatus())
			.driverId(party.getDriver().getId())
			.driverName(party.getDriver().getUser().getName())
			.driverReady(party.getDriverReady())
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
	 * 내가 속한 Party -> PartyDetailsResponse 변환. Party에 진행중인 제안이 있을 경우, 제안 정보 추가. 진행한 결제가 있을 경우, 결제 정보
	 * 추가.
	 */
	private PartyDetailsResponse MyPartyToPartyDetailsResponse(Party party) {
		PartyProposal proposal = partyProposalService.getWaitingProposalByParty(party);
		return PartyDetailsResponse.builder()
			.partyId(party.getId())
			.myParty(partyService.isMyParty(userService.getCurrentUser(), party))
			.dibs(partyDibsService.checkPartyDibs(party))
			.partyStatus(party.getStatus())
			.driverId(party.getDriver().getId())
			.driverName(party.getDriver().getUser().getName())
			.driverReady(party.getDriverReady())
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

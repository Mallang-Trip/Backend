package mallang_trip.backend.domain.party.service;

import static mallang_trip.backend.domain.party.constant.PartyStatus.DAY_OF_TRAVEL;
import static mallang_trip.backend.domain.party.constant.PartyStatus.FINISHED;
import static mallang_trip.backend.domain.party.constant.PartyStatus.RECRUITING;
import static mallang_trip.backend.domain.party.constant.PartyStatus.SEALED;
import static mallang_trip.backend.domain.party.constant.PartyStatus.WAITING_COURSE_CHANGE_APPROVAL;
import static mallang_trip.backend.domain.party.constant.PartyStatus.WAITING_DRIVER_APPROVAL;
import static mallang_trip.backend.domain.party.constant.PartyStatus.WAITING_JOIN_APPROVAL;
import static mallang_trip.backend.domain.party.constant.ProposalType.JOIN_WITH_COURSE_CHANGE;
import static mallang_trip.backend.domain.party.exception.PartyExceptionStatus.*;
import static mallang_trip.backend.global.io.BaseResponseStatus.Bad_Request;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.party.constant.ProposalStatus;
import mallang_trip.backend.domain.party.entity.PartyRegion;
import mallang_trip.backend.domain.party.repository.PartyRegionRepository;
import mallang_trip.backend.domain.user.service.CurrentUserService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.party.dto.PartyBriefResponse;
import mallang_trip.backend.domain.party.dto.PartyDetailsResponse;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.party.entity.PartyMember;
import mallang_trip.backend.domain.party.entity.PartyProposal;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.course.service.CourseService;
import mallang_trip.backend.domain.driver.service.DriverService;
import mallang_trip.backend.domain.party.repository.PartyMemberRepository;
import mallang_trip.backend.domain.party.repository.PartyProposalRepository;
import mallang_trip.backend.domain.party.repository.PartyRepository;
import mallang_trip.backend.domain.reservation.service.ReservationService;
import mallang_trip.backend.domain.user.service.UserService;
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
	private final CurrentUserService currentUserService;
	private final DriverService driverService;
	private final ReservationService reservationService;
	private final PartyHistoryService partyHistoryService;
	private final PartyDibsService partyDibsService;
	private final PartyProposalRepository partyProposalRepository;

	private final PartyRegionRepository partyRegionRepository;

	/**
	 * 모집중인 파티 검색 : region == "all" -> 지역 전체 검색.
	 */
	public List<PartyBriefResponse> searchRecruitingParties(String region, Integer headcount,
		String startDate, String endDate, Integer maxPrice) {

		// 지역 확인
		if(!region.equals("all")){
			partyRegionRepository.findByRegion(region)
				.orElseThrow(() -> new BaseException(REGION_NOT_FOUND));
		}

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
		User user = currentUserService.getCurrentUser();
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
		User user = currentUserService.getCurrentUser();
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
		return myPartyToPartyDetailsResponse(party);
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
	 * (관리자) Status 별 파티 조회
	 */
	public List<PartyBriefResponse> getPartiesByAdmin(String status) {
		switch (status){
			case "CANCELED":
				return getCanceledParties();
			case "BEFORE_RESERVATION":
				return getBeforeReservationParties();
			case "AFTER_RESERVATION":
				return getReservedParties();
			case "FINISHED":
				return getFinishedParties();
			default:
				throw new BaseException(Bad_Request);
		}
	}

	/**
	 * 취소된 (CANCELED_%) 파티 조회
	 */
	private List<PartyBriefResponse> getCanceledParties() {
		return partyRepository.findByStatusStartWithCanceled().stream()
			.map(PartyBriefResponse::of)
			.sorted(Comparator.comparing(PartyBriefResponse::getUpdatedAt).reversed())
			.collect(Collectors.toList());
	}

	/**
	 * 예약 전 (WAITING_DRIVER_APPROVAL, RECRUITING, WAITING_JOIN_APPROVAL) 파티 조회
	 */
	private List<PartyBriefResponse> getBeforeReservationParties() {
		return Stream.of(
				partyRepository.findByStatus(WAITING_DRIVER_APPROVAL),
				partyRepository.findByStatus(RECRUITING),
				partyRepository.findByStatus(WAITING_JOIN_APPROVAL))
			.flatMap(x -> x.stream())
			.map(PartyBriefResponse::of)
			.sorted(Comparator.comparing(PartyBriefResponse::getUpdatedAt).reversed())
			.collect(Collectors.toList());
	}

	/**
	 * 예약 된 (SEALED, WAITING_COURSE_CHANGE_APPROVAL, DAY_OF_TRAVEL) 파티 조회
	 */
	private List<PartyBriefResponse> getReservedParties() {
		return Stream.of(
				partyRepository.findByStatus(SEALED),
				partyRepository.findByStatus(WAITING_COURSE_CHANGE_APPROVAL),
				partyRepository.findByStatus(DAY_OF_TRAVEL))
			.flatMap(x -> x.stream())
			.map(PartyBriefResponse::of)
			.sorted(Comparator.comparing(PartyBriefResponse::getUpdatedAt).reversed())
			.collect(Collectors.toList());
	}

	/**
	 * 완료된 (FINISHED) 파티 조회
	 */
	private List<PartyBriefResponse> getFinishedParties() {
		return partyRepository.findByStatus(FINISHED).stream()
			.map(PartyBriefResponse::of)
			.sorted(Comparator.comparing(PartyBriefResponse::getUpdatedAt).reversed())
			.collect(Collectors.toList());
	}

	/**
	 * (관리자) 파티 상세 조회
	 */
	public PartyDetailsResponse viewPartyForAdmin(Long partyId){
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_PARTY));
		return myPartyToPartyDetailsResponse(party);
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
	 * 내가 속한 Party -> PartyDetailsResponse 변환.
	 * 진행중인 제안이 있을 경우, 제안 정보 추가.
	 * 진행한 결제가 있을 경우, 결제 정보 추가.
	 */
	private PartyDetailsResponse myPartyToPartyDetailsResponse(Party party) {
		PartyProposal proposal = partyProposalService.getWaitingProposalByParty(party);
		return PartyDetailsResponse.builder()
			.partyId(party.getId())
			.myParty(partyService.isMyParty(currentUserService.getCurrentUser(), party))
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

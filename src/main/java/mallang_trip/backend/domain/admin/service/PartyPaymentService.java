package mallang_trip.backend.domain.admin.service;

import static mallang_trip.backend.domain.party.constant.PartyStatus.FINISHED;
import static mallang_trip.backend.global.io.BaseResponseStatus.Bad_Request;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.admin.dto.PartyMemberPaymentResponse;
import mallang_trip.backend.domain.admin.dto.PartyPaymentResponse;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.party.entity.PartyMember;
import mallang_trip.backend.domain.party.repository.PartyRepository;
import mallang_trip.backend.domain.party.service.PartyMemberService;
import mallang_trip.backend.domain.reservation.repository.ReservationRepository;
import mallang_trip.backend.global.io.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PartyPaymentService {

	private final PartyRepository partyRepository;
	private final ReservationRepository reservationRepository;
	private final PartyMemberService partyMemberService;

	/**
	 * 파티의 결제 내역을 조회합니다.
	 * <p>
	 * status 값이 "reserved" 일 경우 예약된 파티를 조회하고, status 값이 "finished" 일 경우 완료된 파티를 조회합니다.
	 *
	 * @param status 조회할 파티 status 값
	 * @return 결제 내역 정보를 담은 PartyPaymentResponse 객체 배열
	 */
	public List<PartyPaymentResponse> getPartiesByStatus(String status) {
		if (status.equalsIgnoreCase("reserved")) {
			return getReservedParties();
		} else if (status.equalsIgnoreCase("finished")) {
			return getFinishedParties();
		} else {
			throw new BaseException(Bad_Request);
		}
	}

	/**
	 * 예약된 파티의 결제 내역 정보를 조회합니다.
	 * <p>
	 * 예약된 파티는 status가 SEALED, WAITING_COURSE_CHANGE_APPROVAL, DAY_OF_TRAVEL 중 하나인 파티를 의미합니다.
	 *
	 * @return 파티의 결제 내역 정보를 담은 PartyPaymentResponse 객체 배열
	 */
	private List<PartyPaymentResponse> getReservedParties() {
		return partyRepository.findReservedParties().stream()
			.map(party -> toPartyPaymentResponse(party))
			.collect(Collectors.toList());
	}

	/**
	 * 완료된 파티의 결제 내역 정보를 조회합니다.
	 * <p>
	 * 완료된 파티는 status가 FINISHED 인 파티를 의미합니다.
	 *
	 * @return 파티의 결제 내역 정보를 담은 PartyPaymentResponse 객체 배열
	 */
	private List<PartyPaymentResponse> getFinishedParties() {
		return partyRepository.findByStatus(FINISHED).stream()
			.map(party -> toPartyPaymentResponse(party))
			.collect(Collectors.toList());
	}

	/**
	 * Party 객체를 파티의 결제 내역 정보를 담은 PartyPaymentResponse DTO 로 변환합니다.
	 *
	 * @param party 변환할 Party 객체
	 * @return 변환된 PartyPaymentResponse 객체
	 */
	private PartyPaymentResponse toPartyPaymentResponse(Party party) {
		List<PartyMemberPaymentResponse> memberResponses = partyMemberService.getMembers(party)
			.stream()
			.map(member -> toPartyMemberPaymentResponse(member))
			.collect(Collectors.toList());

		return PartyPaymentResponse.builder()
			.partyId(party.getId())
			.partyName(party.getCourse().getName())
			.startDate(party.getStartDate())
			.endDate(party.getEndDate())
			.driverId(party.getDriver().getId())
			.driverName(party.getDriver().getUser().getName())
			.capacity(party.getCapacity())
			.headcount(party.getHeadcount())
			.status(party.getStatus())
			.partyMembers(memberResponses)
			.build();
	}

	/**
	 * PartyMember 객체를 파티 멤버의 결제 상태 정보를 담은 PartyMemberPaymentResponse DTO 로 변환합니다.
	 *
	 * @param member 변환할 PartyMember 객체
	 * @return 변환된 PartyMemberPaymentResponse 객체
	 */
	private PartyMemberPaymentResponse toPartyMemberPaymentResponse(PartyMember member) {
		return PartyMemberPaymentResponse.builder()
			.userId(member.getUser().getId())
			.nickname(member.getUser().getNickname())
			.status(reservationRepository.findPaymentStatusByMember(member.getId()))
			.build();
	}
}

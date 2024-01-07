package mallang_trip.backend.service;

import static mallang_trip.backend.constant.ReservationStatus.PAYMENT_COMPLETE;
import static mallang_trip.backend.constant.ReservationStatus.PAYMENT_REQUIRED;
import static mallang_trip.backend.constant.ReservationStatus.REFUND_COMPLETE;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_PAYMENT;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_RESERVATION;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;

import java.time.LocalDate;
import java.time.Period;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.constant.ReservationStatus;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.party.PartyMember;
import mallang_trip.backend.domain.entity.reservation.Reservation;
import mallang_trip.backend.repository.reservation.ReservationRepository;
import mallang_trip.backend.service.party.PartyMemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

	private final PartyMemberService partyMemberService;
	private final ReservationRepository reservationRepository;

	/**
	 * 파티원 1/N 결제
	 */
	public void reserveParty(Party party) {
		partyMemberService.getMembers(party).stream()
			.forEach(member -> pay(member));
	}

	private void pay(PartyMember member) {
		// TODO: 결제 진행
		ReservationStatus status = true ? PAYMENT_COMPLETE : PAYMENT_REQUIRED;
		reservationRepository.save(Reservation.builder()
			.member(member)
			.paymentAmount(getPaymentAmount(member))
			.status(status)
			.build());
	}

	public int refund(PartyMember member) {
		Reservation reservation = reservationRepository.findByMember(member)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_RESERVATION));
		// status CHECK
		if (!reservation.getStatus().equals(PAYMENT_COMPLETE)) {
			throw new BaseException(CANNOT_FOUND_PAYMENT);
		}
		// 위약금 계산
		int refundAmount = getRefundAmount(reservation);
		// TODO: 환불 진행
		reservation.setStatus(REFUND_COMPLETE);
		return refundAmount;
	}

	/**
	 * 결제 금액 조회
	 */
	private int getPaymentAmount(PartyMember member) {
		Party party = member.getParty();
		int totalPrice = party.getCourse().getTotalPrice();
		int totalHeadcount = partyMemberService.getTotalHeadcount(party);
		return totalPrice / totalHeadcount * member.getHeadcount();
	}

	/**
	 * 환불 금액 조회
	 */
	private int getRefundAmount(Reservation reservation) {
		Party party = reservation.getMember().getParty();
		int dDay = Period.between(LocalDate.now(), party.getStartDate()).getDays();
		if (dDay <= 2) {
			return 0;
		} else if (dDay == 3) {
			return (int)(reservation.getPaymentAmount() * 0.1);
		} else if (dDay == 4) {
			return (int)(reservation.getPaymentAmount() * 0.25);
		} else if (dDay == 5) {
			return (int)(reservation.getPaymentAmount() * 0.50);
		} else if (dDay == 6) {
			return (int)(reservation.getPaymentAmount() * 0.75);
		} else if (dDay == 7) {
			return (int)(reservation.getPaymentAmount() * 0.90);
		} else {
			return reservation.getPaymentAmount();
		}
	}


}

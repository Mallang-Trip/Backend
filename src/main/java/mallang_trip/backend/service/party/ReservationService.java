package mallang_trip.backend.service.party;

import static mallang_trip.backend.constant.ReservationStatus.PAYMENT_COMPLETE;
import static mallang_trip.backend.constant.ReservationStatus.PAYMENT_REQUIRED;
import static mallang_trip.backend.constant.ReservationStatus.REFUND_COMPLETE;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_PAYMENT;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_RESERVATION;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.controller.io.BaseResponseStatus.NOT_PARTY_MEMBER;

import java.time.LocalDate;
import java.time.Period;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.constant.ReservationStatus;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.party.ReservationResponse;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.party.PartyMember;
import mallang_trip.backend.domain.entity.reservation.Reservation;
import mallang_trip.backend.repository.party.PartyMemberRepository;
import mallang_trip.backend.repository.reservation.ReservationRepository;
import mallang_trip.backend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

	private final PartyMemberService partyMemberService;
	private final UserService userService;
	private final ReservationRepository reservationRepository;
	private final PartyMemberRepository partyMemberRepository;

	/**
	 * 파티 자동 결제
	 */
	public void reserveParty(Party party) {
		partyMemberService.getMembers(party).stream()
			.forEach(member -> pay(member));
	}

	/**
	 * 파티원 1/N 결제
	 */
	private void pay(PartyMember member) {
		// TODO: 결제 진행
		ReservationStatus status = true ? PAYMENT_COMPLETE : PAYMENT_REQUIRED;
		reservationRepository.save(Reservation.builder()
			.member(member)
			.paymentAmount(getPaymentAmount(member))
			.status(status)
			.build());
	}

	/**
	 * 파티원 환불
	 */
	public int refund(PartyMember member) {
		Reservation reservation = reservationRepository.findByMemberAndStatusNot(member, REFUND_COMPLETE)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_RESERVATION));
		// status CHECK
		if (!reservation.getStatus().equals(PAYMENT_COMPLETE)) {
			throw new BaseException(CANNOT_FOUND_PAYMENT);
		}
		// 위약금 계산
		int refundAmount = getRefundAmount(reservation);
		// TODO: 환불 진행
		reservation.setRefundAmount(reservation.getPaymentAmount());
		reservation.setStatus(REFUND_COMPLETE);
		return refundAmount;
	}

	/**
	 * 무료 환불
	 */
	public void freeRefund(PartyMember member){
		Reservation reservation = reservationRepository.findByMemberAndStatusNot(member, REFUND_COMPLETE)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_RESERVATION));
		if (reservation.getStatus().equals(PAYMENT_COMPLETE)) {
			// TODO: 환불 진행
			reservation.setRefundAmount(reservation.getPaymentAmount());
			reservation.setStatus(REFUND_COMPLETE);
		} else if (reservation.getStatus().equals(PAYMENT_REQUIRED)){
			reservation.setStatus(REFUND_COMPLETE);
		}
	}

	/**
	 * 모든 파티 멤버 전액 환불
	 */
	public void refundAllMembers(Party party){
		partyMemberService.getMembers(party).stream()
			.forEach(member -> freeRefund(member));
	}

	/**
	 * 결제 금액 계산
	 */
	private int getPaymentAmount(PartyMember member) {
		Party party = member.getParty();
		int totalPrice = party.getCourse().getTotalPrice();
		int totalHeadcount = partyMemberService.getTotalHeadcount(party);
		return totalPrice / totalHeadcount * member.getHeadcount();
	}

	/**
	 * 환불 금액 계산
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

	public void payPenaltyByDriver(Party party){
		int penalty = getPenaltyToDriver(party);
		//TODO: 드라이버 위약금 저장
	}
	public int getPenaltyToDriver(Party party){
		int totalPrice = party.getCourse().getTotalPrice();
		int dDay = Period.between(LocalDate.now(), party.getStartDate()).getDays();
		if (dDay > 7) {
			return 0;
		} else if (dDay == 0) {
			return (int)(totalPrice * 0.4);
		} else if (dDay == 1) {
			return (int)(totalPrice * 0.35);
		} else if (dDay == 2) {
			return (int)(totalPrice * 0.3);
		} else if (dDay == 3) {
			return (int)(totalPrice * 0.25);
		} else if (dDay == 4) {
			return (int)(totalPrice * 0.2);
		} else if (dDay == 5) {
			return (int)(totalPrice * 0.15);
		} else if (dDay == 6) {
			return (int)(totalPrice * 0.1);
		} else if (dDay == 7) {
			return (int)(totalPrice * 0.05);
		} else {
			throw new BaseException(Forbidden);
		}
	}

	public ReservationResponse getReservationResponse(Party party){
		PartyMember member = partyMemberRepository.findByPartyAndUser(party,
				userService.getCurrentUser())
			.orElseThrow(() -> new BaseException(NOT_PARTY_MEMBER));
		Reservation reservation = reservationRepository.findByMemberAndStatusNot(member, REFUND_COMPLETE)
			.orElse(null);
		return ReservationResponse.of(reservation);
	}
}

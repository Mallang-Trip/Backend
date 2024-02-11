package mallang_trip.backend.service;

import static mallang_trip.backend.constant.ReservationStatus.PAYMENT_COMPLETE;
import static mallang_trip.backend.constant.ReservationStatus.PAYMENT_REQUIRED;
import static mallang_trip.backend.constant.ReservationStatus.REFUND_COMPLETE;
import static mallang_trip.backend.constant.Role.ROLE_ADMIN;
import static mallang_trip.backend.constant.Role.ROLE_DRIVER;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_PAYMENT;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_RESERVATION;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Forbidden;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.Period;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.constant.Role;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.party.ReservationResponse;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.party.PartyMember;
import mallang_trip.backend.domain.entity.reservation.Reservation;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.party.PartyMemberRepository;
import mallang_trip.backend.repository.reservation.ReservationRepository;
import mallang_trip.backend.service.party.PartyMemberService;
import mallang_trip.backend.service.payment.TossBrandPayService;
import mallang_trip.backend.service.user.UserService;
import org.json.JSONException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

	private final PartyMemberService partyMemberService;
	private final TossBrandPayService tossBrandPayService;
	private final UserService userService;
	private final ReservationRepository reservationRepository;
	private final PartyMemberRepository partyMemberRepository;

	/**
	 * 파티 자동 결제
	 */
	public void reserveParty(Party party)
		throws JSONException, URISyntaxException, JsonProcessingException {
		for (PartyMember partyMember : partyMemberService.getMembers(party)) {
			pay(partyMember);
		}
	}

	/**
	 * 파티원 1/N 결제
	 */
	private void pay(PartyMember member)
		throws JSONException, URISyntaxException, JsonProcessingException {
		Reservation reservation = reservationRepository.save(Reservation.builder()
			.member(member)
			.paymentAmount(calculatePaymentAmount(member))
			.build());
		tossBrandPayService.pay(reservation);
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
		reservation.changeStatus(REFUND_COMPLETE);
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
			reservation.changeStatus(REFUND_COMPLETE);
		} else if (reservation.getStatus().equals(PAYMENT_REQUIRED)){
			reservation.changeStatus(REFUND_COMPLETE);
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
	private int calculatePaymentAmount(PartyMember member) {
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
		User user = userService.getCurrentUser();
		Role role = user.getRole();
		if(role.equals(ROLE_ADMIN) || role.equals(ROLE_DRIVER)){
			return null;
		}
		PartyMember member = partyMemberRepository.findByPartyAndUser(party, user)
			.orElse(null);
		Reservation reservation = reservationRepository.findByMemberAndStatusNot(member, REFUND_COMPLETE)
			.orElse(null);
		return ReservationResponse.of(reservation);
	}
}

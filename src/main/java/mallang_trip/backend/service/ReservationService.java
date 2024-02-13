package mallang_trip.backend.service;

import static mallang_trip.backend.constant.ReservationStatus.PAYMENT_COMPLETE;
import static mallang_trip.backend.constant.ReservationStatus.PAYMENT_REQUIRED;
import static mallang_trip.backend.constant.ReservationStatus.REFUND_COMPLETE;
import static mallang_trip.backend.constant.ReservationStatus.REFUND_FAILED;
import static mallang_trip.backend.constant.Role.ROLE_ADMIN;
import static mallang_trip.backend.constant.Role.ROLE_DRIVER;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_PAYMENT;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_RESERVATION;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Forbidden;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
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
import mallang_trip.backend.service.payment.PaymentService;
import mallang_trip.backend.service.user.UserService;
import org.json.JSONException;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

	private final PartyMemberService partyMemberService;
	private final PaymentService paymentService;
	private final UserService userService;
	private final ReservationRepository reservationRepository;
	private final PartyMemberRepository partyMemberRepository;

	/**
	 * 파티원 전원 자동 결제
	 */
	public void reserveParty(Party party) {
		partyMemberService.getMembers(party).stream()
			.forEach(this::pay);
	}

	/**
	 * 파티원 1/N 결제
	 */
	private void pay(PartyMember member) {
		Reservation reservation = reservationRepository.save(Reservation.builder()
			.member(member)
			.paymentAmount(calculatePaymentAmount(member))
			.build());
		paymentService.pay(reservation);
	}

	/**
	 * 위약금을 제외한 금액 환불 후, 위약금 값 반환
	 */
	public Integer refund(PartyMember member) {
		// 정상적으로 결제된 상태인 경우
		Optional<Reservation> paymentComplete = reservationRepository.findByMemberAndStatus(member, PAYMENT_COMPLETE);
		if (paymentComplete.isPresent()) {
			Reservation reservation = paymentComplete.get();
			Integer refundAmount = getRefundAmount(reservation);
			paymentService.cancel(reservation, refundAmount);
			return reservation.getPaymentAmount() - refundAmount;
		}

		// 결제 실패 상태인 경우
		Optional<Reservation> paymentRequired = reservationRepository.findByMemberAndStatus(member, PAYMENT_REQUIRED);
		if(paymentRequired.isPresent()){
			Reservation reservation = paymentComplete.get();
			if(penaltyExists(reservation.getMember())){
				//TODO: 위약금 지불 필요 알림 전송
			}
			return reservation.getPaymentAmount() - getRefundAmount(reservation);
		}
		return 0;
	}

	/**
	 * 무료 환불
	 */
	public void freeRefund(PartyMember member){
		reservationRepository.findByMemberAndStatus(member, PAYMENT_COMPLETE)
			.ifPresent(reservation -> {
				paymentService.cancel(reservation, reservation.getPaymentAmount());
				reservation.setRefundAmount(reservation.getPaymentAmount());
				reservation.changeStatus(REFUND_COMPLETE);
			});
		reservationRepository.findByMemberAndStatus(member, PAYMENT_REQUIRED)
			.ifPresent(reservation -> {
				reservation.changeStatus(REFUND_COMPLETE);
			});
	}

	/**
	 * 모든 파티 멤버 전액 환불
	 */
	public void refundAllMembers(Party party){
		partyMemberService.getMembers(party).stream()
			.forEach(member -> freeRefund(member));
	}

	/**
	 * 결제 카드 변경
	 */


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
	public int getRefundAmount(Reservation reservation) {
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

	/**
	 * 위약금 발생 여부
	 */
	public Boolean penaltyExists(PartyMember partyMember){
		Party party = partyMember.getParty();
		int dDay = Period.between(LocalDate.now(), party.getStartDate()).getDays();
		return dDay < 8 ? true : false;
	}

	public void payPenaltyByDriver(Party party){
		int penalty = getPenaltyToDriver(party);
		//TODO: 드라이버 패널티 금액 저장
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

		Optional<Reservation> paymentComplete = reservationRepository.findByMemberAndStatus(
			member, PAYMENT_COMPLETE);
		Optional<Reservation> paymentRequired = reservationRepository.findByMemberAndStatus(
			member, PAYMENT_REQUIRED);

		if(paymentComplete.isPresent()){
			return ReservationResponse.of(paymentComplete.get());
		} else if (paymentRequired.isPresent()){
			return ReservationResponse.of(paymentRequired.get());
		} else {
			return null;
		}
	}
}

package mallang_trip.backend.domain.reservation.service;

import static mallang_trip.backend.domain.reservation.constant.ReservationStatus.PAYMENT_COMPLETE;
import static mallang_trip.backend.domain.reservation.constant.ReservationStatus.PAYMENT_REQUIRED;
import static mallang_trip.backend.domain.reservation.constant.ReservationStatus.REFUND_COMPLETE;
import static mallang_trip.backend.domain.user.constant.Role.ROLE_ADMIN;
import static mallang_trip.backend.domain.user.constant.Role.ROLE_DRIVER;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.user.constant.Role;
import mallang_trip.backend.domain.reservation.dto.ReservationResponse;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.party.entity.PartyMember;
import mallang_trip.backend.domain.reservation.entity.Reservation;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.party.repository.PartyMemberRepository;
import mallang_trip.backend.domain.party.service.PartyMemberService;
import mallang_trip.backend.domain.reservation.repository.ReservationRepository;
import mallang_trip.backend.domain.user.service.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

	private final PartyMemberService partyMemberService;
	//private final PaymentService paymentService;
	private final CurrentUserService currentUserService;
	private final ReservationNotificationService reservationNotificationService;
	private final ReservationRepository reservationRepository;
	private final PartyMemberRepository partyMemberRepository;

	/**
	 * 파티원 전원 자동 결제
	 */
	public void reserveParty(Party party) {
		partyMemberService.getMembers(party).stream()
			.forEach(this::reserve);
	}

	/**
	 * 파티원 1/N 결제
	 */
	private void reserve(PartyMember member) {
		Reservation reservation = reservationRepository.save(Reservation.builder()
			.member(member)
			.paymentAmount(calculatePaymentAmount(member))
			.build());
		//paymentService.pay(reservation);
	}

	/**
	 * 위약금을 제외한 금액 환불 후, 위약금 값 반환
	 */
	public Integer refund(PartyMember member) {
		// 정상적으로 결제된 상태인 경우
		Optional<Reservation> paymentComplete = reservationRepository.findByMemberAndStatus(member,
			PAYMENT_COMPLETE);
		if (paymentComplete.isPresent()) {
			Reservation reservation = paymentComplete.get();
			Integer refundAmount = calculateRefundAmount(reservation);
			//paymentService.cancel(reservation, refundAmount);
			return reservation.getPaymentAmount() - refundAmount;
		}

		// 결제 실패 상태인 경우
		Optional<Reservation> paymentRequired = reservationRepository.findByMemberAndStatus(member,
			PAYMENT_REQUIRED);
		if (paymentRequired.isPresent()) {
			Reservation reservation = paymentRequired.get();
			Integer penaltyAmount = reservation.getPaymentAmount() - calculateRefundAmount(reservation);
			if (penaltyAmount > 0) {
				reservationNotificationService.penaltyPaymentRequired(
					reservation.getMember().getUser(), penaltyAmount);
			}
			reservation.changeStatus(REFUND_COMPLETE);
			return penaltyAmount;
		}
		return 0;
	}

	/**
	 * 무료 환불
	 */
	public void freeRefund(PartyMember member) {
		reservationRepository.findByMemberAndStatus(member, PAYMENT_COMPLETE)
			.ifPresent(reservation -> {
				//paymentService.cancel(reservation, reservation.getPaymentAmount());
				reservation.setRefundAmount(reservation.getPaymentAmount());
			});
		reservationRepository.findByMemberAndStatus(member, PAYMENT_REQUIRED)
			.ifPresent(reservation -> {
				reservation.changeStatus(REFUND_COMPLETE);
			});
	}

	/**
	 * 모든 파티 멤버 전액 환불
	 */
	public void refundAllMembers(Party party) {
		partyMemberService.getMembers(party).stream()
			.forEach(member -> freeRefund(member));
	}

	/**
	 * 결제 금액 계산
	 */
	private int calculatePaymentAmount(PartyMember member) {
		Party party = member.getParty();
		int totalPrice = party.getCourse().getTotalPrice();
		int discountPrice = party.getCourse().getDiscountPrice();
		int totalHeadcount = partyMemberService.getTotalHeadcount(party);
		return (totalPrice - discountPrice) / totalHeadcount * member.getHeadcount();
	}

	/**
	 * 환불 금액 계산
	 */
	public int calculateRefundAmount(Reservation reservation) {
		Party party = reservation.getMember().getParty();
		int dDay = Period.between(LocalDate.now(), party.getStartDate()).getDays();
		if (dDay <= 2) {
			return 0;
		} else if (dDay == 3) {
			return (int) (reservation.getPaymentAmount() * 0.1);
		} else if (dDay == 4) {
			return (int) (reservation.getPaymentAmount() * 0.25);
		} else if (dDay == 5) {
			return (int) (reservation.getPaymentAmount() * 0.50);
		} else if (dDay == 6) {
			return (int) (reservation.getPaymentAmount() * 0.75);
		} else if (dDay == 7) {
			return (int) (reservation.getPaymentAmount() * 0.90);
		} else {
			return reservation.getPaymentAmount();
		}
	}

	public void savePenaltyToDriver(Party party) {
		int penalty = calculatePenaltyToDriver(party);
		//TODO: 드라이버 패널티 금액 저장
	}

	public int calculatePenaltyToDriver(Party party) {
		int totalPrice = party.getCourse().getTotalPrice();
		int dDay = Period.between(LocalDate.now(), party.getStartDate()).getDays();
		if (dDay == 0) {
			return (int) (totalPrice * 0.4);
		} else if (dDay == 1) {
			return (int) (totalPrice * 0.35);
		} else if (dDay == 2) {
			return (int) (totalPrice * 0.3);
		} else if (dDay == 3) {
			return (int) (totalPrice * 0.25);
		} else if (dDay == 4) {
			return (int) (totalPrice * 0.2);
		} else if (dDay == 5) {
			return (int) (totalPrice * 0.15);
		} else if (dDay == 6) {
			return (int) (totalPrice * 0.1);
		} else if (dDay == 7) {
			return (int) (totalPrice * 0.05);
		} else {
			return 0;
		}
	}

	public ReservationResponse getReservationResponse(Party party) {
		User user = currentUserService.getCurrentUser();
		Role role = user.getRole();
		if (role.equals(ROLE_ADMIN) || role.equals(ROLE_DRIVER)) {
			return null;
		}

		Optional<PartyMember> member = partyMemberRepository.findByPartyAndUser(party, user);
		if(member.isEmpty()) return null;

		Optional<Reservation> paymentComplete = reservationRepository.findByMemberAndStatus(
			member.get(), PAYMENT_COMPLETE);
		Optional<Reservation> paymentRequired = reservationRepository.findByMemberAndStatus(
			member.get(), PAYMENT_REQUIRED);

		if (paymentComplete.isPresent()) {
			return ReservationResponse.of(paymentComplete.get());
		} else if (paymentRequired.isPresent()) {
			return ReservationResponse.of(paymentRequired.get());
		} else {
			return null;
		}
	}
}

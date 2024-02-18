package mallang_trip.backend.domain.payment.service;

import static mallang_trip.backend.domain.notification.constant.NotificationType.PARTY;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.party.entity.PartyMember;
import mallang_trip.backend.domain.reservation.entity.Reservation;
import mallang_trip.backend.domain.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentNotificationService {

	private final NotificationService notificationService;

	// 1. 결제 성공
	public void paymentSuccess(Reservation reservation){
		PartyMember member = reservation.getMember();
		Party party = member.getParty();
		String content = new StringBuilder()
			.append("[")
			.append(party.getCourse().getName())
			.append("] ")
			.append(reservation.getPaymentAmount())
			.append("원 결제 완료되었습니다.")
			.toString();
		notificationService.create(member.getUser(), content, PARTY, party.getId());
	}

	// 2. 결제 실패
	public void paymentFail(Reservation reservation){
		PartyMember member = reservation.getMember();
		Party party = member.getParty();
		String content = new StringBuilder()
			.append("[")
			.append(party.getCourse().getName())
			.append("] ")
			.append(reservation.getPaymentAmount())
			.append("원 결제에 실패하였습니다. 결제 정보를 다시 한 번 확인하고 재시도해주세요.")
			.toString();
		notificationService.create(member.getUser(), content, PARTY, party.getId());
	}

	// 3. 환불 성공
	public void refundSuccess(Reservation reservation){
		PartyMember member = reservation.getMember();
		Party party = member.getParty();
		String content = new StringBuilder()
			.append("[")
			.append(party.getCourse().getName())
			.append("]의 예약 취소 위약금 ")
			.append(reservation.getPaymentAmount() - reservation.getRefundAmount())
			.append("원을 제외한 ")
			.append(reservation.getRefundAmount())
			.append("원이 환불될 예정입니다.")
			.toString();
		notificationService.create(member.getUser(), content, PARTY, party.getId());
	}
}
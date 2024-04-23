package mallang_trip.backend.domain.payple.service;

import static mallang_trip.backend.domain.payple.exception.PaypleExceptionStatus.BILLING_FAIL;
import static mallang_trip.backend.domain.payple.exception.PaypleExceptionStatus.CANNOT_FOUND_CARD;
import static mallang_trip.backend.domain.reservation.constant.ReservationStatus.PAYMENT_COMPLETE;
import static mallang_trip.backend.domain.reservation.constant.ReservationStatus.PAYMENT_FAILED;
import static mallang_trip.backend.domain.reservation.constant.ReservationStatus.REFUND_COMPLETE;
import static mallang_trip.backend.domain.reservation.constant.ReservationStatus.REFUND_FAILED;
import static mallang_trip.backend.global.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.global.io.BaseResponseStatus.Not_Found;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.payple.dto.BillingResponse;
import mallang_trip.backend.domain.payple.dto.CancelResponse;
import mallang_trip.backend.domain.payple.dto.CardRequest;
import mallang_trip.backend.domain.payple.dto.CardResponse;
import mallang_trip.backend.domain.payple.entity.Card;
import mallang_trip.backend.domain.payple.repository.CardRepository;
import mallang_trip.backend.domain.reservation.entity.Reservation;
import mallang_trip.backend.domain.reservation.repository.ReservationRepository;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.user.service.CurrentUserService;
import mallang_trip.backend.global.io.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaypleService {

	private final CurrentUserService currentUserService;
	private final CardRepository cardRepository;
	private final ReservationRepository reservationRepository;
	private final BillingService billingService;
	private final PaymentNotificationService paymentNotificationService;

	/**
	 * 카드정보를 저장합니다.
	 * <p>
	 * 기존 등록된 카드가 있다면 삭제 후 새로운 카드를 저장합니다.
	 *
	 * @param request 카드등록 요청 결과가 담긴 CardRequest 객체
	 * @throws BaseException Forbidden 카드등록에 실패했거나 현재 유저와 카드등록 유저가 일치하지 않는 경우 발생하는 예외
	 * @return 카드정보가 담긴 CardResponse 객체
	 */
	public CardResponse register(CardRequest request){
		User currentUser = currentUserService.getCurrentUser();
		if(!currentUser.getId().equals(Long.valueOf(request.getUserId()))){
			throw new BaseException(Forbidden);
		}
		// 기존 카드정보 삭제
		delete(currentUser);
		// 카드정보 저장
		Card card = cardRepository.save(Card.builder()
			.user(currentUser)
			.billingKey(request.getBillingKey())
			.cardNumber(request.getCardNumber())
			.cardName(request.getCardName())
			.build());

		return CardResponse.of(card);
	}

	/**
	 * 현재 유저의 등록된 카드가 존재한다면 카드정보를 삭제(soft delete)합니다.
	 */
	public void delete(){
		delete(currentUserService.getCurrentUser());
	}

	/**
	 * 유저의 등록된 카드가 존재한다면 카드 정보를 삭제(soft delete)합니다.
	 *
	 * @param user 카드 정보를 삭제할 User 객체
	 */
	private void delete(User user){
		cardRepository.findByUser(user)
			.ifPresent(card -> cardRepository.delete(card));
	}

	/**
	 * 현재 유저의 등록된 카드 정보를 조회합니다.
	 *
	 * @throws BaseException CANNOT_FOUND_CARD 등록된 카드가 없는 경우 발생하는 예외
	 * @return 등록된 카드 정보가 담긴 CardResponse 객체
	 */
	public CardResponse get(){
		User currentUser = currentUserService.getCurrentUser();
		Optional<Card> card = cardRepository.findByUser(currentUser);
		if(card.isPresent()){
			return CardResponse.of(card.get());
		} else{
			throw new BaseException(CANNOT_FOUND_CARD);
		}
	}

	/**
	 * 결제를 요청합니다.
	 *
	 * @param reservation 결제를 진행할 Reservation 객체
	 * @return 결제 승인 시 true 를, 실패 시 false 를 반환합니다.
	 */
	private boolean billing(Reservation reservation){
		User user = reservation.getMember().getUser();
		Card card = cardRepository.findByUser(user).orElse(null);
		int amount = reservation.getPaymentAmount();
		String goods = reservation.getMember().getParty().getCourse().getName();

		// 등록된 카드가 없는 경우
		if(card == null){
			reservation.changeStatus(PAYMENT_FAILED);
			return false;
		}
		// 빌링 승인 요청
		BillingResponse response = billingService.billing(card.getBillingKey(), goods, amount, user);
		// 빌링이 승인이 되었을 경우
		if(response != null){
			reservation.saveBillingResult(
				response.getPcd_PAY_OID(),
				response.getPcd_PAY_CARDRECEIPT(),
				response.getPcd_PAY_TIME()
			);
			reservation.changeStatus(PAYMENT_COMPLETE);
			return true;
		} else {
			reservation.changeStatus(PAYMENT_FAILED);
			return false;
		}
	}

	/**
	 * 자동 결제를 진행하고, 성공 여부에 따라 알림을 생성합니다.
	 *
	 * @param reservation 자동 결제를 진행할 Reservation 객체
	 */
	public void autoBilling(Reservation reservation){
		if(billing(reservation)){
			paymentNotificationService.paymentSuccess(reservation);
		} else {
			paymentNotificationService.paymentFail(reservation);
		}
	}
	
	/**
	 * 결제가 실패한 예약에 대해 결제를 재시도합니다.
	 *
	 * @param reservationId 실패한 예약에 해당되는 reservation_id 값
	 * @throws BaseException Forbidden 결제가 필요하지 않은 상태일 경우 발생하는 예외
	 * @throws BaseException BILLING_FAIL 결제가 실패했을 경우 발생하는 예외
	 */
	public void manualBilling(Long reservationId){
		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> new BaseException(Not_Found));
		if(!reservation.getStatus().equals(PAYMENT_FAILED)){
			throw new BaseException(Forbidden);
		}
		boolean success = billing(reservation);
		if(!success){
			throw new BaseException(BILLING_FAIL);
		}
	}

	/**
	 * 결제 취소룰 진행합니다.
	 *
	 * @param reservation 취소할 예약에 해당하는 Reservation 객체
	 * @param refundAmount 취소 금액 값
	 */
	public void cancel(Reservation reservation, Integer refundAmount){
		reservation.setRefundAmount(refundAmount);
		if(refundAmount == 0){
			reservation.changeStatus(REFUND_COMPLETE);
			return;
		}

		String oid = reservation.getOrderId();
		String date = reservation.getPayTime().substring(8);
		String amount = String.valueOf(refundAmount);
		CancelResponse response = billingService.cancel(oid, date, amount);

		if(response != null){
			reservation.saveCancelReceipt(response.getPcd_PAY_CARDRECEIPT());
			reservation.changeStatus(REFUND_COMPLETE);
			paymentNotificationService.refundSuccess(reservation);
		} else {
			reservation.changeStatus(REFUND_FAILED);
		}
	}
}

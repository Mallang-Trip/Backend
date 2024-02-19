package mallang_trip.backend.domain.payment.service;

import static mallang_trip.backend.domain.global.io.BaseResponseStatus.CANNOT_FOUND_PAYMENT;
import static mallang_trip.backend.domain.global.io.BaseResponseStatus.PAYMENT_FAIL;
import static mallang_trip.backend.domain.reservation.constant.ReservationStatus.PAYMENT_COMPLETE;
import static mallang_trip.backend.domain.reservation.constant.ReservationStatus.PAYMENT_REQUIRED;
import static mallang_trip.backend.domain.reservation.constant.ReservationStatus.REFUND_COMPLETE;
import static mallang_trip.backend.domain.reservation.constant.ReservationStatus.REFUND_FAILED;
import static mallang_trip.backend.domain.global.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.domain.global.io.BaseResponseStatus.Not_Found;

import java.util.Optional;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.global.io.BaseException;
import mallang_trip.backend.domain.payment.dto.CardResponse;
import mallang_trip.backend.domain.payment.dto.BillingKeyResponse;
import mallang_trip.backend.domain.payment.dto.PaymentRequest;
import mallang_trip.backend.domain.payment.dto.PaymentResponse;
import mallang_trip.backend.domain.payment.entity.Card;
import mallang_trip.backend.domain.payment.entity.Payment;
import mallang_trip.backend.domain.payment.repository.CardRepository;
import mallang_trip.backend.domain.payment.repository.PaymentRepository;
import mallang_trip.backend.domain.reservation.entity.Reservation;
import mallang_trip.backend.domain.reservation.repository.ReservationRepository;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final UserService userService;
    private final PaymentRequestService paymentRequestService;
    private final PaymentNotificationService paymentNotificationService;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final CardRepository cardRepository;

    /**
     * 등록
     */
    public CardResponse register(String authKey) {
        User currentUser = userService.getCurrentUser();
        String customerKey = currentUser.getCustomerKey();
        BillingKeyResponse response = paymentRequestService.postBillingAuthorizations(customerKey,
            authKey);
        if (!response.getCustomerKey().equals(customerKey)) {
            throw new BaseException(Forbidden);
        }
        // 기존 결제정보 삭제
        delete(currentUser);
        // 결제정보 저장
        Card card = saveBillingKeyResponseAsPaymentAndCard(currentUser, response);
        return CardResponse.of(card);
    }

    /**
     * 삭제
     */
    public void delete() {
        delete(userService.getCurrentUser());
    }

    /**
     * 카드 조회
     */
    public CardResponse getCard() {
        User currentUser = userService.getCurrentUser();
        Optional<Payment> optionalPayment = paymentRepository.findByUser(currentUser);
        if (optionalPayment.isEmpty()) {
            throw new BaseException(Not_Found);
        }

        Payment payment = optionalPayment.get();
        Optional<Card> optionalCard = cardRepository.findByPayment(payment);
        if (optionalCard.isEmpty()) {
            throw new BaseException(Not_Found);
        }

        return CardResponse.of(optionalCard.get());
    }

    /**
     * 등록된 결제정보 삭제
     */
    public void delete(User user) {
        paymentRepository.findByUser(user)
            .ifPresent(payment -> paymentRepository.delete(payment));
    }

    /**
     * BillingKeyResponse -> Payment, Card 정보 저장
     */
    public Card saveBillingKeyResponseAsPaymentAndCard(User user, BillingKeyResponse response) {
        Payment payment = paymentRepository.save(Payment.builder()
            .user(user)
            .authenticatedAt(response.getAuthenticatedAt())
            .method(response.getMethod())
            .billingKey(response.getBillingKey())
            .build());
        CardResponse cardResponse = response.getCard();
        Card card = cardRepository.save(Card.builder()
            .payment(payment)
            .issuerCode(cardResponse.getIssuerCode())
            .acquirerCode(cardResponse.getAcquirerCode())
            .number(cardResponse.getNumber())
            .cardType(cardResponse.getCardType())
            .ownerType(cardResponse.getOwnerType())
            .build());
        return card;
    }

    /**
     * 자동 결제
     */
    public void pay(Reservation reservation) {
        User user = reservation.getMember().getUser();
        Optional<Payment> payment = paymentRepository.findByUser(user);
        if (payment.isEmpty()) {
            return;
        }

        PaymentRequest request = PaymentRequest.builder()
            .amount(reservation.getPaymentAmount())
            .customerKey(user.getCustomerKey())
            .orderId(reservation.getId())
            .orderName(reservation.getMember().getParty().getCourse().getName())
            .build();

        PaymentResponse response = paymentRequestService
            .postBilling(payment.get().getBillingKey(), request);

        if (response == null) {
            reservation.changeStatus(PAYMENT_REQUIRED);
            paymentNotificationService.paymentFail(reservation);
        } else {
            reservation.savePaymentKeyAndReceiptUrl(response.getPaymentKey(), response.getReceipt().getUrl());
            reservation.changeStatus(PAYMENT_COMPLETE);
            paymentNotificationService.paymentSuccess(reservation);
        }
    }

    /**
     * 결제 재시도
     */
    public void retryPayment(Long reservationId){
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new BaseException(Not_Found));
        if(!reservation.getStatus().equals(PAYMENT_REQUIRED)){
            throw new BaseException(Forbidden);
        }
        payManually(reservation);
    }

    /**
     * 수동 결제
     */
    public void payManually(Reservation reservation) {
        User user = reservation.getMember().getUser();
        Optional<Payment> payment = paymentRepository.findByUser(user);
        if (payment.isEmpty()) {
            throw new BaseException(CANNOT_FOUND_PAYMENT);
        }

        PaymentRequest request = PaymentRequest.builder()
            .amount(reservation.getPaymentAmount())
            .customerKey(user.getCustomerKey())
            .orderId(reservation.getId())
            .orderName(reservation.getMember().getParty().getCourse().getName())
            .build();

        PaymentResponse response = paymentRequestService
            .postBilling(payment.get().getBillingKey(), request);

        if (response == null) {
            throw new BaseException(PAYMENT_FAIL);
        } else {
            reservation.savePaymentKeyAndReceiptUrl(response.getPaymentKey(), response.getReceipt().getUrl());
            reservation.changeStatus(PAYMENT_COMPLETE);
        }
    }

    /**
     * 결제 취소
     */
    public void cancel(Reservation reservation, Integer refundAmount) {
        Boolean success = paymentRequestService
            .postPaymentsCancel(reservation.getPaymentKey(), refundAmount);
        reservation.setRefundAmount(refundAmount);
        if (success) {
            reservation.changeStatus(REFUND_COMPLETE);
            paymentNotificationService.refundSuccess(reservation);
        } else {
            reservation.changeStatus(REFUND_FAILED);
        }
    }
}

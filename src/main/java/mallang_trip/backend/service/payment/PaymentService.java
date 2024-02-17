package mallang_trip.backend.service.payment;

import static mallang_trip.backend.constant.ReservationStatus.PAYMENT_COMPLETE;
import static mallang_trip.backend.constant.ReservationStatus.PAYMENT_REQUIRED;
import static mallang_trip.backend.constant.ReservationStatus.REFUND_COMPLETE;
import static mallang_trip.backend.constant.ReservationStatus.REFUND_FAILED;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_USER;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;

import java.util.Optional;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.payment.CardResponse;
import mallang_trip.backend.domain.dto.payment.BillingKeyResponse;
import mallang_trip.backend.domain.dto.payment.PaymentRequest;
import mallang_trip.backend.domain.dto.payment.PaymentResponse;
import mallang_trip.backend.domain.entity.payment.Card;
import mallang_trip.backend.domain.entity.payment.Payment;
import mallang_trip.backend.domain.entity.reservation.Reservation;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.payment.CardRepository;
import mallang_trip.backend.repository.payment.PaymentRepository;
import mallang_trip.backend.repository.user.UserRepository;
import mallang_trip.backend.service.user.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final UserService userService;
    private final PaymentRequestService paymentRequestService;
    private final PaymentNotificationService paymentNotificationService;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final CardRepository cardRepository;

    /**
     * 카드 등록
     */
    public void register(String customerKey, String authKey) {
        User user = userRepository.findByCustomerKey(customerKey)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
        BillingKeyResponse response = paymentRequestService.postBillingAuthorizations(customerKey,
            authKey);
        if (response.getCustomerKey() != customerKey) {
            throw new BaseException(Forbidden);
        }
        // 기존 결제정보 삭제
        delete(user);
        // 결제정보 저장
        saveBillingKeyResponseAsPaymentAndCard(user, response);
    }

    /**
     * 등록된 카드 정보 조회
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
     * 현재 유저 결제정보 삭제
     */
    public void delete() {
        delete(userService.getCurrentUser());
    }

    /**
     * BillingKeyResponse -> Payment, Card 정보 저장
     */
    public void saveBillingKeyResponseAsPaymentAndCard(User user, BillingKeyResponse response) {
        Payment payment = paymentRepository.save(Payment.builder()
            .user(user)
            .authenticatedAt(response.getAuthenticatedAt())
            .method(response.getMethod())
            .billingKey(response.getBillingKey())
            .build());
        CardResponse cardResponse = response.getCard();
        cardRepository.save(Card.builder()
            .payment(payment)
            .issuerCode(cardResponse.getIssuerCode())
            .acquirerCode(cardResponse.getAcquirerCode())
            .number(cardResponse.getNumber())
            .cardType(cardResponse.getCardType())
            .ownerType(cardResponse.getOwnerType())
            .build());
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
            .orderId(reservation.getOrderId())
            .orderName(reservation.getMember().getParty().getCourse().getName())
            .build();

        PaymentResponse response = paymentRequestService.postBilling(payment.get().getBillingKey(),
            request);

        if (response == null) {
            reservation.changeStatus(PAYMENT_REQUIRED);
            paymentNotificationService.paymentFail(reservation);
        } else {
            reservation.savePaymentKey(response.getPaymentKey());
            reservation.changeStatus(PAYMENT_COMPLETE);
            paymentNotificationService.paymentSuccess(reservation);
        }
    }


    /**
     * 결제 취소
     */
    public void cancel(Reservation reservation, Integer cancelAmount) {
        Boolean success = paymentRequestService
            .postPaymentsCancel(reservation.getPaymentKey(), cancelAmount);
        reservation.setRefundAmount(cancelAmount);
        if (success) {
            reservation.changeStatus(REFUND_COMPLETE);
            paymentNotificationService.refundSuccess(reservation);
        } else {
            reservation.changeStatus(REFUND_FAILED);
        }
    }
}

package mallang_trip.backend.service.payment;

import static mallang_trip.backend.constant.ReservationStatus.PAYMENT_COMPLETE;
import static mallang_trip.backend.constant.ReservationStatus.PAYMENT_REQUIRED;
import static mallang_trip.backend.constant.ReservationStatus.REFUND_COMPLETE;
import static mallang_trip.backend.constant.ReservationStatus.REFUND_FAILED;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_USER;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URISyntaxException;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.payment.AccessTokenRequest;
import mallang_trip.backend.domain.dto.payment.AccessTokenResponse;
import mallang_trip.backend.domain.dto.payment.PaymentCancelRequest;
import mallang_trip.backend.domain.dto.payment.PaymentMethodsResponse;
import mallang_trip.backend.domain.dto.payment.PaymentRequest;
import mallang_trip.backend.domain.dto.payment.PaymentResponse;
import mallang_trip.backend.domain.entity.party.PartyMember;
import mallang_trip.backend.domain.entity.payment.Payment;
import mallang_trip.backend.domain.entity.reservation.Reservation;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.payment.PaymentRepository;
import mallang_trip.backend.repository.user.UserRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

	private final PaymentRequestService paymentRequestService;
	private final PaymentNotificationService paymentNotificationService;
	private final UserRepository userRepository;
	private final PaymentRepository paymentRepository;

	/**
	 * 토큰 발급
	 */
	public void modifyTokens(String code, String customerKey) {
		User user = userRepository.findByCustomerKey(customerKey)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
		AccessTokenResponse response = paymentRequestService.postAccessToken(
			"AuthorizationCode", code, customerKey, null);
		String accessToken = response.getAccessToken();
		String refreshToken = response.getRefreshToken();

		paymentRepository.findByUser(user)
			.ifPresentOrElse(
				payment -> payment.modifyTokens(accessToken, refreshToken),
				() -> paymentRepository.save(Payment.builder()
					.user(user)
					.accessToken(accessToken)
					.refreshToken(refreshToken)
					.build()));
	}

	/**
	 * 토큰 재발급 (refresh token)
	 */
	private void refreshToken(User user) {
		paymentRepository.findByUser(user).ifPresent(payment -> {
			AccessTokenResponse response = paymentRequestService.postAccessToken(
				"RefreshToken", null, user.getCustomerKey(), payment.getRefreshToken());
			payment.modifyTokens(response.getAccessToken(), response.getRefreshToken());
		});
	}

	/**
	 * 자동 결제
	 */
	public void pay(Reservation reservation) {
		refreshToken(reservation.getMember().getUser());
		PaymentResponse response = paymentRequestService.postPayments(reservation);

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
	 * 수동 결제
	 */

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

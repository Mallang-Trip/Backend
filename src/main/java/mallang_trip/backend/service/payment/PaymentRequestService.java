package mallang_trip.backend.service.payment;

import static mallang_trip.backend.controller.io.BaseResponseStatus.Bad_Request;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Internal_Server_Error;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Unauthorized;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
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
import mallang_trip.backend.repository.payment.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentRequestService {

	@Value("${toss-payment.secretKey}")
	private String secretKey;

	private final String URL = "https://api.tosspayments.com/v1";

	private final PaymentRepository paymentRepository;

	/**
	 * secretKey Base64 인코딩
	 */
	private String encodeSecretKey() {
		String key = secretKey + ":";
		String encodedKey = Base64.getEncoder().encodeToString(key.getBytes());
		return encodedKey;
	}

	/**
	 * Basic Header with SecretKey
	 */
	private HttpHeaders setBasicHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Basic " + encodeSecretKey());
		return headers;
	}

	/**
	 * Bearer Header with AccessToken
	 */
	private HttpHeaders setBearerHeaders(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + accessToken);
		return headers;
	}

	/**
	 * POST /brandpay/authorizations/access-token (토큰 발급)
	 */
	public AccessTokenResponse postAccessToken(String grantType, String code, String customerKey,
		String refreshToken) {
		AccessTokenRequest request = AccessTokenRequest.builder()
			.customerKey(customerKey)
			.grantType(grantType)
			.code(code)
			.refreshToken(refreshToken)
			.build();

		try {
			HttpHeaders headers = setBasicHeaders();
			ObjectMapper objectMapper = new ObjectMapper();
			String body = objectMapper.writeValueAsString(request);
			HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
			ResponseEntity<AccessTokenResponse> responseEntity = restTemplate.postForEntity(
				new URI(URL + "/brandpay/authorizations/access-token"),
				httpBody,
				AccessTokenResponse.class
			);

			HttpStatus statusCode = responseEntity.getStatusCode();
			if (statusCode == HttpStatus.OK) {
				return responseEntity.getBody();
			} else if (statusCode == HttpStatus.BAD_REQUEST) {
				throw new BaseException(Bad_Request);
			} else if (statusCode == HttpStatus.NOT_FOUND) {
				throw new BaseException(Not_Found);
			} else if (statusCode == HttpStatus.UNAUTHORIZED) {
				throw new BaseException(Unauthorized);
			} else {
				throw new BaseException(Internal_Server_Error);
			}
		} catch (RestClientResponseException | URISyntaxException | JsonProcessingException ex) {
			System.out.println(ex.getMessage());
			throw new BaseException(Internal_Server_Error);
		}
	}

	/**
	 * POST /brandpay/payments (자동결제)
	 */
	public PaymentResponse postPayments(Reservation reservation) {
		PartyMember member = reservation.getMember();
		Payment payment = paymentRepository.findByUser(member.getUser())
			.orElseThrow(() -> new BaseException(Not_Found));
		String methodKey = getMethodKey(payment, member.getCardId());
		if (methodKey == null) {
			return null;
		}

		PaymentRequest request = PaymentRequest.builder()
			.customerKey(member.getUser().getCustomerKey())
			.methodKey(methodKey)
			.amount(reservation.getPaymentAmount())
			.orderId(reservation.getOrderId())
			.orderName(member.getParty().getCourse().getName())
			.build();

		try {
			HttpHeaders headers = setBasicHeaders();
			ObjectMapper objectMapper = new ObjectMapper();
			String body = objectMapper.writeValueAsString(request);
			HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
			ResponseEntity<PaymentResponse> responseEntity = restTemplate.postForEntity(
				new URI(URL + "/brandpay/payments"),
				httpBody,
				PaymentResponse.class
			);

			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				return responseEntity.getBody();
			} else {
				return null;
			}

		} catch (RestClientResponseException | URISyntaxException | JsonProcessingException ex) {
			System.out.println("자동 결제 오류: " + ex.getMessage());
			return null;
		}
	}

	/**
	 * POST /payments/{paymentKey}/cancel (결제취소)
	 */
	public Boolean postPaymentsCancel(String paymentKey, Integer cancelAmount) {
		PaymentCancelRequest request = PaymentCancelRequest.builder()
			.cancelReason("여행 예약 취소")
			.cancelAmount(cancelAmount)
			.build();

		try {
			HttpHeaders headers = setBasicHeaders();
			ObjectMapper objectMapper = new ObjectMapper();
			String body = objectMapper.writeValueAsString(request);
			HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
			ResponseEntity<String> responseEntity = restTemplate.postForEntity(
				new URI(URL + "/payments/" + paymentKey + "/cancel"),
				httpBody,
				String.class
			);

			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				return true;
			} else {
				return false;
			}

		} catch (RestClientResponseException | URISyntaxException | JsonProcessingException ex) {
			return false;
		}
	}

	/**
	 * GET /brandpay/payments/methods (결제수단 조회)
	 */
	private PaymentMethodsResponse getPaymentsMethods(Payment payment) {
		HttpHeaders headers = setBearerHeaders(payment.getAccessToken());
		HttpEntity request = new HttpEntity(headers);

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

		try {
			ResponseEntity<PaymentMethodsResponse> responseEntity = restTemplate.exchange(
				new URI(URL + "/brandpay/payments/methods"),
				HttpMethod.GET,
				request,
				PaymentMethodsResponse.class
			);
			return responseEntity.getStatusCode() == HttpStatus.OK ? responseEntity.getBody()
				: null;
		} catch (RestClientResponseException | URISyntaxException ex) {
			System.out.println("결제 수단 조회 오류: " + ex.getMessage());
			return null;
		}
	}

	private String getMethodKey(Payment payment, String cardId) {
		PaymentMethodsResponse response = getPaymentsMethods(payment);
		if (response == null) {
			return null;
		}
		for (PaymentMethodsResponse.Card card : response.getCards()) {
			if (card.getId().equals(cardId)) {
				return card.getMethodKey();
			}
		}
		return null;
	}
}

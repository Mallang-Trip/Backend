package mallang_trip.backend.domain.payment.service;

import static mallang_trip.backend.domain.global.io.BaseResponseStatus.Bad_Request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.global.io.BaseException;
import mallang_trip.backend.domain.payment.dto.BillingKeyRequest;
import mallang_trip.backend.domain.payment.dto.BillingKeyResponse;
import mallang_trip.backend.domain.payment.dto.PaymentCancelRequest;
import mallang_trip.backend.domain.payment.dto.PaymentRequest;
import mallang_trip.backend.domain.payment.dto.PaymentResponse;
import mallang_trip.backend.domain.reservation.entity.Reservation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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

	private final String hostname = "https://api.tosspayments.com/v1";

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
	 * request DTO -> HttpEntity with basic header
	 */
	private HttpEntity<String> setHttpEntity(Object request) throws JsonProcessingException {
		HttpHeaders headers = setBasicHeaders();
		ObjectMapper objectMapper = new ObjectMapper();
		String body = objectMapper.writeValueAsString(request);
		return new HttpEntity<>(body, headers);
	}

	/**
	 * BillingKeyRequest DTO 생성
	 */
	private BillingKeyRequest createBillingKeyRequest(String customerKey, String authKey) {
		return BillingKeyRequest.builder()
			.customerKey(customerKey)
			.authKey(authKey)
			.build();
	}

	/**
	 * PaymentRequest DTO 생성
	 */
	private PaymentRequest createPaymentRequest(Reservation reservation) {
		return PaymentRequest.builder()
			.amount(reservation.getPaymentAmount())
			.customerKey(reservation.getMember().getUser().getCustomerKey())
			.orderId(reservation.getId())
			.orderName(reservation.getMember().getParty().getCourse().getName())
			.build();
	}

	/**
	 * PaymentCancelRequest DTO 생성
	 */
	private PaymentCancelRequest createPaymentCancelRequest(Integer cancelAmount) {
		final String cancelReason = "여행 예약 취소";
		return PaymentCancelRequest.builder()
			.cancelReason(cancelReason)
			.cancelAmount(cancelAmount)
			.build();
	}

	/**
	 * POST /billing/authorizations/issue (카드 빌링키 발급)
	 */
	public BillingKeyResponse postBillingAuthorizations(String customerKey, String authKey) {
		BillingKeyRequest request = createBillingKeyRequest(customerKey, authKey);

		try {
			HttpEntity<String> httpBody = setHttpEntity(request);
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

			ResponseEntity<BillingKeyResponse> responseEntity = restTemplate.postForEntity(
				new URI(hostname + "/billing/authorizations/issue"),
				httpBody,
				BillingKeyResponse.class
			);

			return responseEntity.getBody();

		} catch (RestClientResponseException | URISyntaxException | JsonProcessingException ex) {
			throw new BaseException(Bad_Request);
		}
	}

	/**
	 * POST /billing/{billingKey} (카드 자동결제)
	 * 성공 시 response 객체 반환, 실패 시 null 반환
	 */
	public PaymentResponse postBilling(String billingKey, Reservation reservation) {
		PaymentRequest request = createPaymentRequest(reservation);

		try {
			HttpEntity<String> httpBody = setHttpEntity(request);
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

			ResponseEntity<PaymentResponse> responseEntity = restTemplate.postForEntity(
				new URI(hostname + "/billing/" + billingKey),
				httpBody,
				PaymentResponse.class
			);
			return responseEntity.getBody();

		} catch (RestClientResponseException | URISyntaxException | JsonProcessingException ex) {
			return null;
		}
	}

	/**
	 * POST /payments/{paymentKey}/cancel (결제취소)
	 * 성공 시 true, 실패 시 false 반환
	 */
	public Boolean postPaymentsCancel(String paymentKey, Integer cancelAmount) {
		PaymentCancelRequest request = createPaymentCancelRequest(cancelAmount);

		try {
			HttpEntity<String> httpBody = setHttpEntity(request);
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

			ResponseEntity<String> responseEntity = restTemplate.postForEntity(
				new URI(hostname + "/payments/" + paymentKey + "/cancel"),
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
}

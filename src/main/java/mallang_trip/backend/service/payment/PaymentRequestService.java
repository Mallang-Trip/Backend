package mallang_trip.backend.service.payment;

import static mallang_trip.backend.controller.io.BaseResponseStatus.Bad_Request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.payment.BillingKeyRequest;
import mallang_trip.backend.domain.dto.payment.BillingKeyResponse;
import mallang_trip.backend.domain.dto.payment.PaymentCancelRequest;
import mallang_trip.backend.domain.dto.payment.PaymentRequest;
import mallang_trip.backend.domain.dto.payment.PaymentResponse;
import mallang_trip.backend.repository.payment.PaymentRepository;
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
	 * POST /billing/authorizations/issue (카드 빌링키 발급)
	 */
	public BillingKeyResponse postBillingAuthorizations(String customerKey, String authKey){
		BillingKeyRequest request = BillingKeyRequest.builder()
			.customerKey(customerKey)
			.authKey(authKey)
			.build();
		try{
			HttpHeaders headers = setBasicHeaders();
			ObjectMapper objectMapper = new ObjectMapper();
			String body = objectMapper.writeValueAsString(request);
			HttpEntity<String> httpBody = new HttpEntity<>(body, headers);
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
			ResponseEntity<BillingKeyResponse> responseEntity = restTemplate.postForEntity(
				new URI(URL + "/billing/authorizations/issue"),
				httpBody,
				BillingKeyResponse.class
			);
			return responseEntity.getBody();

		} catch (RestClientResponseException | URISyntaxException | JsonProcessingException ex){
			System.out.println(ex.getMessage());
			throw new BaseException(Bad_Request);
		}
	}

	/**
	 * POST /billing/{billingKey} (카드 자동결제)
	 * 성공 시 response 객체 반환, 실패 시 null 반환
	 */
	public PaymentResponse postBilling(String billingKey, PaymentRequest request){
		try {
			HttpHeaders headers = setBasicHeaders();
			ObjectMapper objectMapper = new ObjectMapper();
			String body = objectMapper.writeValueAsString(request);
			HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
			ResponseEntity<PaymentResponse> responseEntity = restTemplate.postForEntity(
				new URI(URL + "/billing/" + billingKey),
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
}

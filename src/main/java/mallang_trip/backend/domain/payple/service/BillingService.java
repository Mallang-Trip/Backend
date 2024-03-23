package mallang_trip.backend.domain.payple.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.payple.dto.BillingRequest;
import mallang_trip.backend.domain.payple.dto.BillingResponse;
import mallang_trip.backend.domain.payple.dto.CancelRequest;
import mallang_trip.backend.domain.payple.dto.CancelResponse;
import mallang_trip.backend.domain.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Transactional
public class BillingService {

	@Value("${payple.cst-id}")
	private String cstId;

	@Value("${payple.custKey}")
	private String custKey;

	@Value("${payple.cancel-key}")
	private String cancelKey;

	private final String hostname = "https://democpay.payple.kr/php";

	private final RestTemplate restTemplate;
	private final PartnerAuthService partnerAuthService;

	/**
	 * 페이플 POST 요청에 사용될 헤더를 생성합니다.
	 *
	 * @return 생성된 HttpHeaders 객체
	 */
	private HttpHeaders setHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Referer", "https://mallangtrip.com");
		return headers;
	}

	/**
	 * 빌링 승인 요청을 보냅니다.
	 *
	 * @param billingKey 빌링키 값
	 * @param goods 상품명 값
	 * @param amount 결제 금액 값
	 * @param user 결제를 진행하는 유저에 해당하는 User 객체
	 * @return 결제 성공 시, 결제 정보가 담긴 BillingResponse 객체를 반환합니다. 결제 실패 시, null 을 반환합니다.
	 */
	public BillingResponse billing(String billingKey, String goods, int amount, User user) {
		String authKey = partnerAuthService.authBeforeBilling();
		if (authKey == null) {
			return null;
		}

		BillingRequest request = BillingRequest.builder()
			.PCD_CST_ID(cstId)
			.PCD_CUST_KEY(custKey)
			.PCD_AUTH_KEY(authKey)
			.PCD_PAY_TYPE("card")
			.PCD_PAYER_ID(billingKey)
			.PCD_PAY_GOODS(goods)
			.PCD_SIMPLE_FLAG("Y")
			.PCD_PAY_TOTAL(amount)
			.PCD_PAYER_NO(user.getId())
			.PCD_PAYER_NAME(user.getName())
			.PCD_PAYER_HP(user.getPhoneNumber())
			.build();

		try {
			HttpHeaders headers = setHeaders();
			ObjectMapper objectMapper = new ObjectMapper();
			String body = objectMapper.writeValueAsString(request);
			HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

			ResponseEntity<BillingResponse> responseEntity = restTemplate.postForEntity(
				new URI(hostname + "/SimplePayCardAct.php?ACT_=PAYM"),
				httpBody,
				BillingResponse.class
			);

			BillingResponse response = responseEntity.getBody();

			if (response.getPCD_PAY_RST().equals("success")) {
				return response;
			} else {
				return null;
			}

		} catch (RestClientResponseException | URISyntaxException | JsonProcessingException ex) {
			return null;
		}
	}

	/**
	 * 결제 취소 요청을 보냅니다.
	 *
	 * @param oid 원결제 주문번호 값
	 * @param date 원결제 결제일자 값
	 * @param amount 취소 금액 값
	 * @return 취소 성공 시, 취소 정보가 담긴 CancelResponse 객체를 반환합니다. 취소에 실패하면 null 을 반환합니다.
	 */
	public CancelResponse cancel(String oid, String date, String amount) {
		String authKey = partnerAuthService.authBeforeCancel();
		if (authKey == null) {
			return null;
		}

		CancelRequest request = CancelRequest.builder()
			.PCD_CST_ID(cstId)
			.PCD_CUST_KEY(custKey)
			.PCD_AUTH_KEY(authKey)
			.PCD_REFUND_KEY(cancelKey)
			.PCD_PAYCANCEL_FLAG("Y")
			.PCD_PAY_OID(oid)
			.PCD_PAY_DATE(date)
			.PCD_REFUND_TOTAL(amount)
			.build();

		try {
			HttpHeaders headers = setHeaders();
			ObjectMapper objectMapper = new ObjectMapper();
			String body = objectMapper.writeValueAsString(request);
			HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

			ResponseEntity<CancelResponse> responseEntity = restTemplate.postForEntity(
				new URI(hostname + "/account/api/cPayCAct.php"),
				httpBody,
				CancelResponse.class
			);

			CancelResponse response = responseEntity.getBody();
			if (response.getPCD_PAY_RST().equals("success")) {
				return response;
			} else {
				return null;
			}

		} catch (RestClientResponseException | URISyntaxException | JsonProcessingException ex) {
			return null;
		}
	}

}

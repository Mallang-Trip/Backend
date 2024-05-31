package mallang_trip.backend.domain.payple.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mallang_trip.backend.domain.driver.entity.Driver;
import mallang_trip.backend.domain.payple.dto.*;
import mallang_trip.backend.domain.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BillingService {

	@Value("${payple.cancel-key}")
	private String cancelKey;

	@Value("${payple.custKey}")
	private String custKey;

	@Value("${payple.cst-id}")
	private String cstId;

	@Value("${payple.code}")
	private String code;

	// test
	//private final String hostname = "https://democpay.payple.kr/php";

	// real
	private final String hostname = "https://cpay.payple.kr/php";

//	private final String settlement_hostname = "https://hub.payple.kr";

	private final String settlement_hostname = "https://demohub.payple.kr";

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
		PartnerAuthResponse authResponse = partnerAuthService.authBeforeBilling();
		if (authResponse == null) {
			log.info("결제 실패: 파트너 인증 실패, user_id: {}", user.getId());
			return null;
		}

		BillingRequest request = BillingRequest.builder()
				.pcd_CST_ID(authResponse.getCst_id())
				.pcd_CUST_KEY(authResponse.getCustKey())
				.pcd_AUTH_KEY(authResponse.getAuthKey())
				.pcd_PAY_TYPE("card")
				.pcd_PAYER_ID(billingKey)
				.pcd_PAY_GOODS(goods)
				.pcd_SIMPLE_FLAG("Y")
				.pcd_PAY_TOTAL(amount)
				.pcd_PAYER_NO(user.getId())
				.pcd_PAYER_NAME(user.getName())
				.pcd_PAYER_HP(user.getPhoneNumber())
				.build();

		try {
			HttpHeaders headers = setHeaders();
			ObjectMapper objectMapper = new ObjectMapper();
			String body = objectMapper.writeValueAsString(request);
			HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

			ResponseEntity<BillingResponse> responseEntity = restTemplate.postForEntity(
					new URI(hostname + "/SimplePayCardAct.php?ACT_=PAYM"),
					httpBody,
					BillingResponse.class
			);

			BillingResponse response = responseEntity.getBody();

			if (response.getPcd_PAY_RST().equals("success")) {
				return response;
			} else {
				log.info("결제 실패: {}, user_id: {}", response.getPcd_PAY_MSG(), user.getId());
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
		PartnerAuthResponse authResponse = partnerAuthService.authBeforeCancel();
		if (authResponse == null) {
			log.info("결제 취소 실패: 파트너 인증 오류, order_id: {}", oid);
			return null;
		}

		CancelRequest request = CancelRequest.builder()
				.pcd_CST_ID(authResponse.getCst_id())
				.pcd_CUST_KEY(authResponse.getCustKey())
				.pcd_AUTH_KEY(authResponse.getAuthKey())
				.pcd_REFUND_KEY(cancelKey)
				.pcd_PAYCANCEL_FLAG("Y")
				.pcd_PAY_OID(oid)
				.pcd_PAY_DATE(date)
				.pcd_REFUND_TOTAL(amount)
				.build();

		try {
			HttpHeaders headers = setHeaders();
			ObjectMapper objectMapper = new ObjectMapper();
			String body = objectMapper.writeValueAsString(request);
			HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

			ResponseEntity<CancelResponse> responseEntity = restTemplate.postForEntity(
					new URI(hostname + "/account/api/cPayCAct.php"),
					httpBody,
					CancelResponse.class
			);

			CancelResponse response = responseEntity.getBody();
			if (response.getPcd_PAY_RST().equals("success")) {
				return response;
			} else {
				log.info("결제 취소 실패: {}, order_id: {}",response.getPcd_PAY_MSG() , oid);
				return null;
			}

		} catch (RestClientResponseException | URISyntaxException | JsonProcessingException ex) {
			return null;
		}
	}

	/**
	 *
	 * 정산 자동화
	 * @param driver
	 * @param amount : 정산 금액
	 * @param bankNum : 은행 코드
	 *
	 */
	@Async
	public void settlement(Driver driver, String amount, String bankNum) {

		String access_token;
		String billing_tran_id;

//		String webhookUrl="http://your-test-domain.com"; // 테스트용 webhook url

		String group_key;

		/**
		 * 1. 파트너 인증
		 *
		 * */
		SettlementAuthRequest settlementAuthRequest = SettlementAuthRequest.builder()
				.code(code)
				.custKey(custKey)
				.cst_id(cstId)
				.build();
		try{
			HttpHeaders headers = setHeaders();
			ObjectMapper objectMapper = new ObjectMapper();
			String body = objectMapper.writeValueAsString(settlementAuthRequest);
			HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

			ResponseEntity<SettlementAuthResponse> responseEntity = restTemplate.postForEntity(
					new URI(settlement_hostname + "/oauth/token"),
					httpBody,
					SettlementAuthResponse.class
			);

			SettlementAuthResponse response = responseEntity.getBody();
			if(response.getResult().equals("T0000")){
				log.info("파트너 인증 성공");
			} else {
				log.info("파트너 인증 실패: {}", response.getMessage());
				return ;
			}
			access_token = response.getAccess_token();

		} catch (RestClientResponseException | URISyntaxException | JsonProcessingException ex) {
			log.info("정산 인증 실패");
			return ;
		}

		/**
		 * 2. 계좌 인증 요청
		 *
		 *
		 * */
		StringBuilder birthday = new StringBuilder();
		birthday.append(driver.getUser().getBirthday().toString().substring(2, 4))
				.append(driver.getUser().getBirthday().toString().substring(5, 7))
				.append(driver.getUser().getBirthday().toString().substring(8, 10));
		SettlementAccountRequest settlementAccountRequest = SettlementAccountRequest.builder()
				.cst_id(cstId)
				.custKey(custKey)
				.bank_code_std(bankNum) // 은행코드는 수정 필요
				.account_num(driver.getAccountNumber())
				.account_holder_info_type("0") // 0: 개인, 6: 사업자
				.account_holder_info(birthday.toString()) // 생년월일yymmdd or 사업자번호
				.build();

		try{
			HttpHeaders headers = setHeaders();
			headers.set("Authorization", "Bearer " + access_token);
			ObjectMapper objectMapper = new ObjectMapper();
			String body = objectMapper.writeValueAsString(settlementAccountRequest);
			HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

			ResponseEntity<SettlementAccountResponse> responseEntity = restTemplate.postForEntity(
					new URI(settlement_hostname + "/inquiry/real_name"),
					httpBody,
					SettlementAccountResponse.class
			);

			SettlementAccountResponse response = responseEntity.getBody();
			if(response.getResult().equals("A0000")){
				log.info("계좌 인증 성공");
			} else {
				log.info("계좌 인증 실패: {}", response.getMessage());
				return ;
			}
			billing_tran_id = response.getBilling_tran_id();

		} catch (RestClientResponseException | URISyntaxException | JsonProcessingException ex) {
			log.info("계좌 인증 실패");
			return ;
		}

		/**
		 * 3. 빌링키로 이체 대기 요청
		 *
		 * */
		SettlementTransferRequest settlementTransferRequest = SettlementTransferRequest.builder()
				.cst_id(cstId)
				.custKey(custKey)
				.billing_tran_id(billing_tran_id)
				.tran_amt(amount)
				.build();

		try{
			HttpHeaders headers = setHeaders();
			headers.set("Authorization", "Bearer " + access_token);
			ObjectMapper objectMapper = new ObjectMapper();
			String body = objectMapper.writeValueAsString(settlementTransferRequest);
			HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

			ResponseEntity<SettlementTransferResponse> responseEntity = restTemplate.postForEntity(
					new URI(settlement_hostname + "/transfer/request"),
					httpBody,
					SettlementTransferResponse.class
			);

			SettlementTransferResponse response = responseEntity.getBody();
			if(response.getResult().equals("A0000")){
				log.info("이체 대기 성공");
			} else {
				log.info("이체 대기 실패: {}", response.getMessage());
				return ;
			}

			group_key = response.getGroup_key();

		} catch (RestClientResponseException | URISyntaxException | JsonProcessingException ex) {
			log.info("이체 대기 실패");
			return ;
		}

		/**
		 * 4. 이체 실행 요청
		 * */
		SettlementExecuteRequest settlementExecuteRequest = SettlementExecuteRequest.builder()
				.cst_id(cstId)
				.custKey(custKey)
				.group_key(group_key)
//				.webhook_url(webhookUrl)
				.billing_tran_id(billing_tran_id)
				.execute_type("NOW") // NOW: 즉시, CANCEL : 대기 중인 이체 취소
				.build();

		try{
			HttpHeaders headers = setHeaders();
			headers.set("Authorization", "Bearer " + access_token);
			ObjectMapper objectMapper = new ObjectMapper();
			String body = objectMapper.writeValueAsString(settlementExecuteRequest);
			HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

			ResponseEntity<SettlementExecuteResponse> responseEntity = restTemplate.postForEntity(
					new URI(settlement_hostname + "/transfer/execute"),
					httpBody,
					SettlementExecuteResponse.class
			);

			SettlementExecuteResponse response = responseEntity.getBody();
			if(response.getResult().equals("A0000")){
				log.info("이체 실행 성공");
			} else {
				log.info("이체 실행 실패: {}", response.getMessage());
				return ;
			}
		} catch (RestClientResponseException | URISyntaxException | JsonProcessingException ex) {
			log.info("이체 실행 실패");
			return ;
		}

	}


}

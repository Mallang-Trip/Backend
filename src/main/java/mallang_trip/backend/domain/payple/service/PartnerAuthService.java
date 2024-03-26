package mallang_trip.backend.domain.payple.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mallang_trip.backend.domain.payple.dto.PartnerAuthRequest;
import mallang_trip.backend.domain.payple.dto.PartnerAuthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
@Slf4j
public class PartnerAuthService {

	@Value("${payple.cst-id}")
	private String cstId;

	@Value("${payple.custKey}")
	private String custKey;

	//test
	private final String hostname = "https://democpay.payple.kr/php/auth.php";

	//real
	//private final String hostname = "https://cpay.payple.kr/php/auth.php";

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
	 * 페이플 파트너 인증 요청(POST)을 보내는 함수입니다.
	 *
	 * @param request 인증 요청에 사용할 body 정보를 담은 PartnerAuthRequest 객체
	 * @return 인증 성공 시, 페이플 서버로부터 받은 인증 정보를 담은 PartnerAuthResponse 객체를 반환합니다. 인증 실패 시, null 을 반환합니다.
	 */
	private PartnerAuthResponse postPartnerAuthRequest(PartnerAuthRequest request) {
		try {
			HttpHeaders headers = setHeaders();
			ObjectMapper objectMapper = new ObjectMapper();
			String body = objectMapper.writeValueAsString(request);
			HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

			ResponseEntity<PartnerAuthResponse> responseEntity = restTemplate.postForEntity(
				new URI(hostname),
				httpBody,
				PartnerAuthResponse.class
			);

			PartnerAuthResponse response = responseEntity.getBody();
			if (response.getResult().equals("success")) {
				return response;
			} else {
				log.info("파트너 인증 실패: {}", response.getResult_msg());
				return null;
			}

		} catch (RestClientResponseException | URISyntaxException | JsonProcessingException ex) {
			return null;
		}
	}

	/**
	 * 결제 요청을 위해 페이플 파트너 인증을 진행합니다.
	 *
	 * @return 인증 성공 시, 페이플 서버로부터 받은 인증결과를 담은 PartnerAuthResponse 객체를 반환합니다. 인증 실패 시, null 을 반환합니다.
	 */
	public PartnerAuthResponse authBeforeBilling() {
		PartnerAuthRequest request = PartnerAuthRequest.builder()
			.cst_id(cstId)
			.custKey(custKey)
			.pcd_PAY_TYPE("card")
			.pcd_SIMPLE_FLAG("Y")
			.build();

		PartnerAuthResponse response = postPartnerAuthRequest(request);
		if (response == null) {
			return null;
		} else {
			return response;
		}
	}

	/**
	 * 결제 취소을 위해 페이플 파트너 인증을 진행합니다.
	 *
	 * @return 인증 성공 시, 페이플 서버로부터 받은 인증결과를 담은 PartnerAuthResponse 객체를 반환합니다. 인증 실패 시, null 을 반환합니다.
	 */
	public PartnerAuthResponse authBeforeCancel() {
		PartnerAuthRequest request = PartnerAuthRequest.builder()
			.cst_id(cstId)
			.custKey(custKey)
			.pcd_PAYCANCEL_FLAG("Y")
			.build();

		PartnerAuthResponse response = postPartnerAuthRequest(request);
		if (response == null) {
			return null;
		} else {
			return response;
		}
	}

}

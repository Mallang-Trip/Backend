package mallang_trip.backend.domain.payple.service;

import static mallang_trip.backend.global.io.BaseResponseStatus.Internal_Server_Error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.payple.dto.PartnerAuthRequest;
import mallang_trip.backend.domain.payple.dto.PartnerAuthResponse;
import mallang_trip.backend.global.io.BaseException;
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
public class PartnerAuthService {

	@Value("${payple.cstId}")
	private final String cstId;

	@Value("${payple.custKey}")
	private final String custKey;

	private final String hostname = "https://cpay.payple.kr/php/auth.php";

	/**
	 * 파트너 인증 요청에 사용될 헤더를 생성합니다.
	 *
	 * @return 생성된 HttpHeaders 객체
	 */
	private HttpHeaders setHeaders(){
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Referer", "https://www.mallangtrip.com");
		return headers;
	}

	/**
	 * 파트너 인증 요청에 사용될 Body 정보를 담은 DTO 객체를 생성합니다.
	 *
	 * @return 생성된 PartnerAuthRequest 객체
	 */
	private PartnerAuthRequest createPartnerAuthRequest(){
		return PartnerAuthRequest.builder()
			.cst_id(cstId)
			.custKey(custKey)
			.PCD_PAY_TYPE("card")
			.PCD_SIMPLE_FLAG("Y")
			.build();
	}

	/**
	 * 파트너 인증 요청을 보냅니다.
	 *
	 * @throws BaseException Internal_Server_Error 인증에 실패하거나 인증 과정에서 오류가 발생한 경우 발생하는 예외
	 * @return 인증 성공 시 페이플 서버로부터 받은 인증 정보를 담은 PartnerAuthResponse 객체를 반환합니다.
	 */
	public PartnerAuthResponse postPartnerAuthRequest(){
		PartnerAuthRequest request = createPartnerAuthRequest();

		try{
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

			if(!responseEntity.getBody().getResult().equals("success")){
				throw new BaseException(Internal_Server_Error);
			}

			return responseEntity.getBody();

		} catch (RestClientResponseException | URISyntaxException | JsonProcessingException ex) {
			throw new BaseException(Internal_Server_Error);
		}
	}

}

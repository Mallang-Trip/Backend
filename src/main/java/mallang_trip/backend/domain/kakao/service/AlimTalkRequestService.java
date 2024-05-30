package mallang_trip.backend.domain.kakao.service;

import static mallang_trip.backend.global.io.BaseResponseStatus.Internal_Server_Error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mallang_trip.backend.domain.kakao.dto.AlimTalkRequest;
import mallang_trip.backend.global.io.BaseException;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlimTalkRequestService {

	@Value("${naver-cloud-sms.accessKey}")
	private String accessKey;

	@Value("${naver-cloud-sms.secretKey}")
	private String secretKey;

	@Value("${naver-cloud-sms.bizServiceId}")
	private String serviceId;

	private final String hostname = "https://sens.apigw.ntruss.com";

	/**
	 * SecretKey로 암호화한 서명 생성
	 *
	 * @param time 현재 서버 시간
	 * @return 암호화된 서명 값
	 */
	private String makeSignature(Long time) {
		String space = " ";
		String newLine = "\n";
		String method = "POST";
		String url = "/alimtalk/v2/services/" + this.serviceId + "/messages";
		String timestamp = time.toString();
		String accessKey = this.accessKey;
		String secretKey = this.secretKey;

		String message = new StringBuilder()
			.append(method)
			.append(space)
			.append(url)
			.append(newLine)
			.append(timestamp)
			.append(newLine)
			.append(accessKey)
			.toString();
		try {
			SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(signingKey);

			byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
			String encodeBase64String = Base64.encodeBase64String(rawHmac);

			return encodeBase64String;
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
			throw new BaseException(Internal_Server_Error);
		}
	}

	/**
	 * 요청 header 설정
	 *
	 * @return HttpHeaders 객체
	 */
	private HttpHeaders setHeader() {
		Long time = System.currentTimeMillis();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("x-ncp-apigw-timestamp", time.toString());
		headers.set("x-ncp-iam-access-key", accessKey);
		headers.set("x-ncp-apigw-signature-v2", makeSignature(time));

		return headers;
	}

	/**
	 * 알림톡 요청
	 *
	 * @param request 요청 정보가 담긴 AlimTalkRequest DTO
	 */
	public void send(AlimTalkRequest request) {
		try {
			HttpHeaders headers = setHeader();
			ObjectMapper objectMapper = new ObjectMapper();
			String body = objectMapper.writeValueAsString(request);
			HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

			ResponseEntity<String> responseEntity = restTemplate.postForEntity(
				new URI(hostname + "/alimtalk/v2/services/" + this.serviceId + "/messages"),
				httpBody,
				String.class
			);

		} catch (HttpClientErrorException e) {
			log.error("알림톡 발송 요청 실패: {}", e.getResponseBodyAsString());
		} catch (JsonProcessingException | URISyntaxException e) {
			log.error("AlimTalk Internal Error: {}", e.getMessage());
		}
	}
}

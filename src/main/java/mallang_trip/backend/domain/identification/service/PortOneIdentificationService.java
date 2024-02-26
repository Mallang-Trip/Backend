package mallang_trip.backend.domain.identification.service;

import static mallang_trip.backend.global.io.BaseResponseStatus.Bad_Request;
import static mallang_trip.backend.global.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.global.io.BaseResponseStatus.Internal_Server_Error;
import static mallang_trip.backend.global.io.BaseResponseStatus.Not_Found;
import static mallang_trip.backend.global.io.BaseResponseStatus.Unauthorized;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.identification.dto.IdentificationConfirmRequest;
import mallang_trip.backend.domain.identification.dto.IdentificationRequest;
import mallang_trip.backend.domain.identification.dto.IdentificationResponse;
import mallang_trip.backend.domain.identification.dto.IdentificationResultResponse;
import mallang_trip.backend.global.io.BaseException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Transactional
public class PortOneIdentificationService {

	private final String hostname = "https://api.iamport.kr";

	private final PortOneAuthorizationService portOneAuthorizationService;

	private HttpHeaders setBearerHeader() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Bearer " + portOneAuthorizationService.getToken());
		return headers;
	}

	/**
	 * 본인인증 요청
	 */
	public String request(IdentificationRequest request) {
		try {
			HttpHeaders headers = setBearerHeader();
			ObjectMapper objectMapper = new ObjectMapper();
			String body = objectMapper.writeValueAsString(request);
			HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

			ResponseEntity<IdentificationResponse> responseEntity = restTemplate.postForEntity(
				new URI(hostname + "/certifications/otp/request"),
				httpBody,
				IdentificationResponse.class
			);

			return responseEntity.getBody().getResponse().getImp_uid();

		} catch (HttpClientErrorException e) {
			handleHttpClientErrorException(e);
			throw new BaseException(Internal_Server_Error);
		} catch (JsonProcessingException | URISyntaxException e) {
			throw new BaseException(Internal_Server_Error);
		}
	}

	/**
	 * 본인인증 완료
	 */
	public String confirm(String impUid, String otp) {
		IdentificationConfirmRequest request = IdentificationConfirmRequest
			.builder()
			.otp(otp)
			.build();

		try {
			HttpHeaders headers = setBearerHeader();
			ObjectMapper objectMapper = new ObjectMapper();
			String body = objectMapper.writeValueAsString(request);
			HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

			ResponseEntity<IdentificationResultResponse> responseEntity = restTemplate.postForEntity(
				new URI(hostname + "/certifications/otp/confirm/" + impUid),
				httpBody,
				IdentificationResultResponse.class
			);

			if (responseEntity.getBody().getResponse().getCertified()) {
				return responseEntity.getBody().getResponse().getImp_uid();
			} else {
				throw new BaseException(Forbidden);
			}
		} catch (HttpClientErrorException e) {
			handleHttpClientErrorException(e);
			throw new BaseException(Internal_Server_Error);
		} catch (JsonProcessingException | URISyntaxException e) {
			throw new BaseException(Internal_Server_Error);
		}
	}

	/**
	 * 본인인증 결과 조회
	 */
	public IdentificationResultResponse get(String impUid) {
		try {
			HttpHeaders headers = setBearerHeader();
			HttpEntity<String> httpBody = new HttpEntity<>(headers);
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

			ResponseEntity<IdentificationResultResponse> responseEntity = restTemplate.exchange(
				new URI(hostname + "/certifications/" + impUid),
				HttpMethod.GET,
				httpBody,
				IdentificationResultResponse.class
			);

			if (responseEntity.getBody().getResponse().getCertified()) {
				return responseEntity.getBody();
			} else {
				throw new BaseException(Forbidden);
			}
		} catch (HttpClientErrorException e) {
			handleHttpClientErrorException(e);
			throw new BaseException(Internal_Server_Error);
		} catch (URISyntaxException e) {
			throw new BaseException(Internal_Server_Error);
		}
	}

	private void handleHttpClientErrorException(HttpClientErrorException e) {
		HttpStatus statusCode = e.getStatusCode();
		if (statusCode.equals(BAD_REQUEST)) {
			throw new BaseException(Bad_Request);
		} else if (statusCode.equals(UNAUTHORIZED)) {
			throw new BaseException(Unauthorized);
		} else if (statusCode.equals(NOT_FOUND)) {
			throw new BaseException(Not_Found);
		}
	}
}

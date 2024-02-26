package mallang_trip.backend.domain.identification.service;

import static mallang_trip.backend.global.io.BaseResponseStatus.Internal_Server_Error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.identification.dto.PortOneAccessTokenRequest;
import mallang_trip.backend.domain.identification.dto.PortOneAccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
public class PortOneAuthorizationService {

	@Value("${port-one.impKey}")
	private String impKey;

	@Value("${port-one.impSecret}")
	private String impSecret;

	private final String hostname = "https://api.iamport.kr";

	private HttpHeaders setHeader() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}

	private PortOneAccessTokenRequest createRequest() {
		return PortOneAccessTokenRequest.builder()
			.imp_key(impKey)
			.imp_secret(impSecret)
			.build();
	}

	/**
	 * PortOne API access-token 발급
	 */
	public String getToken() {
		PortOneAccessTokenRequest request = createRequest();

		try {
			HttpHeaders headers = setHeader();
			ObjectMapper objectMapper = new ObjectMapper();
			String body = objectMapper.writeValueAsString(request);
			HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

			ResponseEntity<PortOneAccessTokenResponse> responseEntity = restTemplate.postForEntity(
				new URI(hostname + "/users/getToken"),
				httpBody,
				PortOneAccessTokenResponse.class
			);

			return responseEntity.getBody().getResponse().getAccess_token();

		} catch (HttpClientErrorException | JsonProcessingException | URISyntaxException e) {
			throw new BaseException(Internal_Server_Error);
		}
	}
}

package mallang_trip.backend.domain.identification.service;

import static mallang_trip.backend.domain.global.io.BaseResponseStatus.Bad_Request;
import static mallang_trip.backend.domain.global.io.BaseResponseStatus.Internal_Server_Error;
import static mallang_trip.backend.domain.global.io.BaseResponseStatus.Not_Found;
import static mallang_trip.backend.domain.global.io.BaseResponseStatus.Unauthorized;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.identification.dto.IdentificationConfirmRequest;
import mallang_trip.backend.domain.identification.dto.IdentificationRequest;
import mallang_trip.backend.domain.identification.dto.IdentificationResponse;
import mallang_trip.backend.domain.identification.dto.IdentificationResultResponse;
import mallang_trip.backend.domain.global.io.BaseException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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

            Integer code = responseEntity.getBody().getCode();
            if (code == 0) {
                return responseEntity.getBody().getImp_uid();
            } else if (code == 400) {
                throw new BaseException(Bad_Request);
            } else if (code == 401) {
                throw new BaseException(Unauthorized);
            } else {
                throw new BaseException(Internal_Server_Error);
            }

        } catch (RestClientResponseException | JsonProcessingException | URISyntaxException e) {
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

            Integer code = responseEntity.getBody().getCode();
            if (code == 0) {
                return responseEntity.getBody().getImp_uid();
            } else if (code == 400) {
                throw new BaseException(Bad_Request);
            } else if (code == 401) {
                throw new BaseException(Unauthorized);
            } else if (code == 404) {
                throw new BaseException(Not_Found);
            } else {
                throw new BaseException(Internal_Server_Error);
            }

        } catch (RestClientResponseException | JsonProcessingException | URISyntaxException e) {
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
                new URI(hostname + "/certifications/otp/confirm/" + impUid),
                HttpMethod.GET,
                httpBody,
                IdentificationResultResponse.class
            );

            Integer code = responseEntity.getBody().getCode();
            if (code == 0) {
                return responseEntity.getBody();
            } else if (code == 400) {
                throw new BaseException(Bad_Request);
            } else if (code == 401) {
                throw new BaseException(Unauthorized);
            } else if (code == 404) {
                throw new BaseException(Not_Found);
            } else {
                throw new BaseException(Internal_Server_Error);
            }

        } catch (RestClientResponseException | URISyntaxException e) {
            throw new BaseException(Internal_Server_Error);
        }
    }
}

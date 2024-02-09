package mallang_trip.backend.service.payment;

import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_USER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.payment.AccessTokenRequest;
import mallang_trip.backend.domain.dto.payment.AccessTokenResponse;
import mallang_trip.backend.domain.entity.payment.Payment;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.payment.PaymentRepository;
import mallang_trip.backend.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    @Value("${toss-payment.secretKey}")
    private String secretKey;

    private final String url = "https://api.tosspayments.com/v1/brandpay/authorizations/access-token";

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public void save(String code, String customerKey)
        throws URISyntaxException, JsonProcessingException {
        User user = userRepository.findByCustomerKey(customerKey)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
        AccessTokenResponse response = getAccessToken(code, customerKey);
        paymentRepository.save(Payment.builder()
            .user(user)
            .accessToken(response.getAccessToken())
            .refreshToken(response.getRefreshToken())
            .build());
    }

    public AccessTokenResponse getAccessToken(String code, String customerKey)
        throws JsonProcessingException, URISyntaxException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + encodeSecretKey());

        AccessTokenRequest request = AccessTokenRequest.builder()
            .customerKey(customerKey)
            .grantType("AuthorizationCode")
            .code(code)
            .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(request);
        HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        AccessTokenResponse response = restTemplate.postForObject(new URI(url), httpBody, AccessTokenResponse.class);

        return response;
    }

    private String encodeSecretKey(){
        String key = secretKey + ":";
        String encodedKey = Base64.getEncoder().encodeToString(key.getBytes());
        return encodedKey;
    }
}

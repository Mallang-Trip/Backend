package mallang_trip.backend.service.payment;

import static mallang_trip.backend.constant.ReservationStatus.PAYMENT_COMPLETE;
import static mallang_trip.backend.constant.ReservationStatus.PAYMENT_REQUIRED;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_USER;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;

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
import mallang_trip.backend.domain.dto.payment.PaymentMethodsResponse;
import mallang_trip.backend.domain.dto.payment.PaymentRequest;
import mallang_trip.backend.domain.dto.payment.PaymentResponse;
import mallang_trip.backend.domain.entity.party.PartyMember;
import mallang_trip.backend.domain.entity.payment.Payment;
import mallang_trip.backend.domain.entity.reservation.Reservation;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.payment.PaymentRepository;
import mallang_trip.backend.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Transactional
public class TossBrandPayService {

    @Value("${toss-payment.secretKey}")
    private String secretKey;

    private final String BRAND_PAY_URL = "https://api.tosspayments.com/v1/brandpay";

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    /**
     * Redirection URL 처리 로직
     */
    public void modify(String code, String customerKey)
        throws URISyntaxException, JsonProcessingException {
        User user = userRepository.findByCustomerKey(customerKey)
            .orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
        AccessTokenResponse response = authorizationCode(code, customerKey);
        String accessToken = response.getAccessToken();
        String refreshToken = response.getRefreshToken();

        paymentRepository.findByUser(user)
            .ifPresentOrElse(
                payment -> payment.modifyTokens(accessToken, refreshToken),
                () -> paymentRepository.save(Payment.builder()
                    .user(user)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build()));
    }

    /**
     * RefreshToken 으로 AccessToken 재발급
     */
    public void refreshPayment(Payment payment)
        throws URISyntaxException, JsonProcessingException {
        AccessTokenResponse response = refreshToken(payment.getRefreshToken(),
            payment.getUser().getCustomerKey());
        payment.modifyTokens(response.getAccessToken(), response.getRefreshToken());
    }

    private AccessTokenResponse authorizationCode(String code, String customerKey)
        throws URISyntaxException, JsonProcessingException {
        return getAccessToken("AuthorizationCode", customerKey, code, null);
    }

    private AccessTokenResponse refreshToken(String refreshToken, String customerKey)
        throws URISyntaxException, JsonProcessingException {
        return getAccessToken("RefreshToken", customerKey, null, refreshToken);
    }

    /**
     * 토스페이먼츠 [POST /authorizations/access-token] API 요청 (accessToken 생성)
     */
    private AccessTokenResponse getAccessToken(String grantType, String customerKey, String code,
        String refreshToken)
        throws JsonProcessingException, URISyntaxException {
        AccessTokenRequest request = AccessTokenRequest.builder()
            .customerKey(customerKey)
            .grantType(grantType)
            .code(code)
            .refreshToken(refreshToken)
            .build();

        HttpHeaders headers = setBasicHeaders();
        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(request);
        HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        ResponseEntity<AccessTokenResponse> responseEntity = restTemplate.postForEntity(
            new URI(BRAND_PAY_URL + "/authorizations/access-token"),
            httpBody,
            AccessTokenResponse.class
        );

        if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            throw new BaseException(Forbidden);
        }

        return responseEntity.getBody();
    }

    private HttpHeaders setBasicHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + encodeSecretKey());
        return headers;
    }

    private HttpHeaders setBearerHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        return headers;
    }

    private String encodeSecretKey() {
        String key = secretKey + ":";
        String encodedKey = Base64.getEncoder().encodeToString(key.getBytes());
        return encodedKey;
    }

    /**
     * 토스페이먼츠 [POST /payments] API (자동결제 실행)
     */
    public void pay(Reservation reservation)
        throws URISyntaxException, JsonProcessingException {
        PartyMember member = reservation.getMember();
        PaymentRequest request = PaymentRequest.builder()
            .customerKey(member.getUser().getCustomerKey())
            .methodKey(getMethodKey(member))
            .amount(reservation.getPaymentAmount())
            .orderId(reservation.getOrderId())
            .orderName(member.getParty().getCourse().getName())
            .build();

        HttpHeaders headers = setBasicHeaders();
        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(request);
        HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        ResponseEntity<PaymentResponse> responseEntity = restTemplate.postForEntity(
            new URI(BRAND_PAY_URL + "/payments"),
            httpBody,
            PaymentResponse.class
        );

        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            PaymentResponse response = responseEntity.getBody();
            reservation.savePaymentKey(response.getPaymentKey());
            reservation.changeStatus(PAYMENT_COMPLETE);
        } else {
            reservation.changeStatus(PAYMENT_REQUIRED);
        }
    }

    /**
     * 토스페이먼츠 [GET /payments/methods] API 요청 후 일치하는 methodKey 조회
     */
    private String getMethodKey(PartyMember member) throws URISyntaxException {
        Payment payment = paymentRepository.findByUser(member.getUser())
            .orElseThrow(() -> new BaseException(Not_Found));
        HttpHeaders headers = setBearerHeaders(payment.getAccessToken());
        HttpEntity request = new HttpEntity(headers);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        ResponseEntity<PaymentMethodsResponse> responseEntity = restTemplate.exchange(
            new URI(BRAND_PAY_URL + "/payments/methods"),
            HttpMethod.GET,
            request,
            PaymentMethodsResponse.class
        );

        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return findMethodKey(responseEntity.getBody(), member.getCardId());
        }
        return null;
    }

    private String findMethodKey(PaymentMethodsResponse response, String cardId) {
        for (PaymentMethodsResponse.Card card : response.getCards()) {
            if (card.getId().equals(cardId)) {
                return card.getMethodKey();
            }
        }
        return null;
    }

    /**
     * 토스페이먼츠 [POST /v1/payments/{paymentKey}/cancel] API 요청 (결제취소)
     */
    public void cancel(Reservation reservation){

    }

}

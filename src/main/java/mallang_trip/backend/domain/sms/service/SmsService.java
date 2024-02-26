package mallang_trip.backend.domain.sms.service;

import static mallang_trip.backend.global.io.BaseResponseStatus.Bad_Request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.sms.repository.SmsCertification;
import mallang_trip.backend.domain.sms.dto.SmsMessage;
import mallang_trip.backend.domain.sms.dto.SmsRequest;
import mallang_trip.backend.domain.sms.dto.SmsResponse;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
public class SmsService {

    private final SmsCertification smsCertification;

    @Value("${naver-cloud-sms.accessKey}")
    private String accessKey;

    @Value("${naver-cloud-sms.secretKey}")
    private String secretKey;

    @Value("${naver-cloud-sms.serviceId}")
    private String serviceId;

    @Value("${naver-cloud-sms.senderPhone}")
    private String phone;

     /** 인증번호 문자 발송  */
    public void sendSmsCertification(String phoneNumber)
        throws UnsupportedEncodingException, URISyntaxException, NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
        // 인증코드 생성
        String code = createCode();
        // redis 저장
        smsCertification.createSmsCertification(phoneNumber, code);
        // sens 발송
        String content = createCertificationContent(code);
        SmsResponse response = sendSms(phoneNumber, content);
        // 발송 실패 시
        if (!response.getStatusCode().equals("202")) {
            throw new BaseException(Bad_Request);
        }
    }

    /** 인증번호 일치 확인 -> 일치 시 인증번호 삭제 */
    public Boolean verifyAndDeleteCode(String phoneNumber, String code) {
        if (smsCertification.hasKey(phoneNumber) && smsCertification.getSmsCertification(
            phoneNumber).equals(code)) {
            smsCertification.removeSmsCertification(phoneNumber);
            return true;
        } else {
            return false;
        }
    }

    /** 인증번호 일치 확인 -> 일치 시 인증번호 연장 */
    public Boolean verifyAndExtendCode(String phoneNumber, String code) {
        if (smsCertification.hasKey(phoneNumber) && smsCertification.getSmsCertification(
            phoneNumber).equals(code)) {
            smsCertification.extendSmsCertification(phoneNumber);
            return true;
        } else {
            return false;
        }
    }

     /** SMS content 생성 */
    private String createCertificationContent(String code) {
        String content = "[말랑트립] " + code + " 인증 번호입니다.";
        return content;
    }

     /** 인증코드 생성  */
    private String createCode() {
        StringBuffer code = new StringBuffer();
        Random rnd = new Random();
        for (int i = 0; i < 6; i++) {
            code.append((rnd.nextInt(10)));
        }
        return code.toString();
    }

    /** SENS API 형식 */
    private String makeSignature(Long time)
        throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        String space = " ";
        String newLine = "\n";
        String method = "POST";
        String url = "/sms/v2/services/" + this.serviceId + "/messages";
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

        SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);

        byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
        String encodeBase64String = Base64.encodeBase64String(rawHmac);

        return encodeBase64String;
    }

    /** SENS 문자 발송 API 요청 */
    private SmsResponse sendSms(String to, String content)
        throws JsonProcessingException, RestClientException, URISyntaxException, InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
        Long time = System.currentTimeMillis();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-ncp-apigw-timestamp", time.toString());
        headers.set("x-ncp-iam-access-key", accessKey);
        headers.set("x-ncp-apigw-signature-v2", makeSignature(time));

        List<SmsMessage> messages = new ArrayList<>();
        messages.add(SmsMessage.builder().to(to).content(content).build());

        SmsRequest request = SmsRequest.builder()
            .type("SMS")
            .contentType("COMM")
            .countryCode("82")
            .from(phone)
            .content(content)
            .messages(messages)
            .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(request);
        HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        SmsResponse response = restTemplate.postForObject(
            new URI("https://sens.apigw.ntruss.com/sms/v2/services/" + serviceId + "/messages"),
            httpBody, SmsResponse.class);

        return response;
    }
}

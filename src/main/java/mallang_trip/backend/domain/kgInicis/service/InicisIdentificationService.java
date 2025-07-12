package mallang_trip.backend.domain.kgInicis.service;

import static mallang_trip.backend.global.io.BaseResponseStatus.Bad_Request;
import static mallang_trip.backend.global.io.BaseResponseStatus.Conflict;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mallang_trip.backend.domain.dreamSecurity.dto.IdentificationResult;
import mallang_trip.backend.domain.dreamSecurity.service.IdentificationResultService;
import mallang_trip.backend.domain.kgInicis.dto.InicisAuthUserInfoDto;
import mallang_trip.backend.domain.kgInicis.dto.InicisCallbackRequestDto;
import mallang_trip.backend.domain.kgInicis.dto.InicisFormInfoResponseDto;
import mallang_trip.backend.domain.kgInicis.dto.InicisResultResponseDto;
import mallang_trip.backend.domain.user.repository.UserRepository;
import mallang_trip.backend.global.io.BaseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j(topic = "IDENTIFICATION")
@RequiredArgsConstructor
/**
 * KG이니시스 본인인증 서빗,
 * */
public class InicisIdentificationService {

    private final SEED_ENC seedEnc;
    private final IdentificationResultService identificationResultService;

    private static final SecureRandom RANDOM = new SecureRandom();
    private final UserRepository userRepository;

    @Value("${identification.mid}")
    private String MID;

    @Value("${identification.api-key}")
    private String API_KEY;

    /**
     * 본인인증 인증창 요청 데이터
     * */
    public InicisFormInfoResponseDto generateFormInfo(){
        String mTxId = generateTxId();
        InicisFormInfoResponseDto responseDto = new InicisFormInfoResponseDto(
            MID,
            "03",// 본인인증
            mTxId,
            "https://mallangtrip-server.com/api/inicis/callback",
            "https://mallangtrip-server.com/api/inicis/callback",
            sha256Hex(MID + mTxId + API_KEY),
            "N",
            "isUseToken=Y"
            );

        log.debug("본인인증 폼 데이터 요청: {}", responseDto);
        return responseDto;
    }


    /**
     * KG이니시스 본인인증 콜백
     * */
    public InicisAuthUserInfoDto verifyAndFetchUser(InicisCallbackRequestDto req) {

        if (!"0000".equals(req.resultCode())) {
            log.warn("본인인증 실패: {}", req.resultCode());
            throw new BaseException(Bad_Request);
        }

        // authRequestUrl 도메인 검증
        String url = req.authRequestUrl();
        if (!(url.startsWith("https://kssa.inicis.com") ||
            url.startsWith("https://fcsa.inicis.com"))) {
            log.warn("잘못된 url: {}", url);
            throw new BaseException(Bad_Request);
        }

        log.debug("본인인증 성공: req: {}", req);

        InicisAuthUserInfoDto encryptedUserData = getUserInfo(url, Map.of(
            "mid", MID,
            "txId", req.txId()
        ));

        log.debug("encrypt success. data: {}", encryptedUserData);

        String token = req.token();

        InicisAuthUserInfoDto decryptUserInfo = new InicisAuthUserInfoDto(
            encryptedUserData.providerDevCd(),
            encryptedUserData.resultCode(),
            encryptedUserData.resultMsg(),
            seedEnc.decrypt(encryptedUserData.isForeign(), token),
            seedEnc.decrypt(encryptedUserData.userPhone(), token),
            encryptedUserData.mTxId(),
            encryptedUserData.txId(),
            encryptedUserData.svcCd(),
            seedEnc.decrypt(encryptedUserData.userName(), token),
            seedEnc.decrypt(encryptedUserData.signedData(), token),
            seedEnc.decrypt(encryptedUserData.userGender(), token),
            seedEnc.decrypt(encryptedUserData.userCi(), token),
            seedEnc.decrypt(encryptedUserData.userCi2(), token),
            seedEnc.decrypt(encryptedUserData.userDi(), token),
            seedEnc.decrypt(encryptedUserData.userBirthday(), token)
            );

        log.debug("decrypt success. decrypted info: {}", decryptUserInfo);

        return decryptUserInfo;
    }


    public InicisResultResponseDto storeIdentificationInfo(InicisAuthUserInfoDto identifiedInfo) {

        IdentificationResult identificationResult = IdentificationResult.builder()
            .userName(identifiedInfo.userName())
            .ci(identifiedInfo.userCi())
            .di(identifiedInfo.userDi())
            .userPhone(identifiedInfo.userPhone())
            .userBirthday(LocalDate.parse(identifiedInfo.userBirthday(), DateTimeFormatter.ofPattern("yyyyMMdd")))
            .userGender(identifiedInfo.userGender())
            .userNation(identifiedInfo.isForeign())
            .build();

        /*중복가입 방지*/
        if(userRepository.existsByPhoneNumber(identifiedInfo.userPhone())) {
            throw new BaseException(Conflict);
        }

        identificationResultService.saveIdentificationResult(identifiedInfo.mTxId(), identificationResult);

        InicisResultResponseDto response = new InicisResultResponseDto(identifiedInfo.mTxId());

        log.debug("인증정보 저장 성공. mTxId: {}", identifiedInfo.mTxId());
        return response;
    }

    /**
     * 본인인증 콜백값을 통해 사용자 정보를 추출하는 헬퍼 메소드
     * */
    private InicisAuthUserInfoDto getUserInfo(String url, Map<String, Object> body) {

        RestTemplate restTemplate = new RestTemplate();

        /*UTF-8 컨버팅*/
        restTemplate.getMessageConverters().stream()
            .filter(c -> c instanceof StringHttpMessageConverter)
            .map(StringHttpMessageConverter.class::cast)
            .forEach(c -> c.setDefaultCharset(StandardCharsets.UTF_8));

        restTemplate.getMessageConverters().stream()
            .filter(c -> c instanceof MappingJackson2HttpMessageConverter)
            .map(MappingJackson2HttpMessageConverter.class::cast)
            .forEach(c -> c.setDefaultCharset(StandardCharsets.UTF_8));

        /*헤더 설정*/
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            // 1) 원시 JSON 문자열로 수신
            String responseJson = restTemplate.postForObject(url, entity, String.class);
            log.debug("Inicis 인증 결과 raw JSON: {}", responseJson);

            // 2) 빈 응답 체크
            if (responseJson == null || responseJson.isBlank()) {
                throw new RuntimeException("Inicis API 응답이 비어 있습니다.");
            }

            // 3) ObjectMapper 로 파싱
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseJson);

            // 4) 필수 필드 유효성 검사 (예: txId 반드시 존재)
            if (!root.hasNonNull("txId")) {
                throw new RuntimeException("Inicis API 응답에 txId가 없습니다.");
            }

            // 5) DTO 로 변환
            InicisAuthUserInfoDto dto = mapper.treeToValue(root, InicisAuthUserInfoDto.class);
            return dto;

        } catch (ArrayIndexOutOfBoundsException ex) {
            // split() 같은 로직에서 음수∘0 길이 배열 참조 예외 방어
            throw new RuntimeException("Inicis 응답 파싱 중 배열 인덱스 오류가 발생했습니다.", ex);

        } catch (JsonProcessingException ex) {
            // JSON 파싱 자체에서 에러
            throw new RuntimeException("Inicis 응답 JSON 파싱 오류", ex);

        } catch (RestClientException ex) {
            // HTTP 요청/응답 처리 오류
            throw new RuntimeException("Inicis API 호출 오류", ex);

        } catch (Exception ex) {
            // 그 외 예외
            throw new RuntimeException("인증 결과조회 API 처리 중 오류", ex);
        }
    }

    /**SHA-256 해시 함수*/
    private String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return new String(Hex.encode(digest));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static String generateTxId() {

        char[] ALPHANUM =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
                .toCharArray();

        char[] buf = new char[20];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = ALPHANUM[RANDOM.nextInt(ALPHANUM.length)];
        }
        return new String(buf);
    }

}

package mallang_trip.backend.domain.kgInicis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Inicis 콜백 요청 DTO (STEP2 콜백)
 */
public record InicisCallbackRequestDto(

    /**
     * 결과 코드 ("0000": 정상, 이외: 오류)
     */
    @JsonProperty("resultCode")
    String resultCode,

    /**
     * 결과 메시지 (UTF-8 URL Encoding)
     */
    @JsonProperty("resultMsg")
    String resultMsg,

    /**
     * 통합인증 트랜잭션 ID
     */
    @JsonProperty("txId")
    String txId,

    /**
     * 결과조회 요청 URL
     */
    @JsonProperty("authRequestUrl")
    String authRequestUrl,

    /**
     * 토큰 값 (isUseToken=Y인 경우 Base64)
     */
    @JsonProperty("token")
    String token

) {}


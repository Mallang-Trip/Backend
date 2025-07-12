package mallang_trip.backend.domain.kgInicis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Inicis Form Info 응답 DTO (STEP1 통합인증 요청 정보)
 */
public record InicisFormInfoResponseDto(

    /**
     * 가맹점 ID
     */
    @JsonProperty("mid")
    String mid,

    /**
     * 요청 구분 코드 ("01": 간편인증, "02": 전자서명, "03": 본인확인)
     */
    @JsonProperty("reqSvcCd")
    String reqSvcCd,

    /**
     * 가맹점 트랜잭션 ID (20 byte, 유일값)
     */
    @JsonProperty("mTxId")
    String mTxId,

    /**
     * 성공 리다이렉트 URL
     */
    @JsonProperty("successUrl")
    String successUrl,

    /**
     * 실패 리다이렉트 URL
     */
    @JsonProperty("failUrl")
    String failUrl,

    /**
     * 요청 검증용 SHA256 해시 (mid + mTxId + apikey)
     */
    @JsonProperty("authHash")
    String authHash,

    /**
     * 고정 사용자 인증 여부 ("Y": 고정, "N": 미사용)
     */
    @JsonProperty("flgFixedUser")
    String flgFixedUser,

    /**
     * 추가 파라미터 (isUseToken=Y 등)
     */
    @JsonProperty("reservedMsg")
    String reservedMsg

) {}


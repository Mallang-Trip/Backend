package mallang_trip.backend.domain.kgInicis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Inicis 폼 응답(트랜잭션 id반환)
 */
public record InicisResultResponseDto(

    /**
     * 트랜잭션 id.
     * 호환성을 위해 기존 OK-MOBILE(드림시큐리티)의 impUid 필드명 사용
     */
    @JsonProperty("impUid")
    String mTxId
) {}

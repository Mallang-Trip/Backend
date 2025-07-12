package mallang_trip.backend.domain.kgInicis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Inicis 결합인증 결과조회 사용자 데이터 DTO
 * STEP4 결과조회 응답 전체
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InicisAuthUserInfoDto(

    /**
     * 제휴사 코드 (제휴사별 식별자)
     */
    @JsonProperty("providerDevCd")
    String providerDevCd,

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
     * 내·외국인 구분 ("0": 내국인, "1": 외국인)
     */
    @JsonProperty("isForeign")
    String isForeign,

    /**
     * 사용자 전화번호 (SEED 암호화)
     */
    @JsonProperty("userPhone")
    String userPhone,

    /**
     * 가맹점 트랜잭션 ID
     * 인증 요청시 mTxId 필드에 설정된 값
     */
    @JsonProperty("mTxId")
    String mTxId,

    /**
     * 통합인증 트랜잭션 ID
     */
    @JsonProperty("txId")
    String txId,

    /**
     * 요청 구분 코드 ("01":간편인증, "02":전자서명, "03":본인확인)
     */
    @JsonProperty("svcCd")
    String svcCd,

    /**
     * 사용자 이름 (복호화 후 원본)
     */
    @JsonProperty("userName")
    String userName,

    /**
     * 전자서명용 서명 데이터 (SEED 암호화)
     */
    @JsonProperty("signedData")
    String signedData,

    /**
     * 사용자 성별 ("M": 남성, "F": 여성)
     */
    @JsonProperty("userGender")
    String userGender,

    /**
     * 사용자 CI 데이터 (복호화된 값)
     */
    @JsonProperty("userCi")
    String userCi,


    /**
     * 사용자 CI2 데이터 (복호화된 값)
     */
    @JsonProperty("userCi2")
    String userCi2,

    /**
     * 사용자 DI 데이터 (복호화된 값)
     */
    @JsonProperty("userDi")
    String userDi,

    /**
     * 사용자 생년월일 [YYYYMMDD]
     */
    @JsonProperty("userBirthday")
    String userBirthday

) {}

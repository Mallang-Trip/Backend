package mallang_trip.backend.domain.reservation.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mallang_trip.backend.global.io.ResponseStatus;

@Getter
@AllArgsConstructor
public enum PromotionCodeExceptionStatus implements ResponseStatus {
    PROMOTION_CODE_NOT_FOUND(404, "존재하지 않는 프로모션 코드입니다."),
    PROMOTION_CODE_ALREADY_USED(400, "이미 사용된 프로모션 코드입니다."),
    PROMOTION_CODE_EXPIRED(400, "만료된 프로모션 코드입니다."),
    PROMOTION_CODE_NOT_AVAILABLE(400, "사용할 수 없는 프로모션 코드입니다."),
    PROMOTION_CODE_PRICE_NOT_MATCH(400, "프로모션 코드의 금액을 확인해주세요.");

    private final int statusCode;
    private final String message;
}

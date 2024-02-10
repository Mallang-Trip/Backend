package mallang_trip.backend.domain.dto.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccessTokenRequest {

    private String grantType;
    private String customerKey;
    private String code;
    private String refreshToken;
}

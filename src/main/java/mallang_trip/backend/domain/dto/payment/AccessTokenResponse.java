package mallang_trip.backend.domain.dto.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccessTokenResponse {

    private String accessToken;
    private String tokenType;
    private String refreshToken;
    private Integer expiresIn;
}

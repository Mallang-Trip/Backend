package mallang_trip.backend.domain.payple.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SettlementAuthResponse {
    private String result;
    private String message;
    private String code;
    private String access_token;
    private String token_type;
    private String expires_in;
}

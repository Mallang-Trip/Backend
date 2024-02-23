package mallang_trip.backend.domain.identification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortOneAccessTokenResponse {

    private Integer code;
    private String message;
    private AuthAnnotation response;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public class AuthAnnotation {
        private String access_token;
        private String now;
        private String expired_at;
    }
}

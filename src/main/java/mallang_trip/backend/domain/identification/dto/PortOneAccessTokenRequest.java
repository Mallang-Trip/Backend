package mallang_trip.backend.domain.identification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PortOneAccessTokenRequest {

    private String imp_key;
    private String imp_secret;
}

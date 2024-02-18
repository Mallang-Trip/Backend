package mallang_trip.backend.domains.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginIdResponse {

    private String loginId;
}

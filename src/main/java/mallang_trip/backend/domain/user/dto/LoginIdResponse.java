package mallang_trip.backend.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginIdResponse {

    private String loginId;
}

package mallang_trip.backend.domain.dto.User;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginIdResponse {

    private String loginId;
}

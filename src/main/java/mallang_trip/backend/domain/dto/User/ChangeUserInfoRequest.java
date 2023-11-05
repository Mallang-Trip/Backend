package mallang_trip.backend.domain.dto.User;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChangeUserInfoRequest {

    private String before;
    private String after;
}

package mallang_trip.backend.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChangeProfileRequest {

    private String email;
    private String nickname;
    private String introduction;
    private String profileImg;
}

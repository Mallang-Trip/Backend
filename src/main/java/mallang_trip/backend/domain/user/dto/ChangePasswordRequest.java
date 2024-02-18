package mallang_trip.backend.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChangePasswordRequest {

    private String before;
    private String after;
}

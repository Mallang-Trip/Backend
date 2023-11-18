package mallang_trip.backend.domain.dto.User;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResetPasswordRequest {

    private String code;
    private String phoneNumber;
    private String newPassword;
}

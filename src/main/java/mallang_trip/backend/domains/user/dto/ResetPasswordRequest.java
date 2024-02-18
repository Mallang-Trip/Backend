package mallang_trip.backend.domains.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResetPasswordRequest {

    private String code;
    private String phoneNumber;
    private String newPassword;
}

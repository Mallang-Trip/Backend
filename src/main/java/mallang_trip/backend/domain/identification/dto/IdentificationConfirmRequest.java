package mallang_trip.backend.domain.identification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdentificationConfirmRequest {

    private String otp;
}

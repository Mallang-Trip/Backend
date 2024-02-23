package mallang_trip.backend.domain.identification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentificationResponse {

    private Integer code;
    private String message;
    private CertificationOTPAnnotation response;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public class CertificationOTPAnnotation {
        private String imp_uid;
    }
}

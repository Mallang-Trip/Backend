package mallang_trip.backend.domain.identification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentificationResultResponse {

    private Integer code;
    private String message;
    private CertificationAnnotation response;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public class CertificationAnnotation {
        private String imp_uid;
        private String name;
        private String gender;
        private String birthday;
        private Boolean foreigner;
        private String phone;
        private String carrier;
        private Boolean certified;
    }
}

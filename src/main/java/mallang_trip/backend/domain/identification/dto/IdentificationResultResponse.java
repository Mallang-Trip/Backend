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
    private String birthday;
    private String carrier;
    private Boolean certified;
    private String gender;
    private String name;
    private String phone;
    private String imp_uid;
}

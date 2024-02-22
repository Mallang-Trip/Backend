package mallang_trip.backend.domain.identification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdentificationRequest {

    private String name;
    private String phone;
    private String birth;
    private String gender_digit;
    private String carrier;
    private Boolean is_mvno;
    private String company;
}

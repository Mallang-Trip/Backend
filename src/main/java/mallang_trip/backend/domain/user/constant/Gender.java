package mallang_trip.backend.domain.user.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Gender {

    MALE,
    FEMALE,
    ;

    @JsonCreator
    public static Gender from(String str){
        return Gender.valueOf(str.toUpperCase());
    }
}

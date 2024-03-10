package mallang_trip.backend.domain.user.constant;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Gender {

    MALE,
    FEMALE,
    ;

    @JsonCreator
    public static Gender from(String str){
        return Gender.valueOf(str.toUpperCase());
    }
}

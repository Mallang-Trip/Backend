package mallang_trip.backend.domain.user.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Country {

    LOCAL,
    FOREGINER,
    ;

    @JsonCreator
    public static Country from(String str){
        return Country.valueOf(str.toUpperCase());
    }
}

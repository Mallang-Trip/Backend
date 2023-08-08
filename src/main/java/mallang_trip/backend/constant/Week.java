package mallang_trip.backend.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Week {

    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY,
    ;

    @JsonCreator
    public static Week from(String str){
        return Week.valueOf(str.toUpperCase());
    }
}

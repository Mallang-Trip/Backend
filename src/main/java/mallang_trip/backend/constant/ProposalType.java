package mallang_trip.backend.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProposalType {

    JOIN,
    COURSE_CHANGE,
    JOIN_WITH_COURSE_CHANGE,
    ;

    @JsonCreator
    public static ProposalType from(String str){
        return ProposalType.valueOf(str.toUpperCase());
    }
}

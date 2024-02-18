package mallang_trip.backend.domain.party.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProposalType {

    COURSE_CHANGE,
    JOIN_WITH_COURSE_CHANGE,
    ;

    @JsonCreator
    public static ProposalType from(String str){
        return ProposalType.valueOf(str.toUpperCase());
    }
}

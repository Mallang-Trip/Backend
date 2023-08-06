package mallang_trip.backend.domain.dto.Party;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JoinPartyRequest {

    private Long partyId;
    private Long newCourseId;
    private int headcount;
}

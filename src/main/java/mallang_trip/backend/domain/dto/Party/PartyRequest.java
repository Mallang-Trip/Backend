package mallang_trip.backend.domain.dto.Party;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartyRequest {

    private int headcount;
    private String startDate;
    private String endDate;
    private Long driverId;
    private Long courseId;
    private int myPrice;
}

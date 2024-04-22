package mallang_trip.backend.domain.admin.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.party.constant.PartyStatus;

@Getter
@Builder
public class PartyPaymentResponse {

	private Long partyId;
	private String partyName;
	private LocalDate startDate;
	private LocalDate endDate;
	private Long driverId;
	private String driverName;
	private Integer capacity;
	private Integer headcount;
	private PartyStatus status;
	List<PartyMemberPaymentResponse> partyMembers;
}

package mallang_trip.backend.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.reservation.constant.ReservationStatus;

@Getter
@Builder
public class PartyMemberPaymentResponse {

	private Long userId;
	private String nickname;
	private ReservationStatus status;
}

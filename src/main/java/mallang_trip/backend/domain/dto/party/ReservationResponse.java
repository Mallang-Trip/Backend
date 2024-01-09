package mallang_trip.backend.domain.dto.party;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.constant.ReservationStatus;
import mallang_trip.backend.domain.entity.reservation.Reservation;

@Builder
@Getter
public class ReservationResponse {

	private Long reservationId;
	private ReservationStatus status;
	private Integer paymentAmount;
	private Integer refundAmount;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static ReservationResponse of(Reservation reservation){
		if(reservation == null) return null;
		return ReservationResponse.builder()
			.reservationId(reservation.getId())
			.status(reservation.getStatus())
			.paymentAmount(reservation.getPaymentAmount())
			.refundAmount(reservation.getRefundAmount())
			.createdAt(reservation.getCreatedAt())
			.updatedAt(reservation.getUpdatedAt())
			.build();
	}
}

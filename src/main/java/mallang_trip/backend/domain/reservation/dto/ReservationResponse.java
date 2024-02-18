package mallang_trip.backend.domain.reservation.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.reservation.constant.ReservationStatus;
import mallang_trip.backend.domain.reservation.entity.Reservation;

@Builder
@Getter
public class ReservationResponse {

	private String reservationId;
	private ReservationStatus status;
	private Integer paymentAmount;
	private Integer refundAmount;
	private String receiptUrl;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static ReservationResponse of(Reservation reservation){
		if(reservation == null) return null;
		return ReservationResponse.builder()
			.reservationId(reservation.getId())
			.status(reservation.getStatus())
			.paymentAmount(reservation.getPaymentAmount())
			.refundAmount(reservation.getRefundAmount())
			.receiptUrl(reservation.getReceiptUrl())
			.createdAt(reservation.getCreatedAt())
			.updatedAt(reservation.getUpdatedAt())
			.build();
	}
}

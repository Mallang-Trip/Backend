package mallang_trip.backend.domain.reservation.constant;

public enum ReservationStatus {

	PAYMENT_FAILED, // 결제 실패
	PAYMENT_COMPLETE, // 결제 완료
	REFUND_COMPLETE, // 환불 완료
	REFUND_FAILED, // 환불 실패
	;
}

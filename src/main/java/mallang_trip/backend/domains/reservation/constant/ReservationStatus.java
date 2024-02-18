package mallang_trip.backend.domains.reservation.constant;

public enum ReservationStatus {

	PAYMENT_REQUIRED, // 결제 필요
	PAYMENT_COMPLETE, // 결제 완료
	REFUND_COMPLETE, // 환불 완료
	REFUND_FAILED, // 환불 실패
	;
}

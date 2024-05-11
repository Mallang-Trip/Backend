package mallang_trip.backend.domain.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.reservation.constant.ReservationStatus;
import mallang_trip.backend.domain.reservation.entity.Reservation;

@Getter
@Builder
public class PaymentResponse {

    private Long partyId;
    private String partyName;
    private LocalDate partyStartDate;
    private ReservationStatus status;
    private LocalDateTime updatedAt;
    private Integer paymentAmount;
    private Integer refundAmount;
    private String receiptUrl;
    private String cancelReceiptUrl;

    public static PaymentResponse of(Reservation reservation, Party party){
        return PaymentResponse.builder()
            .partyId(party.getId())
            .partyName(party.getCourse().getName())
            .partyStartDate(party.getStartDate())
            .status(reservation.getStatus())
            .updatedAt(reservation.getUpdatedAt())
            .paymentAmount(reservation.getPaymentAmount())
            .refundAmount(reservation.getRefundAmount())
            .receiptUrl(reservation.getReceiptUrl())
            .cancelReceiptUrl(reservation.getCancelReceiptUrl())
            .build();
    }
}

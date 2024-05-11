package mallang_trip.backend.domain.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private LocalDateTime paymentTime;
    private LocalDateTime refundTime;
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
            .paymentTime(parseDateTime(reservation.getPayTime()))
            .refundTime(parseDateTime(reservation.getCancelTime()))
            .paymentAmount(reservation.getPaymentAmount())
            .refundAmount(reservation.getRefundAmount())
            .receiptUrl(reservation.getReceiptUrl())
            .cancelReceiptUrl(reservation.getCancelReceiptUrl())
            .build();
    }

    public static LocalDateTime parseDateTime(String dateString) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            return LocalDateTime.parse(dateString, formatter);
        } catch (Exception e) {
            return null;
        }
    }
}

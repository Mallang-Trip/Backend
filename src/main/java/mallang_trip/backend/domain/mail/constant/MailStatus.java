package mallang_trip.backend.domain.mail.constant;

public enum MailStatus {

    SEALED, // 예약 완료
    CANCELLED, // 예약 취소
    MODIFIED, // 코스 변경
    MODIFIED_JOIN,
     // 코스 변경으로 인한 참가
}

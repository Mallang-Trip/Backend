package mallang_trip.backend.constant;

public enum PartyStatus {

    RECRUITING, // 모집중
    WAITING_DRIVER_APPROVAL, // 드라이버 승낙 대기중
    DRIVER_REFUSED, // 드라이버 거절
    WAITING_JOIN_APPROVAL, // 가입 신청 승낙 대기중
    WAITING_COURSE_CHANGE_APPROVAL, // 코스 변경 승낙 대기중
    RECRUITING_COMPLETED, // 모집 완료
    SEALED, // 파티 확정
    CANCELED, // 취소
    FINISHED, // 종료
    ;
}

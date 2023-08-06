package mallang_trip.backend.constant;

public enum PartyStatus {

    RECRUITING, // 모집중
    WAITING_DRIVER_APPROVAL, // 드라이버 승낙 대기중
    APPROVAL_WAITING, // 드라이버, 기존 멤버 승낙 대기중
    RECRUIT_COMPLETED, // 모집 완료
    MONOPOLIZED, // 독점
    CANCELED, // 취소
    FINISHED, // 종료
    ;
}

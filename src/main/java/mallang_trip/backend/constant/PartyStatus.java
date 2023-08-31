package mallang_trip.backend.constant;

public enum PartyStatus {

    RECRUITING, // 모집중
    WAITING_DRIVER_APPROVAL, // 드라이버 승낙 대기중
    DRIVER_REFUSED, // 드라이버 거절
    JOIN_APPROVAL_WAITING, // 가입 신청 승낙 대기중
    COURSE_CHANGE_APPROVAL_WAITING, // 코스 변경 승낙 대기중
    RECRUIT_COMPLETED, // 모집 완료
    MONOPOLIZED, // 독점
    CANCELED, // 취소
    FINISHED, // 종료
    ;
}

package mallang_trip.backend.domain.party.constant;

public enum PartyStatus {

    RECRUITING, // 모집중
    WAITING_DRIVER_APPROVAL, // 드라이버 승낙 대기중
    WAITING_JOIN_APPROVAL, // 가입 신청 승낙 대기중
    WAITING_COURSE_CHANGE_APPROVAL, // 코스 변경 승낙 대기중
    SEALED, // 파티 확정
    CANCELED_BY_DRIVER_REFUSED, // 파티 생성 신청 거절
    CANCELED_BY_PROPOSER, // 파티 생성 신청 취소
    CANCELED_BY_EXPIRATION, // 모집 기간 만료로 인한 취소
    CANCELED_BY_ALL_QUIT, // 파티원 전원 탈퇴로 인한 취소
    CANCELED_BY_DRIVER_QUIT, // 드라이버 탈퇴로 인한 취소
    CANCELED_BY_USER_QUIT, // 내가 탈퇴한 파티
    DAY_OF_TRAVEL, // 여행 당일
    FINISHED, // 종료
    ;
}

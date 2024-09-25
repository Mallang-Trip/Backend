package mallang_trip.backend.domain.party.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mallang_trip.backend.global.io.ResponseStatus;

@Getter
@AllArgsConstructor
public enum PartyExceptionStatus implements ResponseStatus {
	CANNOT_FOUND_PARTY(404, "파티를 찾을 수 없습니다."),
	PARTY_CONFLICTED(409, "이미 예약된 파티가 있습니다."),
	NOT_PARTY_MEMBER(403, "파티원이 아닙니다."),
	ALREADY_PARTY_MEMBER(409, "이미 가입되어있는 파티입니다."),
	PARTY_NOT_RECRUITING(403, "모집이 종료되었거나, 다른 신청자가 존재합니다."),
	EXCEED_PARTY_CAPACITY(403, "모집 인원 초과입니다."),
	CANNOT_CHANGE_COURSE(403, "코스 변경이 불가능한 상태입니다."),
	CANNOT_CHANGE_COURSE_PROMOTION_CODE(403, "프로모션 코드 제한으로 인해 코스 변경이 불가능합니다."),
	EXPIRED_PROPOSAL(403, "종료된 제안입니다."),
	ONGOING_PARTY_EXISTS(403, "진행중인 여행이 있습니다."),
	;

	private final int statusCode;
	private final String message;
}

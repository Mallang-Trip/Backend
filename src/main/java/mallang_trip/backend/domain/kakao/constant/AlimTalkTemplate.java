package mallang_trip.backend.domain.kakao.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AlimTalkTemplate {

	DRIVER_RESERVATION_CONFIRM("DriverReservationConfirm",
		"[예약 확정 안내]\n"
			+ "예약이 확정되었습니다.\n"
			+ "\n"
			+ "- 파티명: #{party_name}\n"
			+ "- 여행일시: #{date}"),

	DRIVER_RESERVATION_CANCELED("DriverReservationCanceled",
		"[예약 취소 안내]\n"
		+ "예약이 취소되었습니다.\n"
		+ "\n"
		+ "- 파티명: #{party_name}\n"
		+ "- 여행일시: #{date}"),

	DRIVER_NEW_PARTY("DriverNewParty",
		"[여행 신청 안내]\n"
			+ "새로운 여행 신청이 존재합니다. 24시간 내에 승인/거절을 선택해주세요.\n"
			+ "\n"
			+ "- 파티명: #{party_name}\n"
			+ "- 여행일시: #{date}"),

	DRIVER_COURSE_CHANGE("DriverCourseChange",
		"[변경 제안 안내]\n"
			+ "새로운 코스 변경 제안이 존재합니다. 24시간 내에 수락/거절을 선택해주세요.\n"
			+ "\n"
			+ "- 파티명: #{party_name}\n"
			+ "- 여행일시: #{date}"),

	DRIVER_TRAVELER_LIST("DriverTravelerList",
		"[예약자 명단 안내]\n"
			+ "#{driver_name} 드라이버님\n"
			+ "내일 예약된 여행 정보를 안내드립니다. 예약자 명단을 확인하고 대면할 때 승객의 신분증을 확인하여 신원을 철저히 검증해주세요.\n"
			+ "\n"
			+ "- 파티명: #{party_name}\n"
			+ "- 여행일시: #{date}\n"
			+ "- 예약자 명단:\n"
			+ "  #{member_info}"),
	;

	private final String templateCode;
	private final String content;
}

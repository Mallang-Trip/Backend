package mallang_trip.backend.domain.kakao.service;

import static mallang_trip.backend.domain.kakao.constant.AlimTalkTemplate.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.course.service.CourseService;
import mallang_trip.backend.domain.kakao.constant.AlimTalkTemplate;
import mallang_trip.backend.domain.kakao.dto.AlimTalkRequest;
import mallang_trip.backend.domain.party.entity.Party;
import mallang_trip.backend.domain.party.service.PartyMemberService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlimTalkService {
	private final CourseService courseService;
	private final AlimTalkRequestService alimTalkRequestService;
	private final PartyMemberService partyMemberService;

	/**
	 * 알림톡 발송
	 *
	 * @param party 해당하는 Party 객체
	 * @param template
	 * 1. DRIVER_RESERVATION_CONFIRM: 드라이버 예약 확정 알림톡,
	 * 2. DRIVER_RESERVATION_CANCEL: 드라이버 예약 취소 알림톡,
	 * 3. DRIVER_NEW_PARTY: 드라이버 새 여행 신청 알림톡,
	 * 4. DRIVER_RECRUITING_END: 드라이버 파티 모집 종료 알림톡,
	 * 5. DRIVER_COURSE_CHANGE: 드라이버 코스 변경 제안 알림톡,
	 */
	public void sendDriverAlimTalk(AlimTalkTemplate template, Party party){
		Map<String, String> templateValues = new HashMap<>();
		templateValues.put("party_name", party.getCourse().getName());
		templateValues.put("date", getReservationDateTime(party));

		String content = applyTemplate(template.getContent(), templateValues);
		String to = party.getDriver().getUser().getPhoneNumber();

		alimTalkRequestService.send(AlimTalkRequest.of(template, to, content));
	}

	/**
	 * 드라이버 예약자 명단 안내 알림톡 발송
	 *
	 * @param party 해당하는 Party 객체
	 */
	public void sendTravelerListAlimTalk(Party party){
		Map<String, String> templateValues = new HashMap<>();
		templateValues.put("party_name", party.getCourse().getName());
		templateValues.put("date", getReservationDateTime(party));
		templateValues.put("member_info", getPartyMembersInfo(party));

		String content = applyTemplate(DRIVER_TRAVELER_LIST.getContent(), templateValues);
		String to = party.getDriver().getUser().getPhoneNumber();

		alimTalkRequestService.send(AlimTalkRequest.of(DRIVER_TRAVELER_LIST, to, content));
	}


	/**
	 * AlimTalkTemplate content의 변수를 실제 값으로 치환
	 *
	 * @param template 사용할 AlimTalkTemplate의 content
	 * @param values 변수 치환 정보
	 * @return 치환된 content
	 */
	private String applyTemplate(String template, Map<String, String> values) {
		for (Map.Entry<String, String> entry : values.entrySet()) {
			template = template.replace("#{" + entry.getKey() + "}", entry.getValue());
		}
		return template;
	}

	/**
	 * 예약일시 조회
	 *
	 * @param party 조회할 Party 객체
	 * @return MM월 dd일 (E) hh:mm ~ hh:mm 형식의 String
	 */
	private String getReservationDateTime(Party party){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM월 dd일 (E)", Locale.KOREAN);

		LocalDate startDate = party.getStartDate();
		String time = courseService.getStartAndEndTime(party.getCourse());

		return startDate.format(formatter) + " " + time;
	}

	/**
	 * 예약자 명단 정보 조회
	 */
	private String getPartyMembersInfo(Party party){
		StringBuilder stringBuilder = new StringBuilder();
		partyMemberService.getMembers(party).stream()
			.forEach(member -> {
				String phoneNumber = member.getUser().getPhoneNumber();
				stringBuilder.append(member.getUser().getName())
					.append(" (")
					.append(member.getHeadcount())
					.append("인): ")
					.append(phoneNumber.substring(0, 3) + "-" + phoneNumber.substring(3, 7) + "-" + phoneNumber.substring(7))
					.append("\n  ");
			});

		return stringBuilder.toString();
	}
}

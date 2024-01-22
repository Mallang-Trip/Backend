package mallang_trip.backend.service.party;

import static mallang_trip.backend.constant.NotificationType.PARTY;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.entity.party.Party;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyNotificationService {

	private final NotificationService notificationService;

	private String getPartyName(Party party) {
		return party.getCourse().getName();
	}

	// 1. 새로운 파티 생성 신청이 들어왔을 경우
	public void newParty(User driver, Party party) {
		StringBuilder content = new StringBuilder();
		content.append("새로운 파티 신청이 존재합니다. 24시간 내에 수락/거절을 선택해주세요.");
		notificationService.create(driver, content.toString(), PARTY, party.getId());
	}

	// 2. 내 파티 생성 신청을 드라이버가 수락했을 경우
	public void creationAccepted(User user, Party party) {
		StringBuilder content = new StringBuilder();
		content.append("[")
			.append(getPartyName(party))
			.append("] 신청이 수락되었습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	// 3. 내 파티 생성 신청을 드라이버가 거절했을 경우
	public void creationRefused(User user, Party party) {
		StringBuilder content = new StringBuilder();
		content.append("[")
			.append(getPartyName(party))
			.append("] 신청이 거절되었습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	// 4. 파티원 모집이 완료되었을 경우
	public void partyFulled(User user, Party party) {
		StringBuilder content = new StringBuilder();
		content.append("여행자 모집 완료로 [")
			.append(getPartyName(party))
			.append("] 예약이 확정되었습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	// 5. 파티원 전원이 레디를 완료했을 경우
	public void allReady(User user, Party party) {
		StringBuilder content = new StringBuilder();
		content.append("여행자 전원 말랑레디로 [")
			.append(getPartyName(party))
			.append("] 예약이 확정되었습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	// 6. 새로운 파티원이 가입했을 경우
	public void newMember(User user, User joiner, Party party) {
		StringBuilder content = new StringBuilder();
		content.append(joiner.getNickname())
			.append("님이 [")
			.append(getPartyName(party))
			.append("]에 가입했습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	// 7. 코스변경과 함께 가입을 신청했을 경우
	public void newJoinRequest(User user, Party party) {
		StringBuilder content = new StringBuilder();
		content.append("신규 여행자가 [")
			.append(party.getCourse().getName())
			.append("] 참여 승인을 기다리고 있습니다. 24시간 내에 수락/거절을 선택해주세요.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	// 8. 파티 가입이 수락되었을 경우
	public void joinAccepted(User user, Party party) {
		StringBuilder content = new StringBuilder();
		content.append("[")
			.append(getPartyName(party))
			.append("] 가입이 수락되었습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	// 9. 파티 가입이 거절되었을 경우
	public void joinRefused(User user, Party party) {
		StringBuilder content = new StringBuilder();
		content.append("[")
			.append(getPartyName(party))
			.append("] 가입이 거절되었습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	// 10. 파티원이 코스 변경을 제안했을 경우
	public void newCourseChange(User user, Party party) {
		StringBuilder content = new StringBuilder();
		content.append("[")
			.append(getPartyName(party))
			.append("] 새로운 코스 변경 제안이 존재합니다. 24시간 내에 수락/거절을 선택해주세요.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	// 11. 코스가 변경되었을 경우
	public void courseChangeAccepted(User user, Party party) {
		StringBuilder content = new StringBuilder();
		content.append("[")
			.append(getPartyName(party))
			.append("] 코스가 변경되었습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	// 12. 코스 변경 신청이 거절되었을 경우
	public void courseChangeRefused(User user, Party party) {
		StringBuilder content = new StringBuilder();
		content.append("[")
			.append(getPartyName(party))
			.append("] 코스 변경 신청이 거절되었습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	// 13. 드라이버가 파티를 탈퇴할 경우
	public void cancelByDriverQuit(User user, Party party) {
		StringBuilder content = new StringBuilder();
		content.append("드라이버의 취소로 [")
			.append(getPartyName(party))
			.append("]이/가 취소되었습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	// 14. 파티 멤버가 파티를 탈퇴할 경우
	public void memberQuit(User user, User runner, Party party) {
		StringBuilder content = new StringBuilder();
		content.append(runner.getNickname());
		content.append("님이 [")
			.append(getPartyName(party))
			.append("]을/를 나갔습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	// 15. 모집기간 만료로 파티가 취소되었을 경우
	public void cancelByExpiration(User user, Party party) {
		StringBuilder content = new StringBuilder();
		content.append("신청자가 없어 [")
			.append(getPartyName(party))
			.append("]이/가 취소되었습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	// 16. 파티원 전원 탈퇴로 파티가 취소되었을 경우
	public void cancelByAllQuit(User user, Party party) {
		StringBuilder content = new StringBuilder();
		content.append("남은 여행자가 없어 [")
			.append(getPartyName(party))
			.append("]이/가 취소되었습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	// 17. 파티원이 예약을 취소했을 경우
	public void memberCancelReservation(User user, User runner, Party party) {
		StringBuilder content = new StringBuilder();
		content.append(runner.getNickname());
		content.append("님이 [")
			.append(getPartyName(party))
			.append("] 예약을 취소했습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	// 18. 여행 전날/당일

	// 19. 여행 종료
}

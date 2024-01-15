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

	public void newParty(User driver, Party party){
		StringBuilder content = new StringBuilder();
		content.append("새로운 파티 신청이 존재합니다. 24시간 내에 수락/거절을 선택해주세요.");
		notificationService.create(driver, content.toString(), PARTY, party.getId());
	}

	public void creationAccepted(User user, Party party){
		StringBuilder content = new StringBuilder();
		content.append("파티 신청이 수락되었습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	public void creationRefused(User user, Party party){
		StringBuilder content = new StringBuilder();
		content.append("파티 신청이 거절되었습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	public void partyFulled(User user, Party party){
		StringBuilder content = new StringBuilder();
		content.append("파티원 모집 완료로 예약이 확정되었습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	public void allReady(User user, Party party){
		StringBuilder content = new StringBuilder();
		content.append("파티원 전원 말랑레디로 예약이 확정되었습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	public void newMember(User user, User joiner ,Party party){
		StringBuilder content = new StringBuilder();
		content.append(joiner.getNickname());
		content.append("님이 [");
		content.append(party.getCourse().getName());
		content.append("]파티에 가입했습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	public void newJoinRequest(User user, Party party){
		StringBuilder content = new StringBuilder();
		content.append("신규 여행자가 [");
		content.append(party.getCourse().getName());
		content.append("]파티 참여 승인을 기다리고 있습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	public void newCourseChange(User user, Party party){
		StringBuilder content = new StringBuilder();
		content.append("새로운 코스 변경 제안이 존재합니다. 24시간 내에 수락/거절을 선택해주세요.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	public void courseChangeAccepted(User user, Party party){
		StringBuilder content = new StringBuilder();
		content.append("파티 코스가 변경되었습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	public void courseChangeRefused(User user, Party party){
		StringBuilder content = new StringBuilder();
		content.append("코스 변경 신청이 거절되었습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	public void joinAccepted(User user, Party party){
		StringBuilder content = new StringBuilder();
		content.append("파티 가입이 수락되었습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	public void joinRefused(User user, Party party){
		StringBuilder content = new StringBuilder();
		content.append("파티 가입이 거절되었습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	public void cancelByDriverQuit(User user, Party party){
		StringBuilder content = new StringBuilder();
		content.append("드라이버의 취소로 파티가 취소되었습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}

	public void memberQuit(User runner, User user, Party party){
		StringBuilder content = new StringBuilder();
		content.append(runner.getNickname());
		content.append("님이 파티를 나갔습니다.");
		notificationService.create(user, content.toString(), PARTY, party.getId());
	}
}

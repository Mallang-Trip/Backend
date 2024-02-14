package mallang_trip.backend.service.reservation;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.constant.NotificationType;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationNotificationService {

	private final NotificationService notificationService;

	public void penaltyPaymentRequired(User user, Integer penaltyAmount){
		String content = new StringBuilder()
			.append("예약금 미지급 상태에서 예약 취소로 인하여 추가 납입해야 하는 위약금 ")
			.append(penaltyAmount)
			.append("원이 발생하였습니다. 위약금 지급을 위해 말랑트립 1:1 채팅상담에 연락해주세요.")
			.toString();
		notificationService.create(user, content, NotificationType.NONE, null);
	}
}

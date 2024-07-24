package mallang_trip.backend.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.mail.service.MailService;
import mallang_trip.backend.domain.notification.constant.NotificationType;
import mallang_trip.backend.domain.notification.entity.Firebase;
import mallang_trip.backend.domain.notification.repository.FirebaseRepository;
import mallang_trip.backend.domain.notification.service.FirebaseService;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationNotificationService {

	private final NotificationService notificationService;

	private final MailService mailService;
	private final FirebaseService firebaseService;
	private final FirebaseRepository firebaseRepository;

	public void penaltyPaymentRequired(User user, Integer penaltyAmount){
		String content = new StringBuilder()
			.append("예약금 미지급 상태에서 예약 취소로 인하여 추가 납입해야 하는 위약금 ")
			.append(penaltyAmount)
			.append("원이 발생하였습니다. 위약금 지급을 위해 말랑트립 1:1 채팅상담에 연락해주세요.")
			.toString();
		notificationService.create(user, content, NotificationType.NONE, null);
		mailService.sendEmailNotification(user.getEmail(),user.getName(),content,"위약금이 발생하였습니다.");

		Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(user);
		firebase.ifPresent(f -> firebaseService.sendPushMessage(f.getTokens(), "말랑트립", content));
	}
}

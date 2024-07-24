package mallang_trip.backend.domain.driver.service;

import static mallang_trip.backend.domain.notification.constant.NotificationType.DRIVER;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.driver.entity.DriverReview;
import mallang_trip.backend.domain.mail.service.MailService;
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
public class DriverNotificationService {

	private final NotificationService notificationService;

	private final MailService mailService;
	private final FirebaseService firebaseService;
	private final FirebaseRepository firebaseRepository;

	public void newReview(DriverReview review){
		User driver = review.getDriver().getUser();
		String content = new StringBuilder()
			.append(review.getUser().getNickname())
			.append("님이 ")
			.append(driver.getName())
			.append("님에게 리뷰를 작성하였습니다.")
			.toString();
		notificationService.create(driver, content, DRIVER, driver.getId());
		mailService.sendEmailNotification(driver.getEmail(),driver.getName(),content,"새 리뷰가 작성되었습니다.");

		Optional<Firebase> firebase = firebaseRepository.findByUserAndTokensNotNull(driver);
		firebase.ifPresent(value -> firebaseService.sendPushMessage(value.getTokens(), "말랑트립", content));
	}
}

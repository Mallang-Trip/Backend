package mallang_trip.backend.service.driver;

import static mallang_trip.backend.constant.NotificationType.DRIVER;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.entity.driver.DriverReview;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DriverNotificationService {

	private final NotificationService notificationService;

	public void newReview(DriverReview review){
		User driver = review.getDriver().getUser();
		String content = new StringBuilder()
			.append(review.getUser().getNickname())
			.append("님이 ")
			.append(driver.getName())
			.append("님에게 리뷰를 작성하였습니다.")
			.toString();
		notificationService.create(driver, content, DRIVER, driver.getId());
	}
}

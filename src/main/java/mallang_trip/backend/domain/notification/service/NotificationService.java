package mallang_trip.backend.domain.notification.service;

import static mallang_trip.backend.global.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.global.io.BaseResponseStatus.Not_Found;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.notification.constant.NotificationType;
import mallang_trip.backend.domain.user.service.CurrentUserService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.notification.dto.NotificationListResponse;
import mallang_trip.backend.domain.notification.dto.NotificationResponse;
import mallang_trip.backend.domain.notification.entity.Notification;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.notification.repository.NotificationRepository;
import mallang_trip.backend.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

	private final CurrentUserService currentUserService;
	private final NotificationRepository notificationRepository;

	/**
	 * 현재 유저 알림 조회
	 */
	public NotificationListResponse getNotifications() {
		User user = currentUserService.getCurrentUser();
		List<NotificationResponse> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user)
			.stream()
			.map(NotificationResponse::of)
			.collect(Collectors.toList());
		return NotificationListResponse.builder()
			.contents(notifications)
			.uncheckedCount(getUncheckedCount(user))
			.build();
	}

	/**
	 * 알림 생성 및 저장
	 */
	public void create(User user, String content, NotificationType type, Long targetId) {
		notificationRepository.save(Notification.builder()
			.user(user)
			.content(content)
			.type(type)
			.targetId(targetId)
			.build());
	}

	/**
	 * 알림 확인 처리
	 */
	public void check(Long notificationId) {
		Notification notification = notificationRepository.findById(notificationId)
			.orElseThrow(() -> new BaseException(Not_Found));
		if(!notification.getUser().equals(currentUserService.getCurrentUser())){
			throw new BaseException(Forbidden);
		}
		notification.setCheckTrue();
	}

	/**
	 * 알림 전체 확인 처리
	 */
	public void checkAll(){
		notificationRepository.findByUser(currentUserService.getCurrentUser()).stream()
			.forEach(notification -> {
				if(!notification.getChecked()) notification.setCheckTrue();
			});
	}

	/**
	 * 알림 삭제
	 */
	public void delete(Long notificationId){
		Notification notification = notificationRepository.findById(notificationId)
			.orElseThrow(() -> new BaseException(Not_Found));
		if(!notification.getUser().equals(currentUserService.getCurrentUser())){
			throw new BaseException(Forbidden);
		}
		notificationRepository.delete(notification);
	}

	/**
	 * 확인하지 않은 알림 수 조회
	 */
	private Integer getUncheckedCount(User user) {
		return notificationRepository.countByUserAndChecked(user, false);
	}
}

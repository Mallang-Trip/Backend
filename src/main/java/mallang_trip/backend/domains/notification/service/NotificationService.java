package mallang_trip.backend.domains.notification.service;

import static mallang_trip.backend.domains.global.io.BaseResponseStatus.Not_Found;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domains.notification.constant.NotificationType;
import mallang_trip.backend.domains.global.io.BaseException;
import mallang_trip.backend.domains.notification.dto.NotificationListResponse;
import mallang_trip.backend.domains.notification.dto.NotificationResponse;
import mallang_trip.backend.domains.notification.entity.Notification;
import mallang_trip.backend.domains.user.entity.User;
import mallang_trip.backend.domains.notification.repository.NotificationRepository;
import mallang_trip.backend.domains.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final UserService userService;
    private final NotificationRepository notificationRepository;

    /** 현재 유저 알림 조회 */
    public NotificationListResponse getNotifications(){
        User user = userService.getCurrentUser();
        List<NotificationResponse> notifications = notificationRepository.findByUser(user).stream()
            .map(NotificationResponse::of)
            .collect(Collectors.toList());
        return NotificationListResponse.builder()
            .contents(notifications)
            .uncheckedCount(getUncheckedCount(user))
            .build();
    }

    /** 알림 생성 및 저장 */
    public void create(User user, String content, NotificationType type, Long targetId) {
        notificationRepository.save(Notification.builder()
            .user(user)
            .content(content)
            .type(type)
            .targetId(targetId)
            .build());
    }

    /** 알림 확인 처리 */
    public void check(Long notificationId){
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new BaseException(Not_Found));
        notification.setChecked(true);
    }

    /** 확인하지 않은 알림 수 조회 */
    private Integer getUncheckedCount(User user){
        return notificationRepository.countByUserAndChecked(user, false);
    }
}

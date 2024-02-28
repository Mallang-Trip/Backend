package mallang_trip.backend.domain.notification.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.domain.notification.constant.NotificationType;
import mallang_trip.backend.domain.notification.entity.Notification;

@Getter
@Builder
public class NotificationResponse {

    private Long alarmId;
    private String content;
    private NotificationType type;
    private Long targetId;
    private Boolean checked;
    private LocalDateTime createdAt;

    public static NotificationResponse of(Notification notification){
        return NotificationResponse.builder()
            .alarmId(notification.getId())
            .content(notification.getContent())
            .type(notification.getType())
            .targetId(notification.getTargetId())
            .checked(notification.getChecked())
            .createdAt(notification.getCreatedAt())
            .build();
    }
}

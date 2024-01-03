package mallang_trip.backend.domain.dto.notification;

import lombok.Builder;
import lombok.Getter;
import mallang_trip.backend.constant.NotificationType;
import mallang_trip.backend.domain.entity.notification.Notification;

@Getter
@Builder
public class NotificationResponse {

    private Long alarmId;
    private String content;
    private NotificationType type;
    private Long targetId;
    private Boolean checked;

    public static NotificationResponse of(Notification notification){
        return NotificationResponse.builder()
            .alarmId(notification.getId())
            .content(notification.getContent())
            .type(notification.getType())
            .targetId(notification.getTargetId())
            .checked(notification.getChecked())
            .build();
    }
}

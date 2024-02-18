package mallang_trip.backend.domain.notification.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationListResponse {

    private List<NotificationResponse> contents;
    private Integer uncheckedCount;
}

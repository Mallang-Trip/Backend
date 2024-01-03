package mallang_trip.backend.domain.dto.notification;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationListResponse {

    private List<NotificationResponse> contents;
    private Integer uncheckedCount;
}

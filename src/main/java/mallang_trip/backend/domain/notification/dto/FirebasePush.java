package mallang_trip.backend.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FirebasePush {
    private boolean validateOnly;
    private Message message;

    public static class Message{
        private Notification notification;
        private String token;
    }

    public static class Notification{
        private String title;
        private String body;
    }

}

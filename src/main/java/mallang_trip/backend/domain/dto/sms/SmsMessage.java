package mallang_trip.backend.domain.dto.sms;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SmsMessage {

    private String to;
    private String content;
}

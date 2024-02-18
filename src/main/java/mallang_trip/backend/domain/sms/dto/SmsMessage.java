package mallang_trip.backend.domain.sms.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SmsMessage {

    private String to;
    private String content;
}

package mallang_trip.backend.domain.dto.sms;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SmsRequest {

    private String type;
    private String contentType;
    private String countryCode;
    private String from;
    private String content;
    private List<SmsMessage> messages;
}

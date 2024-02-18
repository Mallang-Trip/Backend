package mallang_trip.backend.domain.sms.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SmsResponse {

    private String requestId;
    private LocalDateTime requestTime;
    private String statusCode;
    private String statusName;
}

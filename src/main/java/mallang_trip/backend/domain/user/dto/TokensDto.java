package mallang_trip.backend.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokensDto {
    String accessToken;
    String refreshToken;
}

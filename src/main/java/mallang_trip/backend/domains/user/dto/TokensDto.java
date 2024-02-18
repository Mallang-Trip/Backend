package mallang_trip.backend.domains.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokensDto {
    String accessToken;
    String refreshToken;
}

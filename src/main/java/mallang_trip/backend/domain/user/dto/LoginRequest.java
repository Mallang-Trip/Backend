package mallang_trip.backend.domain.user.dto;

import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Builder
@Getter
public class LoginRequest {

    @NotBlank
    private String id;

    @NotBlank
    private String password;

    // 아이디-비밀번호 일치 검증 로직
    public UsernamePasswordAuthenticationToken toAuthentication() {
        return new UsernamePasswordAuthenticationToken(id, password);
    }
}

package mallang_trip.backend.domain.notification.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Builder
@Jacksonized
public class FirebaseRequest {

    @NotNull
    @ApiModelProperty(value = "Firebase Tokens", required = true)
    private List<String> firebaseTokens;
}

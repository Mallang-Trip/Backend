package mallang_trip.backend.domain.notification.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter
@Builder
public class FirebaseRequest {

    @NotBlank
    @ApiModelProperty(value = "Firebase Token", required = true)
    private String firebaseToken;
}

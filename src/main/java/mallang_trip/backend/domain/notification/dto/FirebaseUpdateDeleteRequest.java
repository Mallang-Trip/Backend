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
public class FirebaseUpdateDeleteRequest {

    @NotBlank
    @ApiModelProperty(value = "Firebase Token", required = true)
    private String firebaseToken;
}

package mallang_trip.backend.domain.notification.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import org.checkerframework.checker.units.qual.N;

import javax.validation.constraints.NotBlank;

@Getter
@Builder
public class FirebaseTest {

    @NotBlank
    @ApiModelProperty(value = "Firebase Token", required = true)
    private String firebaseToken;

    @NotBlank
    @ApiModelProperty(value = "Title", required = true)
    private String title;

    @NotBlank
    @ApiModelProperty(value = "Body", required = true)
    private String body;
}

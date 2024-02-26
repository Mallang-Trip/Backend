package mallang_trip.backend.healthCheck;

import mallang_trip.backend.global.io.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
public class HealthCheckController {

    @GetMapping("/check")
    public BaseResponse<String> healthCheck() {
        return new BaseResponse<>("OK");
    }
}

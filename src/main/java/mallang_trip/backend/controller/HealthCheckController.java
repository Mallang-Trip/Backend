package mallang_trip.backend.controller;

import mallang_trip.backend.controller.io.BaseResponse;
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

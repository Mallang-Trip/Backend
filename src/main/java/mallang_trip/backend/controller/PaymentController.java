package mallang_trip.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.net.URISyntaxException;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponse;
import mallang_trip.backend.service.payment.PaymentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Payment API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @ApiOperation(value = "약관동의 redirection")
    @GetMapping
    @PreAuthorize("permitAll()") // anyone
    public BaseResponse<String> save(@RequestParam String code, @RequestParam String customerKey)
        throws BaseException, URISyntaxException, JsonProcessingException {
        paymentService.save(code, customerKey);
        return new BaseResponse<>("성공");
    }
}

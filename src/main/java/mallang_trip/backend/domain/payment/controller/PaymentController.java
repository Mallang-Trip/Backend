package mallang_trip.backend.domain.payment.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.global.io.BaseException;
import mallang_trip.backend.domain.global.io.BaseResponse;
import mallang_trip.backend.domain.payment.dto.CardResponse;
import mallang_trip.backend.domain.payment.service.PaymentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Payment API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @ApiOperation(value = "카드 등록")
    @PostMapping
    @PreAuthorize("isAuthenticated()") // 로그인 사용자
    public BaseResponse<CardResponse> save(@RequestParam String authKey)
        throws BaseException {
        return new BaseResponse<>(paymentService.register(authKey));
    }

    @ApiOperation(value = "카드 삭제")
    @DeleteMapping
    @PreAuthorize("isAuthenticated()") // 로그인 사용자
    public BaseResponse<String> save() throws BaseException {
        paymentService.delete();
        return new BaseResponse<>("성공");
    }

    @ApiOperation(value = "등록된 카드 조회")
    @GetMapping
    @PreAuthorize("isAuthenticated()") // 로그인 사용자
    public BaseResponse<CardResponse> getCard() throws BaseException {
        return new BaseResponse<>(paymentService.getCard());
    }
}

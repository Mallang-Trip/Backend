package mallang_trip.backend.domain.reservation.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.reservation.dto.PaymentResponse;
import mallang_trip.backend.domain.reservation.service.ReservationService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Payment API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private final ReservationService reservationService;

    @ApiOperation(value = "내 결제/환불 내역 조회")
    @GetMapping("/my")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @ApiResponses({
        @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
        @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
        @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("isAuthenticated()") // 로그인 사용자
    public BaseResponse<List<PaymentResponse>> getMyPayments() throws BaseException {
        return new BaseResponse<>(reservationService.getMyPayments());
    }
}

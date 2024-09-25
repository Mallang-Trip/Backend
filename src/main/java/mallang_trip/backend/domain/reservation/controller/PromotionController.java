package mallang_trip.backend.domain.reservation.controller;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.reservation.dto.*;
import mallang_trip.backend.domain.reservation.service.PromotionService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "Promotion API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/promotion")

public class PromotionController {

    private final PromotionService promotionService;

    @ApiOperation(value = "(관리자)프로모션 코드 생성")
    @PostMapping("/create")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<String> createPromotionCode(@RequestBody PromotionCodeCreateRequest request)
    throws BaseException {
        promotionService.createPromotionCode(request);
        return new BaseResponse<>("성공");
    }

    @ApiOperation(value = "(관리자)프로모션 코드 전체 조회")
    @GetMapping("/list")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<List<PromotionCodeResponse>> getPromotionCodes() {
        return new BaseResponse<>(promotionService.getPromotionCodes());
    }

    @ApiOperation(value = "(관리자)프로모션 코드 수정")
    @PutMapping("/update/{id}")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<String> updatePromotionCode(@PathVariable Long id, @RequestBody PromotionCodeCreateRequest request)
    throws BaseException {
        promotionService.updatePromotionCode(id, request);
        return new BaseResponse<>("성공");
    }

    @ApiOperation(value = "(관리자)프로모션 코드 삭제")
    @DeleteMapping("/delete/{id}")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<String> deletePromotionCode(@PathVariable Long id)
    throws BaseException {
        promotionService.deletePromotionCode(id);
        return new BaseResponse<>("성공");
    }

    @ApiOperation(value = "프로모션 코드 사용 가능 여부 확인")
    @PostMapping("/check")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_USER')") // 사용자
    public BaseResponse<String> checkPromotionCode(@RequestBody PromotionCodeCheckRequest request)
    throws BaseException {
        return new BaseResponse<>(promotionService.checkPromotionCode(request));
    }

    @ApiOperation(value = "프로모션 코드 사용")
    @PostMapping("/use")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_USER')") // 사용자
    public BaseResponse<PromotionCodeUseIdResponse> usePromotionCode(@RequestBody PromotionCodeUseRequest request)
    throws BaseException {
        return new BaseResponse<>(promotionService.usePromotionCode(request));
    }

    @ApiOperation(value = "프로모션 코드 사용 취소")
    @PostMapping("/cancel/{id}")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_USER')") // 사용자
    public BaseResponse<String> cancelPromotionCode(@PathVariable Long id)
    throws BaseException {
        promotionService.cancelPromotionCode(id);
        return new BaseResponse<>("성공");
    }


}

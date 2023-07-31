package mallang_trip.backend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponse;
import mallang_trip.backend.domain.dto.TokensDto;
import mallang_trip.backend.domain.dto.User.AuthResponse;
import mallang_trip.backend.domain.dto.User.LoginRequest;
import mallang_trip.backend.domain.dto.User.SignupRequest;
import mallang_trip.backend.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Api(tags = "User API")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @ApiOperation(value = "회원가입")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "img", value = "프로필 이미지 (생략가능)"),
        @ApiImplicitParam(name = "data", value = "Schemas의 signupRequest 참고 (application/json)")
    })
    public BaseResponse<String> signup(
        @RequestPart(value = "img", required = false) MultipartFile file,
        @RequestPart("data") @Valid SignupRequest request
    ) throws BaseException {
        userService.signup(file, request);
        return new BaseResponse<>("성공");
    }

    @GetMapping("/login")
    @ApiOperation(value = "로그인", notes = "로그인 성공 시 access token, refresh token 발급")
    public BaseResponse<TokensDto> login(@RequestBody @Valid LoginRequest request) throws BaseException {
        return new BaseResponse<>(userService.login(request));
    }

    @GetMapping("/auth")
    @ApiOperation(value = "Auth", notes = "access token 으로 로그인 정보 조회")
    public BaseResponse<AuthResponse> auth() throws BaseException {
        return new BaseResponse<>(userService.auth());
    }

    @GetMapping("/refresh-token")
    @ApiOperation(value = "Resfresh Token", notes = "access token 만료 시, refresh token 으로 재발급 받기")
    public BaseResponse<TokensDto> refreshToken() throws BaseException {
        return new BaseResponse<>(userService.refreshToken());
    }

    @GetMapping("/check-duplication")
    @ApiOperation(value = "중복 확인", notes = "회원가입 시 중복 확인")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "type", value = "[phoneNumber/loginId/email/nickname] 중 하나"),
        @ApiImplicitParam(name = "value", value = "중복 확인할 값")
    })
    @ApiResponses({
        @ApiResponse(code = 200, message = "사용가능"),
        @ApiResponse(code = 409, message = "중복(사용불가)")
    })
    public BaseResponse<String> checkDuplication(@RequestParam String type,
        @RequestParam String value) throws BaseException {
        userService.checkDuplication(type, value);
        return new BaseResponse<>("사용 가능");
    }

}

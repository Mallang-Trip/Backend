package mallang_trip.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public BaseResponse<String> signup(
        @RequestPart(value = "img", required = false) MultipartFile file,
        @RequestParam("data") String data
    ) throws BaseException {
        userService.signup(file, SignupRequest.jsonToSignupRequest(data));
        return new BaseResponse<>("성공");
    }

    @GetMapping("/login")
    public BaseResponse<TokensDto> login(@RequestBody LoginRequest request) throws BaseException {
        return new BaseResponse<>(userService.login(request));
    }

    @GetMapping("/auth")
    public BaseResponse<AuthResponse> auth() throws BaseException {
        return new BaseResponse<>(userService.auth());
    }

    @GetMapping("/refresh-token")
    public BaseResponse<TokensDto> refreshToken() throws BaseException {
        return new BaseResponse<>(userService.refreshToken());
    }

    @GetMapping("/check-duplication")
    public BaseResponse<String> checkDuplication(@RequestParam String type,
        @RequestParam String value) throws BaseException {
        userService.checkDuplication(type, value);
        return new BaseResponse<>("사용 가능");
    }

}

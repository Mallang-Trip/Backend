package mallang_trip.backend.domains.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domains.global.io.BaseException;
import mallang_trip.backend.domains.global.io.BaseResponse;
import mallang_trip.backend.domains.user.dto.TokensDto;
import mallang_trip.backend.domains.user.dto.AuthResponse;
import mallang_trip.backend.domains.user.dto.ChangePasswordRequest;
import mallang_trip.backend.domains.user.dto.ChangeProfileRequest;
import mallang_trip.backend.domains.user.dto.LoginIdResponse;
import mallang_trip.backend.domains.user.dto.LoginRequest;
import mallang_trip.backend.domains.user.dto.ResetPasswordRequest;
import mallang_trip.backend.domains.user.dto.SignupRequest;
import mallang_trip.backend.domains.user.dto.UserBriefResponse;
import mallang_trip.backend.domains.user.service.UserSearchService;
import mallang_trip.backend.domains.user.service.UserService;
import mallang_trip.backend.domains.user.service.UserWithdrawalService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "User API")
@RestController
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final UserSearchService userSearchService;
	private final UserWithdrawalService userWithdrawalService;

	@PostMapping("/signup")
	@ApiOperation(value = "회원가입")
	public BaseResponse<String> signup(@RequestBody @Valid SignupRequest request)
		throws BaseException {
		userService.signup(request);
		return new BaseResponse<>("성공");
	}

	@PostMapping("/login")
	@ApiOperation(value = "로그인", notes = "로그인 성공 시 access token, refresh token 발급")
	public BaseResponse<TokensDto> login(@RequestBody @Valid LoginRequest request)
		throws BaseException {
		return new BaseResponse<>(userService.login(request));
	}

	@GetMapping("/auth")
	@ApiOperation(value = "Auth", notes = "access token 으로 로그인 정보 조회")
	public BaseResponse<AuthResponse> auth() throws BaseException {
		return new BaseResponse<>(userService.auth());
	}

	@GetMapping("/refresh-token")
	@ApiOperation(value = "Refresh Token", notes = "access token 만료 시, refresh token 으로 재발급 받기")
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

	@GetMapping("/certification")
	@ApiOperation(value = "(아이디 찾기/비밀번호 찾기) SMS 인증번호 요청")
	public BaseResponse<String> sendSmsCertification(@RequestParam String phoneNumber)
		throws BaseException, UnsupportedEncodingException, URISyntaxException, NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
		userService.sendSmsCertification(phoneNumber);
		return new BaseResponse<>("성공");
	}

	@GetMapping("/certification/id")
	@ApiOperation(value = "(아이디 찾기)SMS 인증번호 확인")
	public BaseResponse<LoginIdResponse> findId(@RequestParam String phoneNumber,
		@RequestParam String code) throws BaseException {
		return new BaseResponse<>(userService.findId(phoneNumber, code));
	}

	@GetMapping("/certification/password")
	@ApiOperation(value = "(비밀번호 찾기)SMS 인증번호 확인")
	public BaseResponse<String> findPassword(@RequestParam String phoneNumber,
		@RequestParam String code) throws BaseException {
		return new BaseResponse<>(userService.findPassword(phoneNumber, code));
	}

	@PutMapping("/certification/password")
	@ApiOperation(value = "(비밀번호 찾기)비밀번호 초기화")
	public BaseResponse<String> resetPassword(@RequestBody ResetPasswordRequest request)
		throws BaseException {
		userService.resetPassword(request);
		return new BaseResponse<>("성공");
	}

	@PutMapping("/password")
	@ApiOperation(value = "비밀번호 변경")
	public BaseResponse<String> changePassword(@RequestBody ChangePasswordRequest request)
		throws BaseException {
		userService.changePassword(request);
		return new BaseResponse<>("성공");
	}

	@PutMapping("/profile")
	@ApiOperation(value = "프로필 변경")
	public BaseResponse<String> changeProfile(@RequestBody ChangeProfileRequest request)
		throws BaseException {
		userService.changeProfile(request);
		return new BaseResponse<>("성공");
	}

	@GetMapping("/user/search")
	@ApiOperation(value = "유저 검색 by 닉네임")
	public BaseResponse<List<UserBriefResponse>> searchUserByNickname(@RequestParam String nickname)
		throws BaseException {
		return new BaseResponse<>(userSearchService.findByNickname(nickname));
	}

	@GetMapping("/user/info/{userId}")
	@ApiOperation(value = "유저 정보 보기")
	public BaseResponse<UserBriefResponse> getUserInfo(@PathVariable(value = "userId") Long userId)
		throws BaseException {
		return new BaseResponse<>(userSearchService.getUserBriefInfo(userId));
	}

	@DeleteMapping("/user/withdrawal")
	@ApiOperation(value = "회원탈퇴")
	public BaseResponse<String> withdrawal() throws BaseException {
		userWithdrawalService.withdrawal();
		return new BaseResponse<>("성공");
	}
}

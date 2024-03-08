package mallang_trip.backend.domain.user.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import mallang_trip.backend.domain.user.dto.TokensDto;
import mallang_trip.backend.domain.user.dto.AuthResponse;
import mallang_trip.backend.domain.user.dto.ChangePasswordRequest;
import mallang_trip.backend.domain.user.dto.ChangeProfileRequest;
import mallang_trip.backend.domain.user.dto.LoginIdResponse;
import mallang_trip.backend.domain.user.dto.LoginRequest;
import mallang_trip.backend.domain.user.dto.ResetPasswordRequest;
import mallang_trip.backend.domain.user.dto.SignupRequest;
import mallang_trip.backend.domain.user.dto.UserBriefResponse;
import mallang_trip.backend.domain.user.service.UserSearchService;
import mallang_trip.backend.domain.user.service.UserService;
import mallang_trip.backend.domain.user.service.UserWithdrawalService;
import org.springframework.security.access.prepost.PreAuthorize;
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
	@ApiResponses({
		@ApiResponse(code = 200, message = "요청에 성공했습니다."),
		@ApiResponse(code = 400, message = "잘못된 요청입니다."),
		@ApiResponse(code = 403, message = "유효하지 않은 imp_uid 입니다."),
		@ApiResponse(code = 409, message = "중복된 값이 존재합니다."),
		@ApiResponse(code = 500, message = "본인인증 서버 통신 실패.")
	})
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<String> signup(@RequestBody @Valid SignupRequest request)
		throws BaseException {
		userService.signup(request);
		return new BaseResponse<>("성공");
	}

	@PostMapping("/login")
	@ApiOperation(value = "로그인", notes = "로그인 성공 시 Access Token, Refresh Token 을 발급합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "요청에 성공했습니다."),
		@ApiResponse(code = 400, message = "잘못된 요청입니다."),
		@ApiResponse(code = 401, message = "사용자 인증에 실패했습니다.")
	})
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<TokensDto> login(@RequestBody @Valid LoginRequest request)
		throws BaseException {
		return new BaseResponse<>(userService.login(request));
	}

	@GetMapping("/auth")
	@ApiOperation(value = "Auth", notes = "Access Token 으로 로그인 정보를 조회합니다.")
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
		@ApiResponse(code = 200, message = "요청에 성공했습니다."),
		@ApiResponse(code = 400, message = "잘못된 요청입니다."),
		@ApiResponse(code = 401, message = "사용자 인증에 실패했습니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Access Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Access Token 입니다.")
	})
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<AuthResponse> auth() throws BaseException {
		return new BaseResponse<>(userService.auth());
	}

	@GetMapping("/refresh-token")
	@ApiOperation(value = "Refresh Token", notes = "Refresh Token 으로 Access Token 을 재발급 받습니다.")
	@ApiImplicitParam(name = "refresh-token", value = "Refresh Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
		@ApiResponse(code = 200, message = "요청에 성공했습니다."),
		@ApiResponse(code = 400, message = "잘못된 요청입니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<TokensDto> refreshToken() throws BaseException {
		return new BaseResponse<>(userService.refreshToken());
	}

	@GetMapping("/check-duplication")
	@ApiOperation(value = "중복 확인", notes = "회원가입 시 중복 확인")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "type", value = "[phoneNumber/loginId/email/nickname] 중 하나", required = true, paramType = "query", dataTypeClass = String.class),
		@ApiImplicitParam(name = "value", value = "중복 확인할 값", required = true, paramType = "query", dataTypeClass = String.class)
	})
	@ApiResponses({
		@ApiResponse(code = 200, message = "사용 가능한 값입니다."),
		@ApiResponse(code = 400, message = "잘못된 요청입니다."),
		@ApiResponse(code = 409, message = "사용 불가한(중복된) 값입니다.")
	})
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<String> checkDuplication(@RequestParam String type,
		@RequestParam String value) throws BaseException {
		userService.checkDuplication(type, value);
		return new BaseResponse<>("사용 가능");
	}

	@GetMapping("/certification")
	@ApiOperation(value = "SMS 인증번호 요청", notes = "아이디/비밀번호 찾기를 위한 SMS 인증번호를 발송합니다. 인증번호의 유효시간은 5분입니다.")
	@ApiImplicitParam(name = "phoneNumber", value = "휴대폰 번호", required = true, paramType = "query", dataTypeClass = String.class)
	@ApiResponses({
		@ApiResponse(code = 200, message = "요청에 성공했습니다."),
		@ApiResponse(code = 400, message = "잘못된 요청입니다."),
		@ApiResponse(code = 404, message = "해당 휴대폰 번호로 가입된 정보가 없습니다."),
		@ApiResponse(code = 500, message = "SMS 발송 서버 통신 실패.")
	})
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<String> sendSmsCertification(@RequestParam String phoneNumber)
		throws BaseException {
		userService.sendSmsCertification(phoneNumber);
		return new BaseResponse<>("성공");
	}

	@GetMapping("/certification/id")
	@ApiOperation(value = "(아이디 찾기)SMS 인증번호 확인", notes = "인증번호 일치 시, 가입된 로그인 아이디를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "phoneNumber", value = "휴대폰 번호", required = true, paramType = "query", dataTypeClass = String.class),
		@ApiImplicitParam(name = "code", value = "인증번호", required = true, paramType = "query", dataTypeClass = String.class)
	})
	@ApiResponses({
		@ApiResponse(code = 200, message = "요청에 성공했습니다."),
		@ApiResponse(code = 400, message = "잘못된 요청입니다."),
		@ApiResponse(code = 401, message = "인증번호가 일치하지 않습니다."),
		@ApiResponse(code = 404, message = "해당 휴대폰 번호로 가입된 정보가 없습니다."),
	})
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<LoginIdResponse> findId(@RequestParam String phoneNumber,
		@RequestParam String code) throws BaseException {
		return new BaseResponse<>(userService.findId(phoneNumber, code));
	}

	@GetMapping("/certification/password")
	@ApiOperation(value = "(비밀번호 찾기)SMS 인증번호 확인", notes = "인증번호 일치 시, 인증번호 유효시간을 5분 연장(재설정)합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "phoneNumber", value = "휴대폰 번호", required = true, paramType = "query", dataTypeClass = String.class),
		@ApiImplicitParam(name = "code", value = "인증번호", required = true, paramType = "query", dataTypeClass = String.class)
	})
	@ApiResponses({
		@ApiResponse(code = 200, message = "요청에 성공했습니다."),
		@ApiResponse(code = 400, message = "잘못된 요청입니다."),
		@ApiResponse(code = 401, message = "인증번호가 일치하지 않습니다.")
	})
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<String> findPassword(@RequestParam String phoneNumber,
		@RequestParam String code) throws BaseException {
		userService.findPassword(phoneNumber, code);
		return new BaseResponse<>("성공");
	}

	@PutMapping("/certification/password")
	@ApiOperation(value = "(비밀번호 찾기)비밀번호 초기화", notes = "인증번호 일치 시, 새로운 비밀번호로 변경합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "요청에 성공했습니다."),
		@ApiResponse(code = 400, message = "잘못된 요청입니다."),
		@ApiResponse(code = 401, message = "인증번호가 일치하지 않습니다."),
		@ApiResponse(code = 404, message = "해당 휴대폰 번호로 가입된 정보가 없습니다.")
	})
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request)
		throws BaseException {
		userService.resetPassword(request);
		return new BaseResponse<>("성공");
	}

	@PutMapping("/password")
	@ApiOperation(value = "비밀번호 변경", notes = "기존 비밀번호 일치 시, 새 비밀번호로 재설정합니다.")
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
		@ApiResponse(code = 200, message = "요청에 성공했습니다."),
		@ApiResponse(code = 400, message = "잘못된 요청입니다."),
		@ApiResponse(code = 401, message = "인증되지 않은 사용자이거나, 비밀번호가 일치하지 않습니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Access Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Access Token 입니다.")
	})
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> changePassword(@RequestBody @Valid ChangePasswordRequest request)
		throws BaseException {
		userService.changePassword(request);
		return new BaseResponse<>("성공");
	}

	@PutMapping("/profile")
	@ApiOperation(value = "프로필 변경", notes = "입력한 값으로 프로필을 변경합니다. 변경하지 않는 값이라도 기존 값을 입력해야 합니다.")
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
		@ApiResponse(code = 200, message = "요청에 성공했습니다."),
		@ApiResponse(code = 400, message = "잘못된 요청입니다."),
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 409, message = "중복된 값이 존재합니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Access Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Access Token 입니다.")
	})
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> changeProfile(@RequestBody @Valid ChangeProfileRequest request)
		throws BaseException {
		userService.changeProfile(request);
		return new BaseResponse<>("성공");
	}

	@GetMapping("/user/search")
	@ApiOperation(value = "유저 검색", notes = "닉네임으로 다른 유저를 검색합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
		@ApiImplicitParam(name = "nickname", value = "닉네임", required = true, paramType = "query", dataTypeClass = String.class)
	})
	@ApiResponses({
		@ApiResponse(code = 200, message = "요청에 성공했습니다."),
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Access Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Access Token 입니다.")
	})
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<List<UserBriefResponse>> searchUserByNickname(@RequestParam String nickname)
		throws BaseException {
		return new BaseResponse<>(userSearchService.findByNickname(nickname));
	}

	@GetMapping("/user/info/{userId}")
	@ApiOperation(value = "유저 정보 보기", notes = "user_id 로 다른 유저의 정보를 조회합니다.")
	@ApiImplicitParam(name = "userId", value = "user_id", required = true, paramType = "path", dataTypeClass = Long.class)
	@ApiResponses({
		@ApiResponse(code = 200, message = "요청에 성공했습니다."),
		@ApiResponse(code = 404, message = "해당 유저를 찾을 수 없습니다.")
	})
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<UserBriefResponse> getUserInfo(@PathVariable(value = "userId") Long userId)
		throws BaseException {
		return new BaseResponse<>(userSearchService.getUserBriefInfo(userId));
	}

	@DeleteMapping("/user/withdrawal")
	@ApiOperation(value = "회원탈퇴")
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
		@ApiResponse(code = 200, message = "요청에 성공했습니다."),
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 403, message = "회원탈퇴가 불가능합니다. 진행중인 여행이 존재하거나, 미납금이 존재합니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Access Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Access Token 입니다.")
	})
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> withdrawal() throws BaseException {
		userWithdrawalService.withdrawal();
		return new BaseResponse<>("성공");
	}
}

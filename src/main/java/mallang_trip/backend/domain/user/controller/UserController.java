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

	/**
	 * 회원가입을 처리하는 메소드입니다.
	 *
	 * @param request 회원가입 요청 객체
	 * @return 회원가입 결과를 담은 BaseResponse 객체
	 * @throws BaseException 기본 예외 처리를 위한 예외 객체
	 */
	@PostMapping("/signup")
	@ApiOperation(value = "회원가입")
	@ApiResponses({
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

	/**
	 * 로그인 API입니다.
	 * 로그인 성공 시 access token과 refresh token을 발급합니다.
	 *
	 * @param request 로그인 요청 객체
	 * @return 로그인 결과를 담은 응답 객체
	 * @throws BaseException 기본 예외 처리를 위한 예외 객체
	 */
	@PostMapping("/login")
	@ApiOperation(value = "로그인", notes = "로그인 성공 시 Access Token, Refresh Token 을 발급합니다.")
	@ApiResponse(code = 401, message = "사용자 인증에 실패했습니다.")
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<TokensDto> login(@RequestBody @Valid LoginRequest request)
		throws BaseException {
		return new BaseResponse<>(userService.login(request));
	}

	/**
	 * 액세스 토큰을 사용하여 로그인 정보를 검색합니다.
	 *
	 * @return 인증 응답을 포함하는 BaseResponse 객체
	 * @throws BaseException 인증 과정 중에 오류가 발생한 경우
	 */
	@GetMapping("/auth")
	@ApiOperation(value = "Auth", notes = "Access Token 으로 로그인 정보를 조회합니다.")
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
		@ApiResponse(code = 401, message = "사용자 인증에 실패했습니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Access Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Access Token 입니다.")
	})
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<AuthResponse> auth() throws BaseException {
		return new BaseResponse<>(userService.auth());
	}

	/**
	 * Refresh Token
	 *
	 * access token 만료 시, refresh token 으로 재발급 받기
	 *
	 * @return BaseResponse<TokensDto> - 재발급된 토큰 정보를 포함한 응답 객체
	 * @throws BaseException - 기본 예외 처리를 위한 예외 객체
	 */
	@GetMapping("/refresh-token")
	@ApiOperation(value = "Refresh Token", notes = "Refresh Token 으로 Access Token 을 재발급 받습니다.")
	@ApiImplicitParam(name = "refresh-token", value = "Refresh Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<TokensDto> refreshToken() throws BaseException {
		return new BaseResponse<>(userService.refreshToken());
	}

	/**
	 * 중복 확인 메소드입니다.
	 * 회원가입 시 중복된 값인지 확인합니다.
	 *
	 * @param type 중복 확인할 유형입니다. [phoneNumber/loginId/email/nickname] 중 하나를 입력해야 합니다.
	 * @param value 중복 확인할 값입니다.
	 * @return 중복 여부에 따라 다른 응답을 반환합니다.
	 *         - 사용 가능한 경우, "사용 가능" 메시지를 포함한 BaseResponse 객체를 반환합니다.
	 *         - 중복된 경우, 409 상태 코드와 "중복(사용불가)" 메시지를 포함한 BaseResponse 객체를 반환합니다.
	 * @throws BaseException 중복 확인 과정에서 예외가 발생한 경우, BaseException을 던집니다.
	 */
	@GetMapping("/check-duplication")
	@ApiOperation(value = "중복 확인", notes = "회원가입 시 중복 확인")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "type", value = "[phoneNumber/loginId/email/nickname] 중 하나", required = true, paramType = "query", dataTypeClass = String.class),
		@ApiImplicitParam(name = "value", value = "중복 확인할 값", required = true, paramType = "query", dataTypeClass = String.class)
	})
	@ApiResponse(code = 409, message = "사용 불가한(중복된) 값입니다.")
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<String> checkDuplication(@RequestParam String type,
		@RequestParam String value) throws BaseException {
		userService.checkDuplication(type, value);
		return new BaseResponse<>("사용 가능");
	}

	/**
	 * (아이디 찾기/비밀번호 찾기) SMS 인증번호 요청을 처리하는 메소드입니다.
	 *
	 * @param phoneNumber SMS 인증번호를 받을 휴대폰 번호
	 * @return SMS 인증번호 요청 결과를 담은 BaseResponse 객체
	 * @throws BaseException 기본 예외 클래스
	 * @throws UnsupportedEncodingException 인코딩 예외
	 * @throws URISyntaxException URI 문법 예외
	 * @throws NoSuchAlgorithmException 암호화 알고리즘 예외
	 * @throws InvalidKeyException 유효하지 않은 키 예외
	 * @throws JsonProcessingException JSON 처리 예외
	 */
	@GetMapping("/certification")
	@ApiOperation(value = "SMS 인증번호 요청", notes = "아이디/비밀번호 찾기를 위한 SMS 인증번호를 발송합니다. 인증번호의 유효시간은 5분입니다.")
	@ApiImplicitParam(name = "phoneNumber", value = "휴대폰 번호", required = true, paramType = "query", dataTypeClass = String.class)
	@ApiResponses({
		@ApiResponse(code = 404, message = "해당 휴대폰 번호로 가입된 정보가 없습니다."),
		@ApiResponse(code = 500, message = "SMS 발송 서버 통신 실패.")
	})
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<String> sendSmsCertification(@RequestParam String phoneNumber)
		throws BaseException {
		userService.sendSmsCertification(phoneNumber);
		return new BaseResponse<>("성공");
	}

	/**
	 * (아이디 찾기) SMS 인증번호 확인을 위한 API입니다.
	 *
	 * @param phoneNumber 사용자의 휴대폰 번호
	 * @param code SMS 인증번호
	 * @return SMS 인증번호 확인 결과를 담은 BaseResponse 객체
	 * @throws BaseException 기본 예외 처리를 위한 예외 객체
	 */
	@GetMapping("/certification/id")
	@ApiOperation(value = "(아이디 찾기)SMS 인증번호 확인", notes = "인증번호 일치 시, 가입된 로그인 아이디를 반환합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "phoneNumber", value = "휴대폰 번호", required = true, paramType = "query", dataTypeClass = String.class),
		@ApiImplicitParam(name = "code", value = "인증번호", required = true, paramType = "query", dataTypeClass = String.class)
	})
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증번호가 일치하지 않습니다."),
		@ApiResponse(code = 404, message = "해당 휴대폰 번호로 가입된 정보가 없습니다."),
	})
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<LoginIdResponse> findId(@RequestParam String phoneNumber,
		@RequestParam String code) throws BaseException {
		return new BaseResponse<>(userService.findId(phoneNumber, code));
	}

	/**
	 * (비밀번호 찾기) SMS 인증번호 확인을 위한 API입니다.
	 *
	 * @param phoneNumber 사용자의 휴대폰 번호
	 * @param code SMS로 전송된 인증번호
	 * @return 인증번호 확인 결과를 포함한 응답 객체
	 * @throws BaseException 기본 예외 처리를 위한 예외 객체
	 */
	@GetMapping("/certification/password")
	@ApiOperation(value = "(비밀번호 찾기)SMS 인증번호 확인", notes = "인증번호 일치 시, 인증번호 유효시간을 5분 연장(재설정)합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "phoneNumber", value = "휴대폰 번호", required = true, paramType = "query", dataTypeClass = String.class),
		@ApiImplicitParam(name = "code", value = "인증번호", required = true, paramType = "query", dataTypeClass = String.class)
	})
	@ApiResponse(code = 401, message = "인증번호가 일치하지 않습니다.")
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<String> findPassword(@RequestParam String phoneNumber,
		@RequestParam String code) throws BaseException {
		userService.findPassword(phoneNumber, code);
		return new BaseResponse<>("성공");
	}

	/**
	 * 비밀번호 초기화를 수행하는 메소드입니다.
	 *
	 * @param request 비밀번호 초기화 요청 객체
	 * @return 비밀번호 초기화 결과를 담은 BaseResponse 객체
	 * @throws BaseException 비밀번호 초기화 중 발생한 예외
	 */
	@PutMapping("/certification/password")
	@ApiOperation(value = "(비밀번호 찾기)비밀번호 초기화", notes = "인증번호 일치 시, 새로운 비밀번호로 변경합니다.")
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증번호가 일치하지 않습니다."),
		@ApiResponse(code = 404, message = "해당 휴대폰 번호로 가입된 정보가 없습니다.")
	})
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request)
		throws BaseException {
		userService.resetPassword(request);
		return new BaseResponse<>("성공");
	}

	/**
	 * 비밀번호를 변경하는 메소드입니다.
	 *
	 * @param request 비밀번호 변경 요청 객체
	 * @return 비밀번호 변경 결과를 담은 BaseResponse 객체
	 * @throws BaseException 비밀번호 변경 중 발생한 예외
	 */
	@PutMapping("/password")
	@ApiOperation(value = "비밀번호 변경", notes = "기존 비밀번호 일치 시, 새 비밀번호로 재설정합니다.")
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
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

	/**
	 * 프로필을 변경하는 메소드입니다.
	 *
	 * @param request 프로필 변경 요청 객체
	 * @return 프로필 변경 결과를 담은 BaseResponse 객체
	 * @throws BaseException 기본 예외 처리를 위한 예외 객체
	 */
	@PutMapping("/profile")
	@ApiOperation(value = "프로필 변경", notes = "입력한 값으로 프로필을 변경합니다. 변경하지 않는 값이라도 기존 값을 입력해야 합니다.")
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
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

	/**
	 * 닉네임으로 유저를 검색하는 메소드입니다.
	 *
	 * @param nickname 검색할 유저의 닉네임
	 * @return 검색된 유저의 목록을 담은 BaseResponse 객체
	 * @throws BaseException 검색 과정에서 발생하는 예외
	 */
	@GetMapping("/user/search")
	@ApiOperation(value = "유저 검색", notes = "닉네임으로 다른 유저를 검색합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
		@ApiImplicitParam(name = "nickname", value = "닉네임", required = true, paramType = "query", dataTypeClass = String.class)
	})
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Access Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Access Token 입니다.")
	})
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<List<UserBriefResponse>> searchUserByNickname(@RequestParam String nickname)
		throws BaseException {
		return new BaseResponse<>(userSearchService.findByNickname(nickname));
	}

	/**
	 * 유저 정보를 조회하는 메소드입니다.
	 *
	 * @param userId 조회할 유저의 아이디
	 * @return 유저의 간단한 정보를 담은 BaseResponse 객체
	 * @throws BaseException 유저 정보 조회 중 발생한 예외
	 */
	@GetMapping("/user/info/{userId}")
	@ApiOperation(value = "유저 정보 보기", notes = "user_id 로 다른 유저의 정보를 조회합니다.")
	@ApiImplicitParam(name = "userId", value = "user_id", required = true, paramType = "path", dataTypeClass = Long.class)
	@ApiResponse(code = 404, message = "해당 유저를 찾을 수 없습니다.")
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<UserBriefResponse> getUserInfo(@PathVariable(value = "userId") Long userId)
		throws BaseException {
		return new BaseResponse<>(userSearchService.getUserBriefInfo(userId));
	}

	/**
	 * 회원탈퇴를 처리하는 메소드입니다.
	 *
	 * @return 회원탈퇴 성공 여부를 담은 BaseResponse 객체
	 * @throws BaseException 기본 예외 처리를 위한 BaseException
	 */
	@DeleteMapping("/user/withdrawal")
	@ApiOperation(value = "회원탈퇴")
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
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

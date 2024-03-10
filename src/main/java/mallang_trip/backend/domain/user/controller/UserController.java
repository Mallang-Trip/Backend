package mallang_trip.backend.domain.user.controller;

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
	@ApiOperation(value = "로그인", notes = "로그인 성공 시 access token, refresh token 발급")
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
	@ApiOperation(value = "Auth", notes = "access token 으로 로그인 정보 조회")
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
	@ApiOperation(value = "Refresh Token", notes = "access token 만료 시, refresh token 으로 재발급 받기")
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
	@ApiOperation(value = "(아이디 찾기/비밀번호 찾기) SMS 인증번호 요청")
	public BaseResponse<String> sendSmsCertification(@RequestParam String phoneNumber)
		throws BaseException, UnsupportedEncodingException, URISyntaxException, NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
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
	@ApiOperation(value = "(아이디 찾기)SMS 인증번호 확인")
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
	@ApiOperation(value = "(비밀번호 찾기)SMS 인증번호 확인")
	public BaseResponse<String> findPassword(@RequestParam String phoneNumber,
		@RequestParam String code) throws BaseException {
		return new BaseResponse<>(userService.findPassword(phoneNumber, code));
	}

	/**
	 * 비밀번호 초기화를 수행하는 메소드입니다.
	 *
	 * @param request 비밀번호 초기화 요청 객체
	 * @return 비밀번호 초기화 결과를 담은 BaseResponse 객체
	 * @throws BaseException 비밀번호 초기화 중 발생한 예외
	 */
	@PutMapping("/certification/password")
	@ApiOperation(value = "(비밀번호 찾기)비밀번호 초기화")
	public BaseResponse<String> resetPassword(@RequestBody ResetPasswordRequest request)
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
	@ApiOperation(value = "비밀번호 변경")
	public BaseResponse<String> changePassword(@RequestBody ChangePasswordRequest request)
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
	@ApiOperation(value = "프로필 변경")
	public BaseResponse<String> changeProfile(@RequestBody ChangeProfileRequest request)
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
	@ApiOperation(value = "유저 검색 by 닉네임")
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
	@ApiOperation(value = "유저 정보 보기")
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
	public BaseResponse<String> withdrawal() throws BaseException {
		userWithdrawalService.withdrawal();
		return new BaseResponse<>("성공");
	}
}

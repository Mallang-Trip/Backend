package mallang_trip.backend.domain.user.service;

import static mallang_trip.backend.domain.user.constant.Role.ROLE_ADMIN;
import static mallang_trip.backend.global.io.BaseResponseStatus.Bad_Request;
import static mallang_trip.backend.global.io.BaseResponseStatus.Conflict;
import static mallang_trip.backend.global.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.global.io.BaseResponseStatus.Unauthorized;
import static mallang_trip.backend.domain.user.constant.Role.ROLE_USER;
import static mallang_trip.backend.domain.user.exception.UserExceptionStatus.CANNOT_FOUND_USER;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.user.dto.*;
import mallang_trip.backend.global.config.security.TokenProvider;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.identification.dto.IdentificationResultResponse;
import mallang_trip.backend.domain.identification.service.PortOneIdentificationService;
import mallang_trip.backend.domain.user.constant.Country;
import mallang_trip.backend.domain.user.constant.Gender;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.sms.service.SmsService;
import mallang_trip.backend.domain.user.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

	private final UserRepository userRepository;
	private final AuthenticationManagerBuilder managerBuilder;
	private final TokenProvider tokenProvider;
	private final PasswordEncoder passwordEncoder;
	private final CurrentUserService currentUserService;
	private final SmsService smsService;
	private final PortOneIdentificationService portOneIdentificationService;

	private final UserSearchService userSearchService;

	/**
	 * 회원 가입을 처리하는 메소드입니다.
	 *
	 * @param request 회원 가입 요청 객체
	 * @throws BaseException 중복된 회원 정보가 있을 경우 발생하는 예외
	 */
	public void signup(SignupRequest request) {
		if (isDuplicate(request)) {
			throw new BaseException(Conflict);
		}

		IdentificationResultResponse.CertificationAnnotation response =
			portOneIdentificationService.get(request.getImpUid()).getResponse();

		if(!response.getName().equals(request.getName()) || !response.getPhone().equals(request.getPhone())){
			throw new BaseException(Forbidden);
		}

		userRepository.save(User.builder()
			.loginId(request.getId())
			.password(passwordEncoder.encode(request.getPassword()))
			.email(request.getEmail())
			.name(response.getName())
			.birthday(LocalDate.parse(response.getBirthday()))
			.country(Country.from(request.getCountry()))
			.gender(Gender.from(response.getGender()))
			.phoneNumber(response.getPhone())
			.nickname(request.getNickname())
			.introduction(request.getIntroduction())
			.profileImage(request.getProfileImg())
			.role(ROLE_USER)
			.build());
	}

	/**
	 * 로그인 요청에 대한 토큰 정보를 반환하는 메소드입니다.
	 *
	 * @param request 로그인 요청 객체
	 * @return 토큰 정보를 담은 TokensDto 객체
	 */
	public TokensDto login(LoginRequest request) {
		UsernamePasswordAuthenticationToken token = request.toAuthentication();
		Authentication authentication = managerBuilder.getObject().authenticate(token);

		return tokenProvider.createToken(authentication);
	}

	/**
	 * Auth (내 정보 조회)
	 */
	public AuthResponse auth() {
		User user = currentUserService.getCurrentUser();
		return AuthResponse.of(user);
	}

	/**
	 * Access Token 재발급
	 */
	public TokensDto refreshToken() {
		return tokenProvider.doRefresh();
	}

	/**
	 * 중복 체크를 수행하는 메서드입니다.
	 *
	 * @param type  중복 체크할 유형 (phoneNumber, loginId, email, nickname)
	 * @param value 중복 체크할 값
	 * @throws BaseException 중복이 발생한 경우 예외를 던집니다.
	 */
	public void checkDuplication(String type, String value) {
		boolean isDuplicate;
		switch (type) {
			case "phoneNumber":
				isDuplicate = userRepository.existsByPhoneNumber(value);
				break;
			case "loginId":
				isDuplicate = userRepository.existsByLoginId(value);
				break;
			case "email":
				isDuplicate = userRepository.existsByEmail(value);
				break;
			case "nickname":
				isDuplicate = userRepository.existsByNickname(value);
				break;
			default:
				throw new BaseException(Bad_Request);
		}
		if (isDuplicate) {
			throw new BaseException(Conflict);
		}
	}

	/**
	 * 중복 여부를 확인하는 메소드입니다.
	 *
	 * @param request 회원가입 요청 객체
	 * @return 중복이 있는 경우 true, 없는 경우 false를 반환합니다.
	 */
	private Boolean isDuplicate(SignupRequest request) {
		if (userRepository.existsByLoginId(request.getId())
			|| userRepository.existsByEmail(request.getEmail())
			|| userRepository.existsByNickname(request.getNickname())
			|| userRepository.existsByPhoneNumber(request.getPhone())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 휴대폰 번호를 인증하기 위해 SMS를 전송하는 메소드입니다.
	 * (아이디 찾기, 비밀번호 찾기 공통)
	 *
	 * @param phoneNumber 인증할 휴대폰 번호
	 * @throws UnsupportedEncodingException 인코딩이 지원되지 않을 때 발생하는 예외
	 * @throws URISyntaxException 잘못된 URI 문법이 있을 때 발생하는 예외
	 * @throws NoSuchAlgorithmException 암호화 알고리즘이 지원되지 않을 때 발생하는 예외
	 * @throws InvalidKeyException 잘못된 키가 제공되었을 때 발생하는 예외
	 * @throws JsonProcessingException JSON 처리 중 오류가 발생한 경우 발생하는 예외
	 * @throws BaseException 사용자를 찾을 수 없을 때 발생하는 예외
	 */
	public void sendSmsCertification(String phoneNumber) {
		if (!userRepository.existsByPhoneNumber(phoneNumber)) {
			throw new BaseException(CANNOT_FOUND_USER);
		}
		smsService.sendSmsCertification(phoneNumber);
	}

	/**
	 * 아이디 찾기 (인증코드 확인)
	 */
	public LoginIdResponse findId(String phoneNumber, String code) {
		if (!smsService.verifyAndDeleteCode(phoneNumber, code)) {
			throw new BaseException(Unauthorized);
		}

		User user = userRepository.findByPhoneNumber(phoneNumber)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));

		return LoginIdResponse.builder()
			.loginId(user.getLoginId())
			.build();
	}

	/**
	 * 비밀번호를 찾는 메서드입니다.
	 *
	 * @param phoneNumber 전화번호
	 * @param code 인증 코드
	 * @return 비밀번호 찾기 성공 시 "성공" 문자열 반환
	 * @throws BaseException 인증 실패 시 Unauthorized 예외 발생
	 */
	public void findPassword(String phoneNumber, String code) {
		if (!smsService.verifyAndExtendCode(phoneNumber, code)) {
			throw new BaseException(Unauthorized);
		}
	}

	/**
	 * 비밀번호 초기화 (인증코드 확인 성공 시)
	 *
	 * @param request 비밀번호 재설정 요청 객체
	 * @throws BaseException 인증되지 않은 사용자일 경우 예외가 발생합니다.
	 */
	public void resetPassword(ResetPasswordRequest request) {
		if (!smsService.verifyAndDeleteCode(request.getPhoneNumber(), request.getCode())) {
			throw new BaseException(Unauthorized);
		} else {
			User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
				.orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
			user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		}
	}

	/**
	 * 비밀번호를 변경하는 메서드입니다.
	 *
	 * @param request 비밀번호 변경 요청 객체
	 * @throws BaseException 이전 비밀번호가 일치하지 않을 경우 발생하는 예외
	 */
	public void changePassword(ChangePasswordRequest request) {
		User user = currentUserService.getCurrentUser();
		if (!passwordEncoder.matches(request.getBefore(), user.getPassword())) {
			throw new BaseException(Unauthorized);
		}
		user.setPassword(passwordEncoder.encode(request.getAfter()));
	}

	/**
	 * 프로필을 변경하는 메소드입니다.
	 *
	 * @param request 프로필 변경 요청 객체
	 */
	public void changeProfile(ChangeProfileRequest request) {
		User user = currentUserService.getCurrentUser();
		String newNickname = request.getNickname();
		String newEmail = request.getEmail();

		if (!newNickname.equals(user.getNickname())) {
			checkDuplication("nickname", newNickname);
			user.setNickname(newNickname);
		}
		if (!newEmail.equals(user.getEmail())) {
			checkDuplication("email", newEmail);
			user.setEmail(newEmail);
		}
		user.setProfileImage(request.getProfileImg());
		user.setIntroduction(request.getIntroduction());
	}

	/**
	 * (관리자) 회원 관리자 권한 부여
	 *
	 */
	public void grantAdminRole(GrantAdminRoleRequest request) {
		// List<Integer> userIds
		List<Long> userIds = request.getUserIds();
		for (Long userId : userIds) {
			User user = userRepository.findById(userId)
					.orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
			if (!user.getRole().equals(ROLE_ADMIN)) {
				user.setRole(ROLE_ADMIN);
			}
		}
	}

	/**
	 * (관리자) 회원 관리자 권한 해제
	 *
	 */
	public void revokeAdminRole(Long userId){
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
		if(user.getRole().equals(ROLE_ADMIN)){
			user.setRole(ROLE_USER);
		}
	}
}

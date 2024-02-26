package mallang_trip.backend.domain.user.service;

import static mallang_trip.backend.global.io.BaseResponseStatus.Bad_Request;
import static mallang_trip.backend.global.io.BaseResponseStatus.Conflict;
import static mallang_trip.backend.global.io.BaseResponseStatus.EMPTY_JWT;
import static mallang_trip.backend.global.io.BaseResponseStatus.Unauthorized;
import static mallang_trip.backend.domain.user.constant.Role.ROLE_USER;
import static mallang_trip.backend.domain.user.exception.UserExceptionStatus.CANNOT_FOUND_USER;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.global.config.security.TokenProvider;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.identification.dto.IdentificationResultResponse;
import mallang_trip.backend.domain.identification.service.PortOneIdentificationService;
import mallang_trip.backend.domain.user.constant.Country;
import mallang_trip.backend.domain.user.constant.Gender;
import mallang_trip.backend.domain.user.dto.TokensDto;
import mallang_trip.backend.domain.user.dto.AuthResponse;
import mallang_trip.backend.domain.user.dto.ChangePasswordRequest;
import mallang_trip.backend.domain.user.dto.ChangeProfileRequest;
import mallang_trip.backend.domain.user.dto.LoginIdResponse;
import mallang_trip.backend.domain.user.dto.LoginRequest;
import mallang_trip.backend.domain.user.dto.ResetPasswordRequest;
import mallang_trip.backend.domain.user.dto.SignupRequest;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.sms.service.SmsService;
import mallang_trip.backend.domain.user.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
	private final SmsService smsService;
	private final PortOneIdentificationService portOneIdentificationService;

	/**
	 * 회원가입
	 */
	public void signup(SignupRequest request) {
		if(isDuplicate(request)){
			throw new BaseException(Conflict);
		}
		IdentificationResultResponse.CertificationAnnotation response =
			portOneIdentificationService.get(request.getImpUid()).getResponse();
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
	 * 로그인
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
		User user = getCurrentUser();
		return AuthResponse.of(user);
	}

	/**
	 * Access Token 재발급
	 */
	public TokensDto refreshToken() {
		return tokenProvider.doRefresh();
	}

	/**
	 * 가입 정보 중복 확인
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
	 * User table unique check
	 */
	private Boolean isDuplicate(SignupRequest request){
		if(userRepository.existsByLoginId(request.getId())
		|| userRepository.existsByEmail(request.getEmail())
		|| userRepository.existsByNickname(request.getNickname())){
			return true;
		}
		else{
			return false;
		}
	}

	/**
	 * 현재 유저 조회
	 */
	public User getCurrentUser() {
		final Authentication authentication = SecurityContextHolder.getContext()
			.getAuthentication();
		if (authentication == null || authentication.getName() == null) {
			throw new RuntimeException("Security Context에 인증 정보가 없습니다.");
		}
		User user = authentication.getName().equals("anonymousUser") ? null
			: userRepository.findById(Long.parseLong(authentication.getName()))
				.orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
		return user;
	}

	/**
	 * STOMP header 기반 현재 유저 조회
	 */
	public User getCurrentUser(String accessToken) {
		if (accessToken == null) {
			throw new BaseException(EMPTY_JWT);
		}
		String token = accessToken.substring(7);
		Authentication authentication = tokenProvider.getAuthentication(token);
		User user = userRepository.findById(Long.parseLong(authentication.getName()))
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
		return user;
	}

	/**
	 * 인증 코드 보내기 (아이디 찾기, 비밀번호 찾기 공통)
	 */
	public void sendSmsCertification(String phoneNumber)
		throws UnsupportedEncodingException, URISyntaxException, NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
		if (!userRepository.existsByPhoneNumber(phoneNumber)) {
			throw new BaseException(CANNOT_FOUND_USER);
		}
		smsService.sendSmsCertification(phoneNumber);
	}

	/**
	 * 아이디 찾기 (인증코드 확인)
	 */
	public LoginIdResponse findId(String phoneNumber, String code) {
		if (!userRepository.existsByPhoneNumber(phoneNumber)) {
			throw new BaseException(CANNOT_FOUND_USER);
		}
		if (!smsService.verifyAndDeleteCode(phoneNumber, code)) {
			throw new BaseException(Unauthorized);
		} else {
			User user = userRepository.findByPhoneNumber(phoneNumber)
				.orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
			return LoginIdResponse.builder()
				.loginId(user.getLoginId())
				.build();
		}
	}

	/**
	 * 비밀번호 찾기 (인증코드 확인)
	 */
	public String findPassword(String phoneNumber, String code) {
		if (!smsService.verifyAndExtendCode(phoneNumber, code)) {
			throw new BaseException(Unauthorized);
		} else {
			return "성공";
		}
	}

	/**
	 * 비밀번호 초기화 (인증코드 확인 성공 시)
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
	 * 비밀번호 변경
	 */
	public void changePassword(ChangePasswordRequest request) {
		User user = getCurrentUser();
		if (!passwordEncoder.matches(request.getBefore(), user.getPassword())) {
			throw new BaseException(Unauthorized);
		}
		user.setPassword(passwordEncoder.encode(request.getAfter()));
	}

	/**
	 * 프로필 변경
	 */
	public void changeProfile(ChangeProfileRequest request) {
		User user = getCurrentUser();
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
}

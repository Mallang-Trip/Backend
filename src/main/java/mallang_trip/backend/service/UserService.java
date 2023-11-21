package mallang_trip.backend.service;

import static mallang_trip.backend.controller.io.BaseResponseStatus.Bad_Request;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_USER;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Conflict;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Unauthorized;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.config.security.TokenProvider;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponseStatus;
import mallang_trip.backend.domain.dto.TokensDto;
import mallang_trip.backend.domain.dto.User.AuthResponse;
import mallang_trip.backend.domain.dto.User.ChangePasswordRequest;
import mallang_trip.backend.domain.dto.User.ChangeProfileRequest;
import mallang_trip.backend.domain.dto.User.LoginIdResponse;
import mallang_trip.backend.domain.dto.User.LoginRequest;
import mallang_trip.backend.domain.dto.User.ResetPasswordRequest;
import mallang_trip.backend.domain.dto.User.SignupRequest;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.user.UserRepository;
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

    // 회원가입
    public void signup(SignupRequest request) {
        userRepository.save(request.toUser(passwordEncoder));
    }

    // 로그인
    // 정지 or 탈퇴한 사용자 처리 필요
    public TokensDto login(LoginRequest request) {
        UsernamePasswordAuthenticationToken token = request.toAuthentication();
        Authentication authentication = managerBuilder.getObject().authenticate(token);

        return tokenProvider.createToken(authentication);
    }

    // Auth (내 정보 조회)
    // 정지 or 탈퇴한 사용자 처리 필요
    public AuthResponse auth() {
        User user = getCurrentUser();
        return AuthResponse.of(user);
    }

    // Access Token 재발급
    public TokensDto refreshToken() {
        return tokenProvider.doRefresh();
    }

    // 중복 확인
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

    // 현재 유저
    public User getCurrentUser() {
        final Authentication authentication = SecurityContextHolder.getContext()
            .getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Security Context에 인증 정보가 없습니다.");
        }
        User user = null;
        if (!authentication.getName().equals("anonymousUser")) {
            user = userRepository.findById(Long.parseLong(authentication.getName()))
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CANNOT_FOUND_USER));
        }
        return user;
    }


    // 인증 코드 보내기
    public void sendSmsCertification(String phoneNumber)
        throws UnsupportedEncodingException, URISyntaxException, NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
        if (!userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new BaseException(CANNOT_FOUND_USER);
        }
        smsService.sendSmsCertification(phoneNumber);
    }

    // 아이디 찾기
    public LoginIdResponse findId(String phoneNumber, String code) {
        if (!userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new BaseException(CANNOT_FOUND_USER);
        }
        if (!smsService.verifyAndDeleteCode(phoneNumber, code)) {
            throw new BaseException(Unauthorized);
        } else {
            return LoginIdResponse.builder()
                .loginId(userRepository.findByPhoneNumber(phoneNumber).getLoginId())
                .build();
        }
    }

    // 비밀번호 찾기
    public String findPassword(String phoneNumber, String code) {
        if (!smsService.verifyAndExtendCode(phoneNumber, code)) {
            throw new BaseException(Unauthorized);
        } else {
            return "성공";
        }
    }

    // 비밀번호 초기화
    public void resetPassword(ResetPasswordRequest request) {
        if (!smsService.verifyAndDeleteCode(request.getPhoneNumber(), request.getCode())) {
            throw new BaseException(Unauthorized);
        } else {
            User user = userRepository.findByPhoneNumber(request.getPhoneNumber());
            if (user == null) {
                throw new BaseException(Not_Found);
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }
    }

    // 비밀번호 변경
    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(request.getBefore(), user.getPassword())) {
            throw new BaseException(Unauthorized);
        }
        user.setPassword(passwordEncoder.encode(request.getAfter()));
    }

    // 프로필 변경
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

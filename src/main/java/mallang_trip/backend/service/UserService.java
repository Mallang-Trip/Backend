package mallang_trip.backend.service;

import static mallang_trip.backend.controller.io.BaseResponseStatus.Bad_Request;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Conflict;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.config.s3.AwsS3Uploader;
import mallang_trip.backend.config.security.TokenProvider;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponseStatus;
import mallang_trip.backend.domain.dto.TokensDto;
import mallang_trip.backend.domain.dto.User.AuthResponse;
import mallang_trip.backend.domain.dto.User.LoginRequest;
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
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final AuthenticationManagerBuilder managerBuilder;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public void signup(SignupRequest request){
        userRepository.save(request.toUser(passwordEncoder));
    }

    // 로그인
    // 정지 or 탈퇴한 사용자 처리 필요
    public TokensDto login(LoginRequest request){
        UsernamePasswordAuthenticationToken token = request.toAuthentication();
        Authentication authentication = managerBuilder.getObject().authenticate(token);

        return tokenProvider.createToken(authentication);
    }

    // Auth (내 정보 조회)
    // 정지 or 탈퇴한 사용자 처리 필요
    public AuthResponse auth(){
        User user = getCurrentUser();
        return AuthResponse.of(user);
    }

    // Access Token 재발급
    public TokensDto refreshToken(){
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
        if(isDuplicate) throw new BaseException(Conflict);
    }

    // 현재 유저
    public User getCurrentUser() {
        final Authentication authentication = SecurityContextHolder.getContext()
            .getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Security Context에 인증 정보가 없습니다.");
        }
        User user = userRepository.findById(Long.parseLong(authentication.getName()))
            .orElseThrow(() -> new BaseException(BaseResponseStatus.CANNOT_FOUND_USER));

        return user;
    }
}

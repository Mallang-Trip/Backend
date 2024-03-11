package mallang_trip.backend.domain.user.service;

import static mallang_trip.backend.global.io.BaseResponseStatus.Unauthorized;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.user.repository.UserRepository;
import mallang_trip.backend.global.config.security.TokenProvider;
import mallang_trip.backend.global.io.BaseException;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CurrentUserService {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    /**
     * 현재 SecurityContextHolder 유저 조회
     */
    public User getCurrentUser() {
        final Authentication authentication = SecurityContextHolder.getContext()
            .getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new BaseException(Unauthorized);
        }
        User user = authentication.getName().equals("anonymousUser") ? null
            : userRepository.findById(Long.parseLong(authentication.getName()))
                .orElseThrow(() -> new BaseException(Unauthorized));
        return user;
    }

    /**
     * StompHeaderAccessor header 기반 현재 유저 조회
     */
    public User getCurrentUser(StompHeaderAccessor accessor) {
        String tokenHeader = accessor.getFirstNativeHeader("access-token");
        if (tokenHeader == null || tokenHeader.isEmpty()) {
            throw new MessageDeliveryException("EMPTY_JWT");
        }
        String token = tokenHeader.substring(7);
        Authentication authentication = tokenProvider.getAuthentication(token);
        User user = userRepository.findById(Long.parseLong(authentication.getName()))
            .orElseThrow(() -> new MessageDeliveryException("INVALID_JWT"));
        return user;
    }
}

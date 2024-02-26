package mallang_trip.backend.domain.user.service;

import static mallang_trip.backend.global.io.BaseResponseStatus.Not_Found;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.user.dto.UserBriefResponse;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.admin.service.SuspensionService;
import mallang_trip.backend.domain.chat.service.ChatBlockService;
import mallang_trip.backend.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserSearchService {

	private final UserRepository userRepository;
	private final UserService userService;
	private final SuspensionService suspensionService;
	private final ChatBlockService chatBlockService;

	/**
	 * 유저 검색 by nickname
	 */
	public List<UserBriefResponse> findByNickname(String nickname) {
		User currentUser = userService.getCurrentUser();
		return userRepository.findByNicknameContainingIgnoreCaseAndDeleted(nickname, false).stream()
			.filter(user -> !user.equals(currentUser))
			.filter(user -> !chatBlockService.isBlocked(user, currentUser))
			.filter(user -> !suspensionService.isSuspending(user))
			.map(UserBriefResponse::of)
			.collect(Collectors.toList());
	}

	/**
	 * 유저 간단 프로필 조회
	 */
	public UserBriefResponse getUserBriefInfo(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BaseException(Not_Found));
		return UserBriefResponse.of(user);
	}
}

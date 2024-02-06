package mallang_trip.backend.service.user;

import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.user.UserBriefResponse;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.user.UserRepository;
import mallang_trip.backend.service.admin.SuspensionService;
import mallang_trip.backend.service.chat.ChatBlockService;
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

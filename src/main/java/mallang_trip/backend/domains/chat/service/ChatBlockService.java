package mallang_trip.backend.domains.chat.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domains.global.io.BaseException;
import mallang_trip.backend.domains.global.io.BaseResponseStatus;
import mallang_trip.backend.domains.user.dto.UserBriefResponse;
import mallang_trip.backend.domains.chat.entity.ChatBlock;
import mallang_trip.backend.domains.user.entity.User;
import mallang_trip.backend.domains.chat.repository.ChatBlockRepository;
import mallang_trip.backend.domains.user.repository.UserRepository;
import mallang_trip.backend.domains.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatBlockService {

	private final UserService userService;
	private final ChatBlockRepository chatBlockRepository;
	private final UserRepository userRepository;

	/**
	 * 차단하기
	 */
	public void save(Long targetUserId){
		User user = userService.getCurrentUser();
		User targetUser = userRepository.findById(targetUserId)
				.orElseThrow(() -> new BaseException(BaseResponseStatus.CANNOT_FOUND_USER));
		if(isBlocked(user, targetUser)){
			return;
		}
		chatBlockRepository.save(ChatBlock.builder()
			.user(user)
			.targetUser(targetUser)
			.build());
	}

	/**
	 * 차단 취소
	 */
	public void delete(Long targetUserId){
		User user = userService.getCurrentUser();
		User targetUser = userRepository.findById(targetUserId)
			.orElseThrow(() -> new BaseException(BaseResponseStatus.CANNOT_FOUND_USER));
		chatBlockRepository.deleteByUserAndTargetUser(user, targetUser);
	}

	/**
	 * 차단 유무 확인
	 */
	public boolean isBlocked(User user, User targetUser){
		return chatBlockRepository.existsByUserAndTargetUser(user, targetUser);
	}

	/**
	 * 차단 목록 조회
	 */
	public List<UserBriefResponse> getBlockList(){
		return chatBlockRepository.findByUser(userService.getCurrentUser()).stream()
			.map(block -> block.getTargetUser())
			.map(UserBriefResponse::of)
			.collect(Collectors.toList());
	}
}

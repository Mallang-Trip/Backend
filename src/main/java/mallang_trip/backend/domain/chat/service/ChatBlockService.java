package mallang_trip.backend.domain.chat.service;

import static mallang_trip.backend.domain.user.exception.UserExceptionStatus.CANNOT_FOUND_USER;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.user.service.CurrentUserService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.user.dto.UserBriefResponse;
import mallang_trip.backend.domain.chat.entity.ChatBlock;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.chat.repository.ChatBlockRepository;
import mallang_trip.backend.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatBlockService {

	private final CurrentUserService currentUserService;
	private final ChatBlockRepository chatBlockRepository;
	private final UserRepository userRepository;

	/**
	 * 차단하기
	 */
	public void save(Long targetUserId){
		User user = currentUserService.getCurrentUser();
		User targetUser = userRepository.findById(targetUserId)
				.orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
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
		User user = currentUserService.getCurrentUser();
		User targetUser = userRepository.findById(targetUserId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
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
		return chatBlockRepository.findByUser(currentUserService.getCurrentUser()).stream()
			.map(block -> block.getTargetUser())
			.map(UserBriefResponse::of)
			.collect(Collectors.toList());
	}
}

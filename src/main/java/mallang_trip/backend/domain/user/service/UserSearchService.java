package mallang_trip.backend.domain.user.service;

import static mallang_trip.backend.domain.user.constant.Role.ROLE_ADMIN;
import static mallang_trip.backend.domain.user.exception.UserExceptionStatus.CANNOT_FOUND_USER;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.user.dto.UserInfoForAdminResponse;
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
	private final CurrentUserService currentUserService;
	private final SuspensionService suspensionService;
	private final ChatBlockService chatBlockService;

	/**
	 * 유저 검색 by nickname
	 */
	public List<UserBriefResponse> findByNickname(String nickname) {
		User currentUser = currentUserService.getCurrentUser();
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
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
		return UserBriefResponse.of(user);
	}

	/**
	 * (관리자) 회원 정보 목록 조회
	 */
	public List<UserInfoForAdminResponse> getUserList(String nicknameOrId){
		if(nicknameOrId == null){
			return userRepository.findAll().stream()
				.map(user-> UserInfoForAdminResponse.of(user,suspensionService.getSuspensionDuration(user)))
				.collect(Collectors.toList());
		}
		return userRepository.findByNicknameContainingIgnoreCaseOrLoginIdContainingIgnoreCase(nicknameOrId,nicknameOrId).stream()
			.map(user-> UserInfoForAdminResponse.of(user,suspensionService.getSuspensionDuration(user)))
			.collect(Collectors.toList());
	}

	/**
	 * (관리자) 관리자 권한 회원 목록 조회
	 *
	 */
	public List<UserInfoForAdminResponse> getAdminList(){
		return userRepository.findByRole(ROLE_ADMIN).stream()
			.map(user-> UserInfoForAdminResponse.of(user,0))
			.collect(Collectors.toList());
	}
}

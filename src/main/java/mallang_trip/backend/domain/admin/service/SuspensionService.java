package mallang_trip.backend.domain.admin.service;

import static mallang_trip.backend.domain.notification.constant.NotificationType.SUSPEND;
import static mallang_trip.backend.domain.admin.constant.SuspensionStatus.CANCELED;
import static mallang_trip.backend.domain.admin.constant.SuspensionStatus.EXPIRED;
import static mallang_trip.backend.domain.admin.constant.SuspensionStatus.SUSPENDING;
import static mallang_trip.backend.domain.user.exception.UserExceptionStatus.CANNOT_FOUND_USER;
import static mallang_trip.backend.global.io.BaseResponseStatus.Not_Found;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.admin.entity.Report;
import mallang_trip.backend.domain.admin.exception.AdminExceptionStatus;
import mallang_trip.backend.domain.admin.repository.ReportRepository;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.admin.dto.SuspendingUserResponse;
import mallang_trip.backend.domain.admin.dto.SuspensionRequest;
import mallang_trip.backend.domain.admin.entity.Suspension;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.admin.repository.SuspensionRepository;
import mallang_trip.backend.domain.user.repository.UserRepository;
import mallang_trip.backend.domain.notification.service.NotificationService;
import mallang_trip.backend.domain.chat.service.ChatRoomService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SuspensionService {

	private final UserRepository userRepository;
	private final SuspensionRepository suspensionRepository;
	private final NotificationService notificationService;
	private final ChatRoomService chatRoomService;

	private final ReportRepository reportRepository;

	/**
	 * 유저 정지
	 */
	public void suspend(Long userId, SuspensionRequest request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));

		Report report = reportRepository.findById(request.getReportId())
			.orElseThrow(() -> new BaseException(Not_Found));
		if(isSuspending(user)){
			cancelSuspension(user);
		}
		suspensionRepository.save(Suspension.builder()
			.report(report)
			.user(user)
			.content(request.getContent())
			.duration(request.getDuration())
			.build());
		chatRoomService.leaveAllChatExceptMyParty(user);
		notifySuspend(user, request.getContent(), request.getDuration());
	}

	/**
	 * 정지 중인 유저인지 확인
	 */
	public Boolean isSuspending(User user) {
		return suspensionRepository.existsByUserAndStatus(user, SUSPENDING);
	}

	/**
	 * 유저의 정지 일 수 확인
	 */
	public Integer getSuspensionDuration(User user) {
		return suspensionRepository.findByUserAndStatus(user, SUSPENDING)
			.map(Suspension::getDuration)
			.orElse(null);
	}

	/**
	 * 정지 알림 전송
	 */
	private void notifySuspend(User user, String reason, Integer duration) {
		String content = new StringBuilder()
				.append(user.getNickname())
				.append("님은 ")
				.append(reason)
				.append("로 인하여 ")
				.append(duration == -1 ? duration + "일" : "영구")
				.append(" 정지 처리되었습니다. 이의 제기를 하시려면 고객센터를 통해서 내용을 전달해주세요. 허위 사실을 기재할 경우 제재가 추가될 수 있습니다.")
			//.append("14일 전까지 이의제기가 가능하며 이의 제기를 원하시면 여기를 눌러주세요.")
			.toString();
		notificationService.create(user, content, SUSPEND, null);
	}

	/**
	 * 정지 취소
	 */
	public void cancelSuspension(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_USER));
		cancelSuspension(user);
	}

	private void cancelSuspension(User user){
		suspensionRepository.findByUserAndStatus(user, SUSPENDING)
			.ifPresent(suspension -> suspension.setStatus(CANCELED));
	}

	/**
	 * 정지 기간 만료 처리 (매일 0시 0분 5초 실행)
	 */
	@Scheduled(cron = "5 0 0 * * *")
	public void expireSuspension() {
		suspensionRepository.findByStatus(SUSPENDING).stream()
			.filter(suspension -> isSuspensionExpired(suspension))
			.forEach(suspension -> suspension.setStatus(EXPIRED));
	}

	/**
	 * 정지 기간이 지났는지 확인
	 */
	public boolean isSuspensionExpired(Suspension suspension){
		if(suspension.getDuration() == -1){
			return false;
		}
		LocalDate currentDate = LocalDate.now();
		LocalDate expirationDate = LocalDate.from(
			suspension.getCreatedAt().plusDays(suspension.getDuration()));
		return currentDate.isAfter(expirationDate);
	}

	/**
	 * 정지중인 유저 목록 조회
	 */
	public List<SuspendingUserResponse> getSuspendingUsers() {
		return suspensionRepository.findByStatus(SUSPENDING).stream()
			.map(SuspendingUserResponse::of)
			.collect(Collectors.toList());
	}

	/**
	 * 현재 유저가 정지된 사용자일 때 exception throw
	 */
	public void checkSuspension(User user){
		if(isSuspending(user)){
			throw new BaseException(AdminExceptionStatus.SUSPENDING);
		}
	}
}

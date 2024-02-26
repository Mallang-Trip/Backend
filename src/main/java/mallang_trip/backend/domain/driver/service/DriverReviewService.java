package mallang_trip.backend.domain.driver.service;

import static mallang_trip.backend.domain.driver.constant.DriverStatus.ACCEPTED;
import static mallang_trip.backend.domain.driver.exception.DriverExceptionStatus.CANNOT_FOUND_DRIVER;
import static mallang_trip.backend.domain.user.constant.Role.ROLE_ADMIN;
import static mallang_trip.backend.global.io.BaseResponseStatus.Conflict;
import static mallang_trip.backend.global.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.global.io.BaseResponseStatus.Not_Found;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.driver.repository.DriverRepository;
import mallang_trip.backend.domain.driver.repository.DriverReviewRepository;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.driver.dto.DriverReviewRequest;
import mallang_trip.backend.domain.driver.dto.DriverReviewResponse;
import mallang_trip.backend.domain.driver.entity.Driver;
import mallang_trip.backend.domain.driver.entity.DriverReview;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.admin.service.SuspensionService;
import mallang_trip.backend.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DriverReviewService {

	private final UserService userService;
	private final SuspensionService suspensionService;
	private final DriverRepository driverRepository;
	private final DriverReviewRepository driverReviewRepository;
	private final DriverNotificationService driverNotificationService;

	/**
	 * 드라이버 리뷰 등록
	 */
	public void create(Long driverId, DriverReviewRequest request) {
		Driver driver = driverRepository.findByIdAndStatus(driverId, ACCEPTED)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_DRIVER));
		User currentUser = userService.getCurrentUser();
		// 정지된 사용자인지 확인
		suspensionService.checkSuspension(currentUser);
		// 1인 1리뷰 확인
		if (driverReviewRepository.existsByDriverAndUser(driver, currentUser)) {
			throw new BaseException(Conflict);
		}
		// 저장
		DriverReview review = driverReviewRepository.save(DriverReview.builder()
			.driver(driver)
			.user(currentUser)
			.rate(request.getRate())
			.content(request.getContent())
			.images(request.getImages())
			.build());
		// 새 리뷰 알림 전송
		driverNotificationService.newReview(review);
	}

	/**
	 * 드라이버 리뷰 수정
	 */
	public void change(Long reviewId, DriverReviewRequest request) {
		DriverReview review = driverReviewRepository.findById(reviewId)
			.orElseThrow(() -> new BaseException(Not_Found));
		User currentUser = userService.getCurrentUser();
		// 정지된 사용자일 경우
		suspensionService.checkSuspension(currentUser);
		// 작성자가 아닐 경우
		if (currentUser.equals(review.getUser())) {
			throw new BaseException(Forbidden);
		}
		review.change(request.getRate(), request.getContent(), request.getImages());
	}

	/**
	 * 드라이버 리뷰 삭제
	 */
	public void delete(Long reviewId) {
		DriverReview review = driverReviewRepository.findById(reviewId)
			.orElseThrow(() -> new BaseException(Not_Found));
		// 관리자나 작성자가 아닐 경우 403
		User CurrentUser = userService.getCurrentUser();
		if (!CurrentUser.getRole().equals(ROLE_ADMIN) && !CurrentUser.equals(review.getUser())) {
			throw new BaseException(Forbidden);
		}
		driverReviewRepository.delete(review);
	}

	/**
	 * 드라이버 리뷰 조회
	 */
	public List<DriverReviewResponse> get(Driver driver){
		return driverReviewRepository
			.findAllByDriverOrderByUpdatedAtDesc(driver)
			.stream()
			.map(DriverReviewResponse::of)
			.collect(Collectors.toList());
	}

	/**
	 * 리뷰 평균 평점 조회
	 */
	public Double getAvgRate(Driver driver){
		return driverReviewRepository.getAvgRating(driver);
	}
}

package mallang_trip.backend.service.destination;

import static mallang_trip.backend.constant.Role.ROLE_ADMIN;
import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_DESTINATION;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Conflict;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.destination.DestinationReviewRequest;
import mallang_trip.backend.domain.dto.destination.DestinationReviewResponse;
import mallang_trip.backend.domain.entity.destination.Destination;
import mallang_trip.backend.domain.entity.destination.DestinationReview;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.destination.DestinationRepository;
import mallang_trip.backend.repository.destination.DestinationReviewRepository;
import mallang_trip.backend.service.admin.SuspensionService;
import mallang_trip.backend.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DestinationReviewService {

	private final UserService userService;
	private final SuspensionService suspensionService;
	private final DestinationRepository destinationRepository;
	private final DestinationReviewRepository destinationReviewRepository;

	/**
	 * 여행지 리뷰 추가
	 */
	public void create(Long destinationId, DestinationReviewRequest request) {
		Destination destination = destinationRepository.findByIdAndDeleted(destinationId, false)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_DESTINATION));
		User currentUser = userService.getCurrentUser();
		// 정지된 유저인지 CHECK
		suspensionService.checkSuspension(currentUser);
		// 1인 1리뷰 CHECK
		if (destinationReviewRepository.existsByDestinationAndUser(destination, currentUser)) {
			throw new BaseException(Conflict);
		}
		destinationReviewRepository.save(DestinationReview.builder()
			.destination(destination)
			.user(currentUser)
			.rate(request.getRate())
			.content(request.getContent())
			.images(request.getImages())
			.build());
	}

	/**
	 * 여행지 리뷰 수정
	 */
	public void change(Long reviewId, DestinationReviewRequest request) {
		DestinationReview review = destinationReviewRepository.findById(reviewId)
			.orElseThrow(() -> new BaseException(Not_Found));
		User currentUser = userService.getCurrentUser();
		// 정지된 사용자인지 CHECK
		suspensionService.checkSuspension(currentUser);
		// 작성자가 아닐 경우
		if (!currentUser.equals(review.getUser())) {
			throw new BaseException(Forbidden);
		}
		review.change(request);
	}

	/**
	 * 여행지 리뷰 삭제
	 */
	public void delete(Long reviewId) {
		DestinationReview review = destinationReviewRepository.findById(reviewId)
			.orElseThrow(() -> new BaseException(Not_Found));
		User currentUser = userService.getCurrentUser();
		// 관리자나 작성자가 아닐 경우
		if (!currentUser.getRole().equals(ROLE_ADMIN) && !currentUser.equals(review.getUser())) {
			throw new BaseException(Forbidden);
		}
		destinationReviewRepository.delete(review);
	}

	/**
	 * 여행지 리뷰 조회
	 */
	public List<DestinationReviewResponse> get(Destination destination) {
		return destinationReviewRepository.findAllByDestinationOrderByUpdatedAtDesc(destination)
			.stream()
			.map(DestinationReviewResponse::of)
			.collect(Collectors.toList());
	}

	/**
	 * 평균 평점 조회
	 */
	public Double getAvgRating(Destination destination){
		return destinationReviewRepository.getAvgRating(destination);
	}
}

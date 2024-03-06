package mallang_trip.backend.domain.destination.service;

import static mallang_trip.backend.domain.destination.exception.DestinationExceptionStatus.CANNOT_FOUND_DESTINATION;
import static mallang_trip.backend.domain.user.constant.Role.ROLE_ADMIN;
import static mallang_trip.backend.global.io.BaseResponseStatus.Conflict;
import static mallang_trip.backend.global.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.global.io.BaseResponseStatus.Not_Found;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.destination.repository.DestinationRepository;
import mallang_trip.backend.domain.destination.repository.DestinationReviewRepository;
import mallang_trip.backend.domain.user.service.CurrentUserService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.destination.dto.DestinationReviewRequest;
import mallang_trip.backend.domain.destination.dto.DestinationReviewResponse;
import mallang_trip.backend.domain.destination.entity.Destination;
import mallang_trip.backend.domain.destination.entity.DestinationReview;
import mallang_trip.backend.domain.user.entity.User;
import mallang_trip.backend.domain.admin.service.SuspensionService;
import mallang_trip.backend.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DestinationReviewService {

	private final CurrentUserService currentUserService;
	private final SuspensionService suspensionService;
	private final DestinationRepository destinationRepository;
	private final DestinationReviewRepository destinationReviewRepository;

	/**
	 * 여행지 리뷰 추가
	 */
	public void create(Long destinationId, DestinationReviewRequest request) {
		Destination destination = destinationRepository.findByIdAndDeleted(destinationId, false)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_DESTINATION));
		User currentUser = currentUserService.getCurrentUser();
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
		User currentUser = currentUserService.getCurrentUser();
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
		User currentUser = currentUserService.getCurrentUser();
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

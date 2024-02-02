package mallang_trip.backend.service;

import static mallang_trip.backend.constant.DestinationType.BY_ADMIN;
import static mallang_trip.backend.constant.DestinationType.BY_USER;
import static mallang_trip.backend.constant.Role.ROLE_ADMIN;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Conflict;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Forbidden;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;
import static mallang_trip.backend.controller.io.BaseResponseStatus.SUSPENDING;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Unauthorized;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.constant.DestinationType;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.destination.DestinationBriefResponse;
import mallang_trip.backend.domain.dto.destination.DestinationDetailsResponse;
import mallang_trip.backend.domain.dto.destination.DestinationIdResponse;
import mallang_trip.backend.domain.dto.destination.DestinationMarkerResponse;
import mallang_trip.backend.domain.dto.destination.DestinationRequest;
import mallang_trip.backend.domain.dto.destination.DestinationReviewRequest;
import mallang_trip.backend.domain.dto.destination.DestinationReviewResponse;
import mallang_trip.backend.domain.entity.destination.Destination;
import mallang_trip.backend.domain.entity.destination.DestinationDibs;
import mallang_trip.backend.domain.entity.destination.DestinationReview;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.destination.DestinationDibsRepository;
import mallang_trip.backend.repository.destination.DestinationRepository;
import mallang_trip.backend.repository.destination.DestinationReviewRepository;
import mallang_trip.backend.service.admin.SuspensionService;
import mallang_trip.backend.service.user.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DestinationService {

	private final DestinationRepository destinationRepository;
	private final DestinationReviewRepository destinationReviewRepository;
	private final DestinationDibsRepository destinationDibsRepository;
	private final UserService userService;
	private final SuspensionService suspensionService;

	/** 여행지 추가 */
	public DestinationIdResponse createDestination(DestinationRequest request,
		DestinationType type) {
		Destination destination = request.toDestination();
		destination.setType(type);
		destinationRepository.save(destination);

		return DestinationIdResponse.builder()
			.destinationId(destination.getId())
			.build();
	}

	/** 여행지 삭제 */
	public void deleteDestination(Long destinationId) {
		Destination destination = destinationRepository.findByIdAndDeleted(destinationId, false)
			.orElseThrow(() -> new BaseException(Not_Found));
		destination.setDeleted(true);
	}

	/** 여행지 키워드 검색 */
	public List<DestinationBriefResponse> searchDestination(String keyword) {
		List<Destination> destinations = destinationRepository.searchByKeyword(keyword);
		return destinations.stream()
			.map(destination -> DestinationBriefResponse.of(destination,
				checkDestinationDibs(destination),
				destinationReviewRepository.getAvgRating(destination)))
			.collect(Collectors.toList());
	}

	/** 전체 지도 마커 조회 */
	public List<DestinationMarkerResponse> getDestinationMarkers() {
		return destinationRepository.findByTypeAndDeleted(BY_ADMIN, false).stream()
			.map(DestinationMarkerResponse::of)
			.collect(Collectors.toList());
	}

	/** 여행지 수정 */
	public void changeDestination(Long destinationId, DestinationRequest request) {
		Destination destination = destinationRepository.findByIdAndDeleted(destinationId, false)
			.orElseThrow(() -> new BaseException(Not_Found));
		destination.setName(request.getName());
		destination.setAddress(request.getAddress());
		destination.setLon(request.getLon());
		destination.setLat(request.getLat());
		destination.setContent(request.getContent());
		destination.setImages(request.getImages());
	}

	/** 여행지 상세 조회 */
	public DestinationDetailsResponse getDestinationDetails(Long destinationId) {
		Destination destination = destinationRepository.findByIdAndDeleted(destinationId, false)
			.orElseThrow(() -> new BaseException(Not_Found));
		if (destination.getType().equals(BY_USER)) {
			throw new BaseException(Not_Found);
		}
		// 조회수 ++
		destination.setViews(destination.getViews() + 1);

		List<DestinationReviewResponse> reviewResponses = destinationReviewRepository.findAllByDestinationOrderByUpdatedAtDesc(
				destination).stream()
			.map(DestinationReviewResponse::of)
			.collect(Collectors.toList());

		return DestinationDetailsResponse.builder()
			.destinationId(destination.getId())
			.name(destination.getName())
			.address(destination.getAddress())
			.lon(destination.getLon())
			.lat(destination.getLat())
			.content(destination.getContent())
			.images(destination.getImages())
			.views(destination.getViews())
			.reviews(reviewResponses)
			.avgRate(destinationReviewRepository.getAvgRating(destination))
			.dibs(checkDestinationDibs(destination))
			.build();
	}

	/** 여행지 리뷰 추가 */
	public void createDestinationReview(Long destinationId, DestinationReviewRequest request) {
		Destination destination = destinationRepository.findByIdAndDeleted(destinationId, false)
			.orElseThrow(() -> new BaseException(Not_Found));
		User user = userService.getCurrentUser();
		if(suspensionService.isSuspending(user)){
			throw new BaseException(SUSPENDING);
		}
		// 1인 1리뷰 CHECK
		if (destinationReviewRepository.existsByDestinationAndUser(destination, user)) {
			throw new BaseException(Conflict);
		}
		destinationReviewRepository.save(DestinationReview.builder()
			.destination(destination)
			.user(user)
			.rate(request.getRate())
			.content(request.getContent())
			.images(request.getImages())
			.build());
	}

	/** 여행지 리뷰 수정 */
	public void changeDestinationReview(Long reviewId, DestinationReviewRequest request) {
		DestinationReview review = destinationReviewRepository.findById(reviewId)
			.orElseThrow(() -> new BaseException(Not_Found));
		User user = userService.getCurrentUser();
		if(suspensionService.isSuspending(user)){
			throw new BaseException(SUSPENDING);
		}
		// 작성자가 아닐 경우
		if (!user.equals(review.getUser())) {
			throw new BaseException(Forbidden);
		}
		review.setRate(request.getRate());
		review.setContent(request.getContent());
		review.setImages(request.getImages());
	}

	/** 여행지 리뷰 삭제 */
	public void deleteDestinationReview(Long reviewId) {
		DestinationReview review = destinationReviewRepository.findById(reviewId)
			.orElseThrow(() -> new BaseException(Not_Found));
		// 작성자가 아닐 경우
		User user = userService.getCurrentUser();
		if (!user.getRole().equals(ROLE_ADMIN) && !user.equals(review.getUser())) {
			throw new BaseException(Forbidden);
		}
		destinationReviewRepository.delete(review);
	}

	/** 여행지 찜하기 */
	public void createDestinationDibs(Long destinationId) {
		Destination destination = destinationRepository.findById(destinationId)
			.orElseThrow(() -> new BaseException(Not_Found));
		// 이미 찜한 경우
		if (checkDestinationDibs(destination)) {
			return;
		}
		destinationDibsRepository.save(DestinationDibs.builder()
			.destination(destination)
			.user(userService.getCurrentUser())
			.build());
	}

	/** 여행지 찜 취소 */
	public void deleteDestinationDibs(Long destinationId) {
		Destination destination = destinationRepository.findById(destinationId)
			.orElseThrow(() -> new BaseException(Not_Found));
		// 찜한 여행지 아닐 때
		if (!checkDestinationDibs(destination)) {
			return;
		}
		destinationDibsRepository.deleteByDestinationAndUser(destination,
			userService.getCurrentUser());
	}

	/** 현재 유저 찜한 여행지 조회 */
	public List<DestinationBriefResponse> getMyDestinationDibs() {
		return destinationDibsRepository.findAllByUserOrderByUpdatedAtDesc(userService.getCurrentUser()).stream()
			.filter(dib -> !dib.getDestination().getDeleted()) // 삭제된 여행지 제외
			.map(dib -> DestinationBriefResponse.of(dib.getDestination(), true,
				destinationReviewRepository.getAvgRating(dib.getDestination())))
			.collect(Collectors.toList());
	}

	/** 현재 유저 여행지 찜 여부 확인 */
	private boolean checkDestinationDibs(Destination destination) {
		User user = userService.getCurrentUser();
		if (user == null) {
			return false;
		}
		return destinationDibsRepository.existsByDestinationAndUser(destination, user);
	}
}

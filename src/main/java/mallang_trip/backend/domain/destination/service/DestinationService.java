package mallang_trip.backend.domain.destination.service;

import static mallang_trip.backend.domain.destination.constant.DestinationType.BY_ADMIN;
import static mallang_trip.backend.domain.destination.exception.DestinationExceptionStatus.CANNOT_FOUND_DESTINATION;
import static mallang_trip.backend.global.io.BaseResponseStatus.Not_Found;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.destination.constant.DestinationType;
import mallang_trip.backend.domain.destination.repository.DestinationRepository;
import mallang_trip.backend.domain.user.service.CurrentUserService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.domain.destination.dto.DestinationBriefResponse;
import mallang_trip.backend.domain.destination.dto.DestinationDetailsResponse;
import mallang_trip.backend.domain.destination.dto.DestinationIdResponse;
import mallang_trip.backend.domain.destination.dto.DestinationMarkerResponse;
import mallang_trip.backend.domain.destination.dto.DestinationRequest;
import mallang_trip.backend.domain.destination.entity.Destination;
import mallang_trip.backend.domain.user.entity.User;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DestinationService {

	private final CurrentUserService currentUserService;
	private final DestinationReviewService destinationReviewService;
	private final DestinationDibsService destinationDibsService;
	private final DestinationRepository destinationRepository;

	/**
	 * 여행지 추가
	 */
	public DestinationIdResponse create(DestinationRequest request, DestinationType type) {
		Destination destination = request.toDestination(type);
		destinationRepository.save(destination);
		return DestinationIdResponse.builder()
			.destinationId(destination.getId())
			.build();
	}

	/**
	 * 여행지 삭제
	 */
	@CacheEvict(value = "destination", key = "#destinationId")
	public void delete(Long destinationId) {
		Destination destination = destinationRepository.findByIdAndDeleted(destinationId, false)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_DESTINATION));
		destinationRepository.delete(destination);
	}

	/**
	 * 여행지 키워드 검색
	 */
	public List<DestinationBriefResponse> search(String keyword) {
		List<Destination> destinations = destinationRepository.searchByKeyword(keyword);
		User currentUser = currentUserService.getCurrentUser();
		return destinations.stream()
			.map(destination -> DestinationBriefResponse.of(destination,
				destinationDibsService.checkDestinationDibs(currentUser, destination),
				destinationReviewService.getAvgRating(destination)))
			.collect(Collectors.toList());
	}

	/**
	 * 전체 지도 마커 조회
	 */
	public List<DestinationMarkerResponse> getMarkers() {
		return destinationRepository.findByTypeAndDeleted(BY_ADMIN, false).stream()
			.map(DestinationMarkerResponse::of)
			.collect(Collectors.toList());
	}

	/**
	 * 여행지 수정
	 */
	@CacheEvict(value = "destination", key = "#destinationId")
	public void change(Long destinationId, DestinationRequest request) {
		Destination destination = destinationRepository.findByIdAndDeleted(destinationId, false)
			.orElseThrow(() -> new BaseException(Not_Found));
		destination.change(request);
	}

	/**
	 * 여행지 상세 조회
	 */
	public DestinationDetailsResponse view(Long destinationId) {
		Destination destination = destinationRepository.findByIdAndDeleted(destinationId, false)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_DESTINATION));
		// 조회수 ++
		destination.increaseViews();
		return DestinationDetailsResponse.builder()
			.destinationId(destination.getId())
			.name(destination.getName())
			.address(destination.getAddress())
			.lon(destination.getLon())
			.lat(destination.getLat())
			.content(destination.getContent())
			.images(destination.getImages())
			.views(destination.getViews())
			.reviews(destinationReviewService.get(destination))
			.avgRate(destinationReviewService.getAvgRating(destination))
			.dibs(destinationDibsService.checkDestinationDibs(currentUserService.getCurrentUser(),
				destination))
			.build();
	}

	@Cacheable(value = "destination", key = "#destinationId")
	public Optional<Destination> getDestination(Long destinationId) {
		return destinationRepository.findById(destinationId);
	}
}

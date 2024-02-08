package mallang_trip.backend.service.destination;

import static mallang_trip.backend.controller.io.BaseResponseStatus.CANNOT_FOUND_DESTINATION;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.destination.DestinationBriefResponse;
import mallang_trip.backend.domain.entity.destination.Destination;
import mallang_trip.backend.domain.entity.destination.DestinationDibs;
import mallang_trip.backend.domain.entity.user.User;
import mallang_trip.backend.repository.destination.DestinationDibsRepository;
import mallang_trip.backend.repository.destination.DestinationRepository;
import mallang_trip.backend.repository.destination.DestinationReviewRepository;
import mallang_trip.backend.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DestinationDibsService {

	private final UserService userService;
	private final DestinationRepository destinationRepository;
	private final DestinationDibsRepository destinationDibsRepository;
	private final DestinationReviewRepository destinationReviewRepository;

	/**
	 * 여행지 찜하기
	 */
	public void create(Long destinationId) {
		Destination destination = destinationRepository.findById(destinationId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_DESTINATION));
		User currentUser = userService.getCurrentUser();
		// 이미 찜한 경우
		if (checkDestinationDibs(currentUser, destination)) {
			return;
		}
		destinationDibsRepository.save(DestinationDibs.builder()
			.destination(destination)
			.user(currentUser)
			.build());
	}

	/**
	 * 여행지 찜 취소
	 */
	public void delete(Long destinationId) {
		Destination destination = destinationRepository.findById(destinationId)
			.orElseThrow(() -> new BaseException(CANNOT_FOUND_DESTINATION));
		User currentUser = userService.getCurrentUser();
		// 찜한 여행지 아닐 때
		if (!checkDestinationDibs(currentUser, destination)) {
			return;
		}
		destinationDibsRepository.deleteByDestinationAndUser(destination, currentUser);
	}

	/**
	 * 찜한 여행지 조회
	 */
	public List<DestinationBriefResponse> getDestinations() {
		return destinationDibsRepository
			.findAllByUserOrderByUpdatedAtDesc(userService.getCurrentUser())
			.stream()
			.filter(dib -> !dib.getDestination().getDeleted()) // 삭제된 여행지 제외
			.map(dib -> DestinationBriefResponse.of(dib.getDestination(), true,
				destinationReviewRepository.getAvgRating(dib.getDestination())))
			.collect(Collectors.toList());
	}

	/**
	 * 여행지 찜 여부 확인
	 */
	public boolean checkDestinationDibs(User user, Destination destination) {
		if (user == null) {
			return false;
		}
		return destinationDibsRepository.existsByDestinationAndUser(destination, user);
	}
}

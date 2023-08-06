package mallang_trip.backend.service;

import static mallang_trip.backend.controller.io.BaseResponseStatus.Not_Found;
import static mallang_trip.backend.controller.io.BaseResponseStatus.Unauthorized;

import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.domain.dto.destination.DestinationBriefResponse;
import mallang_trip.backend.domain.dto.destination.DestinationDetailsResponse;
import mallang_trip.backend.domain.dto.destination.DestinationIdResponse;
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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DestinationService {

    private final DestinationRepository destinationRepository;
    private final DestinationReviewRepository destinationReviewRepository;
    private final DestinationDibsRepository destinationDibsRepository;
    private final UserService userService;

    // 여행지 추가 (관리자 권한 설정 필요)
    public DestinationIdResponse createDestination(DestinationRequest request) {
        Destination destination = destinationRepository.save(request.toDestination());
        return DestinationIdResponse.builder()
            .destinationId(destination.getId())
            .build();
    }

    // 여행지 삭제 (관리자 권한 설정 필요)
    public void deleteDestination(Long destinationId) {
        Destination destination = destinationRepository.findById(destinationId)
            .orElseThrow(() -> new BaseException(Not_Found));
        destinationRepository.delete(destination);
    }

    // 여행지 키워드 검색
    public List<DestinationBriefResponse> searchDestination(String keyword) {
        List<Destination> destinations = destinationRepository.searchByKeyword(keyword);
        List<DestinationBriefResponse> responses = new ArrayList<>();
        for (Destination destination : destinations) {
            responses.add(
                DestinationBriefResponse.of(destination, checkDestinationDibs(destination),
                    destinationReviewRepository.getAvgRating(destination)));
        }
        return responses;
    }

    // 여행지 수정 (관리자 권한 설정 필요)
    public void changeDestination(Long destinationId, DestinationRequest request) {
        Destination destination = destinationRepository.findById(destinationId)
            .orElseThrow(() -> new BaseException(Not_Found));
        destination.setName(request.getName());
        destination.setAddress(request.getAddress());
        destination.setContent(request.getContent());
        destination.setImages(request.getImages());
    }

    // 여행지 상세 조회
    public DestinationDetailsResponse getDestinationDetails(Long destinationId) {
        Destination destination = destinationRepository.findById(destinationId)
            .orElseThrow(() -> new BaseException(Not_Found));
        destination.setViews(destination.getViews() + 1);

        List<DestinationReviewResponse> reviewResponses = new ArrayList<>();
        List<DestinationReview> reviews = destinationReviewRepository.findAllByDestination(
            destination);
        for (DestinationReview review : reviews) {
            reviewResponses.add(DestinationReviewResponse.of(review));
        }

        return DestinationDetailsResponse.builder()
            .destinationId(destination.getId())
            .name(destination.getName())
            .address(destination.getAddress())
            .content(destination.getContent())
            .images(destination.getImages())
            .views(destination.getViews())
            .reviews(reviewResponses)
            .avgRate(destinationReviewRepository.getAvgRating(destination))
            .dibs(checkDestinationDibs(destination))
            .build();
    }

    // 여행지 리뷰 추가
    public void createDestinationReview(Long destinationId, DestinationReviewRequest request) {
        Destination destination = destinationRepository.findById(destinationId)
            .orElseThrow(() -> new BaseException(Not_Found));
        destinationReviewRepository.save(DestinationReview.builder()
            .destination(destination)
            .user(userService.getCurrentUser())
            .rate(request.getRate())
            .content(request.getContent())
            .build());
    }

    // 여행지 리뷰 수정
    public void changeDestinationReview(Long reviewId, DestinationReviewRequest request) {
        DestinationReview review = destinationReviewRepository.findById(reviewId)
            .orElseThrow(() -> new BaseException(Not_Found));
        if (userService.getCurrentUser().getId() != review.getUser().getId()) {
            throw new BaseException(Unauthorized);
        }
        review.setRate(request.getRate());
        review.setContent(request.getContent());
    }

    // 여행지 리뷰 삭제
    public void deleteDestinationReview(Long reviewId) {
        DestinationReview review = destinationReviewRepository.findById(reviewId)
            .orElseThrow(() -> new BaseException(Not_Found));
        if (userService.getCurrentUser().getId() != review.getUser().getId()) {
            throw new BaseException(Unauthorized);
        }
        destinationReviewRepository.delete(review);
    }

    // 여행지 찜
    public void createDestinationDibs(Long destinationId) {
        Destination destination = destinationRepository.findById(destinationId)
            .orElseThrow(() -> new BaseException(Not_Found));
        destinationDibsRepository.save(DestinationDibs.builder()
            .destination(destination)
            .user(userService.getCurrentUser())
            .build());
    }

    // 여행지 찜 취소
    public void deleteDestinationDibs(Long destinationId) {
        Destination destination = destinationRepository.findById(destinationId)
            .orElseThrow(() -> new BaseException(Not_Found));
        destinationDibsRepository.deleteByDestinationAndUser(destination,
            userService.getCurrentUser());
    }

    // 내가 찜한 여행지 조회
    public List<DestinationBriefResponse> getMyDestinationDibs() {
        List<Destination> destinations = new ArrayList<>();
        List<DestinationDibs> dibs = destinationDibsRepository.findAllByUser(
            userService.getCurrentUser());
        for (DestinationDibs dib : dibs) {
            destinations.add(dib.getDestination());
        }
        List<DestinationBriefResponse> responses = new ArrayList<>();
        for (Destination destination : destinations) {
            responses.add(
                DestinationBriefResponse.of(destination, true,
                    destinationReviewRepository.getAvgRating(destination)));
        }
        return responses;
    }

    // 여행지 찜 여부 확인
    private boolean checkDestinationDibs(Destination destination) {
        User user = userService.getCurrentUser();
        return destinationDibsRepository.existsByDestinationAndUser(destination, user);
    }
}

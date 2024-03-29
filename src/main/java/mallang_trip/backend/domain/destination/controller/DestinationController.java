package mallang_trip.backend.domain.destination.controller;

import static mallang_trip.backend.domain.destination.constant.DestinationType.BY_ADMIN;
import static mallang_trip.backend.domain.destination.constant.DestinationType.BY_USER;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.destination.service.DestinationDibsService;
import mallang_trip.backend.domain.destination.service.DestinationReviewService;
import mallang_trip.backend.domain.destination.service.DestinationService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import mallang_trip.backend.domain.destination.dto.DestinationBriefResponse;
import mallang_trip.backend.domain.destination.dto.DestinationDetailsResponse;
import mallang_trip.backend.domain.destination.dto.DestinationIdResponse;
import mallang_trip.backend.domain.destination.dto.DestinationMarkerResponse;
import mallang_trip.backend.domain.destination.dto.DestinationRequest;
import mallang_trip.backend.domain.destination.dto.DestinationReviewRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Destination API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/destination")
public class DestinationController {

	private final DestinationService destinationService;
	private final DestinationReviewService destinationReviewService;
	private final DestinationDibsService destinationDibsService;

	@PostMapping
	@ApiOperation(value = "여행지 추가(관리자)")
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<DestinationIdResponse> createDestinationByAdmin(
		@RequestBody @Valid DestinationRequest request)
		throws BaseException {
		return new BaseResponse<>(destinationService.create(request, BY_ADMIN));
	}

	@PostMapping("/by-user")
	@ApiOperation(value = "여행지 추가(시용자)")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<DestinationIdResponse> createDestinationByUser(
		@RequestBody @Valid DestinationRequest request)
		throws BaseException {
		return new BaseResponse<>(destinationService.create(request, BY_USER));
	}

	@DeleteMapping("/{destination_id}")
	@ApiOperation(value = "여행지 삭제(관리자)")
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<String> deleteDestination(@PathVariable(value = "destination_id") Long id)
		throws BaseException {
		destinationService.delete(id);
		return new BaseResponse<>("성공");
	}

	@GetMapping
	@ApiOperation(value = "여행지 키워드 검색")
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<List<DestinationBriefResponse>> searchDestinationsByKeyword(
		@RequestParam String keyword)
		throws BaseException {
		return new BaseResponse<>(destinationService.search(keyword));
	}

	@GetMapping("/map")
	@ApiOperation(value = "여행지 전체 마커 조회")
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<List<DestinationMarkerResponse>> getDestinationMarkers()
		throws BaseException {
		return new BaseResponse<>(destinationService.getMarkers());
	}

	@PutMapping("/{destination_id}")
	@ApiOperation(value = "여행지 수정(관리자)")
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<String> changeDestination(@PathVariable(value = "destination_id") Long id,
		@RequestBody @Valid DestinationRequest request)
		throws BaseException {
		destinationService.change(id, request);
		return new BaseResponse<>("성공");
	}

	@GetMapping("/{destination_id}")
	@ApiOperation(value = "여행지 상세 조회")
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<DestinationDetailsResponse> getDestinationDetails(
		@PathVariable(value = "destination_id") Long id)
		throws BaseException {
		return new BaseResponse<>(destinationService.view(id));
	}

	@PostMapping("/review/{destination_id}")
	@ApiOperation(value = "여행지 리뷰 등록")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> createDestinationReview(
		@PathVariable(value = "destination_id") Long id,
		@RequestBody @Valid DestinationReviewRequest request)
		throws BaseException {
		destinationReviewService.create(id, request);
		return new BaseResponse<>("성공");
	}

	@PutMapping("/review/{review_id}")
	@ApiOperation(value = "여행지 리뷰 수정")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> changeDestinationReview(@PathVariable(value = "review_id") Long id,
		@RequestBody @Valid DestinationReviewRequest request)
		throws BaseException {
		destinationReviewService.change(id, request);
		return new BaseResponse<>("성공");
	}

	@DeleteMapping("/review/{review_id}")
	@ApiOperation(value = "여행지 리뷰 삭제")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> deleteDestinationReview(@PathVariable(value = "review_id") Long id)
		throws BaseException {
		destinationReviewService.delete(id);
		return new BaseResponse<>("성공");
	}

	@PostMapping("/dibs/{destination_id}")
	@ApiOperation(value = "여행지 찜하기")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> createDestinationDibs(
		@PathVariable(value = "destination_id") Long id) throws BaseException {
		destinationDibsService.create(id);
		return new BaseResponse<>("성공");
	}

	@DeleteMapping("/dibs/{destination_id}")
	@ApiOperation(value = "여행지 찜하기 취소")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> deleteDestinationDibs(
		@PathVariable(value = "destination_id") Long id) throws BaseException {
		destinationDibsService.delete(id);
		return new BaseResponse<>("성공");
	}

	@GetMapping("/dibs")
	@ApiOperation(value = "내가 찜한 여행지 조회")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<List<DestinationBriefResponse>> getMyDestinationDibs()
		throws BaseException {
		return new BaseResponse<>(destinationDibsService.getDestinations());
	}
}

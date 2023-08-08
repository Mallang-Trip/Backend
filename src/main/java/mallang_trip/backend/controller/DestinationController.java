package mallang_trip.backend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponse;
import mallang_trip.backend.domain.dto.destination.DestinationBriefResponse;
import mallang_trip.backend.domain.dto.destination.DestinationDetailsResponse;
import mallang_trip.backend.domain.dto.destination.DestinationIdResponse;
import mallang_trip.backend.domain.dto.destination.DestinationRequest;
import mallang_trip.backend.domain.dto.destination.DestinationReviewRequest;
import mallang_trip.backend.service.DestinationService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 여행지 CRUD 권한, 중복 이슈
@Api(tags = "Destination API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/destination")
public class DestinationController {

    private final DestinationService destinationService;

    @PostMapping
    @ApiOperation(value = "여행지 추가")
    public BaseResponse<DestinationIdResponse> createDestination(
        @RequestBody DestinationRequest request)
        throws BaseException {
        return new BaseResponse<>(destinationService.createDestination(request));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "여행지 삭제")
    public BaseResponse<String> deleteDestination(@PathVariable Long id)
        throws BaseException {
        destinationService.deleteDestination(id);
        return new BaseResponse<>("성공");
    }

    @GetMapping
    @ApiOperation(value = "여행지 키워드 검색")
    public BaseResponse<List<DestinationBriefResponse>> searchDestination(
        @RequestParam String keyword)
        throws BaseException {
        return new BaseResponse<>(destinationService.searchDestination(keyword));
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "여행지 수정")
    public BaseResponse<String> changeDestination(@PathVariable Long id,
        @RequestBody DestinationRequest request)
        throws BaseException {
        destinationService.changeDestination(id, request);
        return new BaseResponse<>("성공");
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "여행지 상세 조회")
    public BaseResponse<DestinationDetailsResponse> getDestinationDetails(@PathVariable Long id)
        throws BaseException {
        return new BaseResponse<>(destinationService.getDestinationDetails(id));
    }

    @PostMapping("/review/{id}")
    @ApiOperation(value = "여행지 리뷰 등록")
    public BaseResponse<String> createDestinationReview(@PathVariable Long id,
        @RequestBody DestinationReviewRequest request)
        throws BaseException {
        destinationService.createDestinationReview(id, request);
        return new BaseResponse<>("성공");
    }

    @PutMapping("/review/{id}")
    @ApiOperation(value = "여행지 리뷰 수정")
    public BaseResponse<String> changeDestinationReview(@PathVariable Long id,
        @RequestBody DestinationReviewRequest request)
        throws BaseException {
        destinationService.changeDestinationReview(id, request);
        return new BaseResponse<>("성공");
    }

    @DeleteMapping("/review/{id}")
    @ApiOperation(value = "여행지 리뷰 삭제")
    public BaseResponse<String> deleteDestinationReview(@PathVariable Long id)
        throws BaseException {
        destinationService.deleteDestinationReview(id);
        return new BaseResponse<>("성공");
    }

    @PostMapping("/dibs/{id}")
    @ApiOperation(value = "여행지 찜하기")
    public BaseResponse<String> createDestinationDibs(@PathVariable Long id)
        throws BaseException {
        destinationService.createDestinationDibs(id);
        return new BaseResponse<>("성공");
    }

    @DeleteMapping("/dibs/{id}")
    @ApiOperation(value = "여행지 찜하기 취소")
    public BaseResponse<String> deleteDestinationDibs(@PathVariable Long id)
        throws BaseException {
        destinationService.deleteDestinationDibs(id);
        return new BaseResponse<>("성공");
    }

    @GetMapping("/dibs")
    @ApiOperation(value = "내가 찜한 여행지 조회")
    public BaseResponse<List<DestinationBriefResponse>> getMyDestinationDibs()
        throws BaseException {
        return new BaseResponse<>(destinationService.getMyDestinationDibs());
    }
}

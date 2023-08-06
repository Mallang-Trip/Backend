package mallang_trip.backend.controller.party;

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

@RestController
@RequiredArgsConstructor
@RequestMapping("/destination")
public class DestinationController {

    private final DestinationService destinationService;

    @PostMapping
    public BaseResponse<DestinationIdResponse> createDestination(
        @RequestBody DestinationRequest request)
        throws BaseException {
        return new BaseResponse<>(destinationService.createDestination(request));
    }

    @DeleteMapping("/{id}")
    public BaseResponse<String> deleteDestination(@PathVariable Long id)
        throws BaseException {
        destinationService.deleteDestination(id);
        return new BaseResponse<>("성공");
    }

    @GetMapping
    public BaseResponse<List<DestinationBriefResponse>> searchDestination(
        @RequestParam String keyword)
        throws BaseException {
        return new BaseResponse<>(destinationService.searchDestination(keyword));
    }

    @PutMapping("/{id}")
    public BaseResponse<String> changeDestination(@PathVariable Long id,
        @RequestBody DestinationRequest request)
        throws BaseException {
        destinationService.changeDestination(id, request);
        return new BaseResponse<>("성공");
    }

    @GetMapping("/{id}")
    public BaseResponse<DestinationDetailsResponse> getDestinationDetails(@PathVariable Long id)
        throws BaseException {
        return new BaseResponse<>(destinationService.getDestinationDetails(id));
    }

    @PostMapping("/review/{id}")
    public BaseResponse<String> createDestinationReview(@PathVariable Long id,
        @RequestBody DestinationReviewRequest request)
        throws BaseException {
        destinationService.createDestinationReview(id, request);
        return new BaseResponse<>("성공");
    }

    @PutMapping("/review/{id}")
    public BaseResponse<String> changeDestinationReview(@PathVariable Long id,
        @RequestBody DestinationReviewRequest request)
        throws BaseException {
        destinationService.changeDestinationReview(id, request);
        return new BaseResponse<>("성공");
    }

    @DeleteMapping("/review/{id}")
    public BaseResponse<String> deleteDestinationReview(@PathVariable Long id)
        throws BaseException {
        destinationService.deleteDestinationReview(id);
        return new BaseResponse<>("성공");
    }

    @PostMapping("/dibs/{id}")
    public BaseResponse<String> createDestinationDibs(@PathVariable Long id)
        throws BaseException {
        destinationService.createDestinationDibs(id);
        return new BaseResponse<>("성공");
    }

    @DeleteMapping("/dibs/{id}")
    public BaseResponse<String> deleteDestinationDibs(@PathVariable Long id)
        throws BaseException {
        destinationService.deleteDestinationDibs(id);
        return new BaseResponse<>("성공");
    }

    @GetMapping("/dibs")
    public BaseResponse<List<DestinationBriefResponse>> getMyDestinationDibs()
        throws BaseException {
        return new BaseResponse<>(destinationService.getMyDestinationDibs());
    }
}

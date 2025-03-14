package mallang_trip.backend.domain.driver.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.driver.service.DriverReviewService;
import mallang_trip.backend.domain.driver.service.DriverService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import mallang_trip.backend.domain.driver.dto.ChangeDriverProfileRequest;
import mallang_trip.backend.domain.driver.dto.DriverBriefResponse;
import mallang_trip.backend.domain.driver.dto.DriverDetailsResponse;
import mallang_trip.backend.domain.driver.dto.DriverRegistrationRequest;
import mallang_trip.backend.domain.driver.dto.DriverRegistrationResponse;
import mallang_trip.backend.domain.driver.dto.DriverReviewRequest;
import mallang_trip.backend.domain.driver.dto.MyDriverProfileResponse;
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

@Api(tags = "Driver API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/driver")
public class DriverController {

    private final DriverService driverService;
    private final DriverReviewService driverReviewService;

    @PostMapping("/apply")
    @ApiOperation(value = "드라이버 신청")
    @PreAuthorize("hasRole('ROLE_USER')") // 일반 사용자
    public BaseResponse<String> registerDriver(
        @RequestBody @Valid DriverRegistrationRequest request)
        throws BaseException {
        driverService.registerDriver(request);
        return new BaseResponse<>("성공");
    }

    @GetMapping("/apply")
    @ApiOperation(value = "내 드라이버 신청 현황 조회")
    @PreAuthorize("hasRole('ROLE_USER')") // 일반 사용자
    public BaseResponse<DriverRegistrationResponse> getMyDriverRegistration()
        throws BaseException {
        return new BaseResponse<>(driverService.getMyDriverRegistration());
    }

    @PutMapping("/apply")
    @ApiOperation(value = "드라이버 신청 정보 수정, 재신청")
    @PreAuthorize("hasRole('ROLE_USER')") // 일반 사용자
    public BaseResponse<String> changeRegistration(
        @RequestBody @Valid DriverRegistrationRequest request)
        throws BaseException {
        driverService.changeDriverRegistration(request);
        return new BaseResponse<>("성공");
    }

    @DeleteMapping("/apply")
    @ApiOperation(value = "드라이버 신청 취소")
    @PreAuthorize("hasRole('ROLE_USER')") // 일반 사용자
    public BaseResponse<String> changeRegistration()
        throws BaseException {
        driverService.cancelDriverRegistration();
        return new BaseResponse<>("성공");
    }

    @GetMapping("/accept")
    @ApiOperation(value = "(관리자)드라이버 등록 신청 목록 조회")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<List<DriverRegistrationResponse>> getDriverRegistrations()
        throws BaseException {
        return new BaseResponse<>(driverService.getDriverRegistrationList());
    }

    @PutMapping("/accept/{driver_id}")
    @ApiOperation(value = "(관리자)드라이버 등록 수락/거절")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<String> acceptDriver(@PathVariable(value = "driver_id") Long id,
        @RequestParam Boolean accept)
        throws BaseException {
        driverService.acceptDriverRegistration(id, accept);
        return new BaseResponse<>("성공");
    }

    @PutMapping("/my")
    @ApiOperation(value = "(드라이버) 내 프로필 정보 변경")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @PreAuthorize("hasRole('ROLE_DRIVER')") // 드라이버
    public BaseResponse<String> changeMyDriverProfile(
        @RequestBody @Valid ChangeDriverProfileRequest request)
        throws BaseException {
        driverService.changeProfile(request);
        return new BaseResponse<>("성공");
    }

    @GetMapping("/my")
    @ApiOperation(value = "(드라이버) 내 프로필 정보 조회")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @PreAuthorize("hasRole('ROLE_DRIVER')") // 드라이버
    public BaseResponse<MyDriverProfileResponse> getMyDriverProfile()
        throws BaseException {
        return new BaseResponse<>(driverService.getMyDriverProfile());
    }

    @PostMapping("/review/{review_id}")
    @ApiOperation(value = "드라이버 리뷰 등록")
    @PreAuthorize("hasRole('ROLE_USER')") // 일반 사용자
    public BaseResponse<String> createDriverReview(@PathVariable(value = "review_id") Long id,
        @RequestBody @Valid
            DriverReviewRequest request)
        throws BaseException {
        driverReviewService.create(id, request);
        return new BaseResponse<>("성공");
    }

    @PutMapping("/review/{review_id}")
    @ApiOperation(value = "드라이버 리뷰 수정")
    @PreAuthorize("hasRole('ROLE_USER')") // 일반 사용자
    public BaseResponse<String> changeDriverReview(@PathVariable(value = "review_id") Long id,
        @RequestBody @Valid
            DriverReviewRequest request)
        throws BaseException {
        driverReviewService.change(id, request);
        return new BaseResponse<>("성공");
    }

    @DeleteMapping("/review/{review_id}")
    @ApiOperation(value = "드라이버 리뷰 삭제")
    @PreAuthorize("isAuthenticated()") // 로그인 사용자
    public BaseResponse<String> deleteDriverReview(@PathVariable(value = "review_id") Long id)
        throws BaseException {
        driverReviewService.delete(id);
        return new BaseResponse<>("성공");
    }

    @GetMapping("/search")
    @ApiOperation(value = "지역, 인원, 날짜로 가능한 드라이버 조회")
    @PreAuthorize("permitAll()") // anyone
    public BaseResponse<List<DriverBriefResponse>> getDriversByRegion(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Integer headcount,
            @RequestParam(required = false) String startDate
    )
        throws BaseException {
        return new BaseResponse<>(driverService.getPossibleDriver(region, headcount, startDate));
    }

    @GetMapping("/{driver_id}")
    @ApiOperation(value = "드라이버 상세 조회")
    @PreAuthorize("permitAll()") // anyone
    public BaseResponse<DriverDetailsResponse> getDriverDetails(
        @PathVariable(value = "driver_id") Long id)
        throws BaseException {
        return new BaseResponse<>(driverService.getDriverDetails(id));
    }
}

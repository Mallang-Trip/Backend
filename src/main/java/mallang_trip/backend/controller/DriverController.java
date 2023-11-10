package mallang_trip.backend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponse;
import mallang_trip.backend.domain.dto.driver.ChangeBankAccountRequest;
import mallang_trip.backend.domain.dto.driver.ChangeDriverProfileRequest;
import mallang_trip.backend.domain.dto.driver.ChangePriceRequest;
import mallang_trip.backend.domain.dto.driver.ChangeVehicleRequest;
import mallang_trip.backend.domain.dto.driver.DriverBriefResponse;
import mallang_trip.backend.domain.dto.driver.DriverDetailsResponse;
import mallang_trip.backend.domain.dto.driver.DriverRegistrationRequest;
import mallang_trip.backend.domain.dto.driver.DriverRegistrationResponse;
import mallang_trip.backend.domain.dto.driver.DriverReviewRequest;
import mallang_trip.backend.domain.dto.driver.HolidayRequest;
import mallang_trip.backend.domain.dto.driver.HolidayResponse;
import mallang_trip.backend.domain.dto.driver.MyDriverProfileResponse;
import mallang_trip.backend.service.DriverService;
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

    @PostMapping("/apply")
    @ApiOperation(value = "드라이버 신청")
    public BaseResponse<String> registerDriver(@RequestBody DriverRegistrationRequest request)
        throws BaseException {
        driverService.registerDriver(request);
        return new BaseResponse<>("성공");
    }

    @GetMapping("/status")
    @ApiOperation(value = "내 드라이버 신청 현황 조회")
    public BaseResponse<DriverRegistrationResponse> getMyDriverRegistration()
        throws BaseException {
        return new BaseResponse<>(driverService.getMyDriverRegistration());
    }

    @PostMapping("/reapply")
    @ApiOperation(value = "드라이버 신청 정보 수정, 재신청")
    public BaseResponse<String> reapplyDriver(@RequestBody DriverRegistrationRequest request)
        throws BaseException {
        driverService.reapplyDriver(request);
        return new BaseResponse<>("성공");
    }

    @GetMapping("/accept")
    @ApiOperation(value = "(관리자)드라이버 등록 신청 목록 조회")
    public BaseResponse<List<DriverRegistrationResponse>> getDriverRegistrations()
        throws BaseException {
        return new BaseResponse<>(driverService.getDriverRegistrationList());
    }

    @PutMapping("/accept/{id}")
    @ApiOperation(value = "(관리자)드라이버 등록 수락/거절")
    public BaseResponse<String> acceptDriver(@PathVariable Long id, @RequestParam Boolean accept)
        throws BaseException {
        driverService.acceptDriverRegistration(id, accept);
        return new BaseResponse<>("성공");
    }

    @PutMapping("/my")
    @ApiOperation(value = "드라이버 내 프로필 정보 변경")
    public BaseResponse<String> changeMyDriverProfile(@RequestBody ChangeDriverProfileRequest request)
        throws BaseException {
        driverService.changeProfile(request);
        return new BaseResponse<>("성공");
    }

    @GetMapping("/my")
    @ApiOperation(value = "드라이버 내 프로필 정보 조회")
    public BaseResponse<MyDriverProfileResponse> getMyDriverProfile()
        throws BaseException {
        return new BaseResponse<>(driverService.getMyDriverProfile());
    }

    @PostMapping("/review/{id}")
    @ApiOperation(value = "드라이버 리뷰 등록")
    public BaseResponse<String> createDriverReview(@PathVariable Long id, @RequestBody
        DriverReviewRequest request)
        throws BaseException {
        driverService.createDriverReview(id, request);
        return new BaseResponse<>("성공");
    }

    @PutMapping("/review/{id}")
    @ApiOperation(value = "드라이버 리뷰 수정")
    public BaseResponse<String> changeDriverReview(@PathVariable Long id, @RequestBody
        DriverReviewRequest request)
        throws BaseException {
        driverService.changeDriverReview(id, request);
        return new BaseResponse<>("성공");
    }

    @DeleteMapping("/review/{id}")
    @ApiOperation(value = "드라이버 리뷰 삭제")
    public BaseResponse<String> deleteDriverReview(@PathVariable Long id)
        throws BaseException {
        driverService.deleteDriverReview(id);
        return new BaseResponse<>("성공");
    }

    @GetMapping("/list")
    @ApiOperation(value = "지역 별 드라이버 목록 조회")
    public BaseResponse<List<DriverBriefResponse>> getDriversByRegion(@RequestParam String region)
        throws BaseException {
        return new BaseResponse<>(driverService.getDriversByRegion(region));
    }

    @GetMapping("/search")
    @ApiOperation(value = "지역, 인원, 날짜로 가능한 드라이버 조회")
    public BaseResponse<List<DriverBriefResponse>> getDriversByRegion(@RequestParam String region,
        @RequestParam Integer headcount, @RequestParam String startDate)
        throws BaseException {
        return new BaseResponse<>(driverService.getPossibleDriver(region, headcount, startDate));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "드라이버 상세 조회")
    public BaseResponse<DriverDetailsResponse> getDriverDetails(@PathVariable Long id)
        throws BaseException {
        return new BaseResponse<>(driverService.getDriverDetails(id));
    }
}

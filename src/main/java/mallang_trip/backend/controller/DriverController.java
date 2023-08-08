package mallang_trip.backend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponse;
import mallang_trip.backend.domain.dto.driver.ChangeBankAccountRequest;
import mallang_trip.backend.domain.dto.driver.ChangePriceRequest;
import mallang_trip.backend.domain.dto.driver.ChangeVehicleRequest;
import mallang_trip.backend.domain.dto.driver.DriverPriceRequest;
import mallang_trip.backend.domain.dto.driver.DriverRegistrationRequest;
import mallang_trip.backend.domain.dto.driver.HolidayRequest;
import mallang_trip.backend.domain.dto.driver.HolidayResponse;
import mallang_trip.backend.domain.dto.driver.MyDriverProfileResponse;
import mallang_trip.backend.service.DriverService;
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

    @PostMapping
    @ApiOperation(value = "드라이버 등록")
    public BaseResponse<String> registerDriver(@RequestBody DriverRegistrationRequest request)
        throws BaseException {
        driverService.registerDriver(request);
        return new BaseResponse<>("성공");
    }

    @PostMapping("/reapply")
    @ApiOperation(value = "드라이버 재등록 or 등록신청 수정")
    public BaseResponse<String> reapplyDriver(@RequestBody DriverRegistrationRequest request)
        throws BaseException {
        driverService.reapplyDriver(request);
        return new BaseResponse<>("성공");
    }

    @PostMapping("/accept/{id}")
    @ApiOperation(value = "드라이버 등록 수락/거절")
    public BaseResponse<String> acceptDriver(@PathVariable Long id, @RequestParam Boolean accept)
        throws BaseException {
        driverService.acceptDriverRegistration(id, accept);
        return new BaseResponse<>("성공");
    }

    @PutMapping("/my/holiday/weekly")
    @ApiOperation(value = "드라이버 정기 휴일 설정")
    public BaseResponse<String> setWeeklyHoliday(@RequestBody HolidayRequest request)
        throws BaseException {
        driverService.setWeeklyHoliday(request);
        return new BaseResponse<>("성공");
    }

    @PutMapping("/my/holiday")
    @ApiOperation(value = "드라이버 휴일 설정")
    public BaseResponse<String> setHoliday(@RequestBody HolidayRequest request)
        throws BaseException {
        driverService.setHoliday(request);
        return new BaseResponse<>("성공");
    }

    @GetMapping("/holiday/weekly/{id}")
    @ApiOperation(value = "드라이버 정기 휴일 조회")
    public BaseResponse<HolidayResponse> getWeeklyHoliday(@PathVariable Long id)
        throws BaseException {
        return new BaseResponse<>(driverService.getWeeklyHoliday(id));
    }

    @GetMapping("/holiday/{id}")
    @ApiOperation(value = "드라이버 휴일 조회")
    public BaseResponse<HolidayResponse> getHoliday(@PathVariable Long id)
        throws BaseException {
        return new BaseResponse<>(driverService.getHoliday(id));
    }

    @PutMapping("/my/region/{region}")
    @ApiOperation(value = "드라이버 활동 지역 변경")
    public BaseResponse<String> setRegion(@PathVariable String region)
        throws BaseException {
        driverService.setRegion(region);
        return new BaseResponse<>("성공");
    }

    @PutMapping("/my/bank")
    @ApiOperation(value = "계좌 수정")
    public BaseResponse<String> changeBankAccount(@RequestBody ChangeBankAccountRequest request)
        throws BaseException {
        driverService.changeBankAccount(request);
        return new BaseResponse<>("성공");
    }

    @PutMapping("/my/vehicle")
    @ApiOperation(value = "차량 정보 수정")
    public BaseResponse<String> changeVehicle(@RequestBody ChangeVehicleRequest request)
        throws BaseException {
        driverService.changeVehicle(request);
        return new BaseResponse<>("성공");
    }

    @PutMapping("/my/price")
    @ApiOperation(value = "가격 설정")
    public BaseResponse<String> setPrice(@RequestBody ChangePriceRequest request)
        throws BaseException {
        driverService.setPrice(request);
        return new BaseResponse<>("성공");
    }

    @GetMapping("/my")
    @ApiOperation(value = "드라이버 내 프로필 정보 조회")
    public BaseResponse<MyDriverProfileResponse> getMyDriverProfile()
        throws BaseException {
        return new BaseResponse<>(driverService.getMyDriverProfile());
    }

/*    @GetMapping("/{id}")
    @ApiOperation(value = "드라이버 정보 조회")
    public BaseResponse<MyDriverProfileResponse> getDriverProfile(@PathVariable Long id)
        throws BaseException {
        return new BaseResponse<>(driverService.getMyDriverProfile());
    }*/
}

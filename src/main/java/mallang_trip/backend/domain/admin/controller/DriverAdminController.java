package mallang_trip.backend.domain.admin.controller;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.admin.service.DriverAdminService;
import mallang_trip.backend.domain.admin.service.UserAdminService;
import mallang_trip.backend.domain.course.dto.CourseDetailsResponse;
import mallang_trip.backend.domain.course.dto.CourseIdResponse;
import mallang_trip.backend.domain.course.dto.CourseRequest;
import mallang_trip.backend.domain.driver.dto.ChangeDriverProfileRequest;
import mallang_trip.backend.domain.driver.dto.DriverRegistrationResponse;
import mallang_trip.backend.domain.driver.dto.MyDriverProfileResponse;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "Driver Admin API")
@RestController
@RequiredArgsConstructor
public class DriverAdminController {

    private final DriverAdminService driverAdminService;

    /**
     * (관리자) 모든 드라이버 목록 조회
     *
     */
    @GetMapping("/admin/driver/apply")
    @ApiOperation(value = "(관리자) 모든 드라이버 목록 조회")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<List<DriverRegistrationResponse>> getDriverList() throws BaseException {
        return new BaseResponse<>(driverAdminService.getDriverList());
    }

    /**
     * (관리자) 드라이버 신청
     *
     */
    @PostMapping("/admin/driver/apply/{userId}")
    @ApiOperation(value = "(관리자) 드라이버 신청")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
            @ApiImplicitParam(name = "userId", value = "user_id", required = true, paramType = "path", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "region", value = "지역", required = true, paramType = "query", dataTypeClass = String.class)
    })
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
            @ApiResponse(code = 404, message = "해당 유저를 찾을 수 없습니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<String> registerDriver(@PathVariable(value = "userId") Long userId, @RequestParam String region) throws BaseException {
        driverAdminService.registerDriver(userId, region);
        return new BaseResponse<>("성공");
    }

    /**
     * (관리자) 드라이버 신청 취소
     *
     */
    @DeleteMapping("/admin/driver/apply/{driverId}")
    @ApiOperation(value = "(관리자) 드라이버 신청 취소")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
            @ApiImplicitParam(name = "driverId", value = "driver_id", required = true, paramType = "path", dataTypeClass = Long.class)
    })
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
            @ApiResponse(code = 404, message = "해당 드라이버를 찾을 수 없습니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<String> cancelDriverRegistration(@PathVariable(value = "driverId") Long driverId) throws BaseException {
        driverAdminService.cancelDriverRegistration(driverId);
        return new BaseResponse<>("성공");
    }

    /**
     * (관리자) 드라이버 프로필 조회
     *
     */
    @GetMapping("/admin/driver/my/{driverId}")
    @ApiOperation(value = "(관리자) 드라이버 프로필 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
            @ApiImplicitParam(name = "driverId", value = "driver_id", required = true, paramType = "path", dataTypeClass = Long.class)
    })
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
            @ApiResponse(code = 404, message = "해당 드라이버를 찾을 수 없습니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<MyDriverProfileResponse> getMyDriverProfile(@PathVariable(value = "driverId") Long driverId) throws BaseException {
        return new BaseResponse<>(driverAdminService.getDriverProfile(driverId));
    }

    /**
     * (관리자) 드라이버 프로필 수정
     *
     */
    @PutMapping("/admin/driver/my/{driverId}")
    @ApiOperation(value = "(관리자) 드라이버 프로필 수정")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
            @ApiImplicitParam(name = "driverId", value = "driver_id", required = true, paramType = "path", dataTypeClass = Long.class)
    })
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
            @ApiResponse(code = 404, message = "해당 드라이버를 찾을 수 없습니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<String> changeMyDriverProfile(@PathVariable(value = "driverId") Long driverId, @RequestBody @Valid ChangeDriverProfileRequest request) throws BaseException {
        driverAdminService.changeDriverProfile(driverId, request);
        return new BaseResponse<>("성공");
    }

    /**
     * (관리자) 드라이버 코스 생성
     *
     */
    @PostMapping("/admin/driver/course/{driverId}")
    @ApiOperation(value = "(관리자) 드라이버 코스 생성")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
            @ApiImplicitParam(name = "driverId", value = "driver_id", required = true, paramType = "path", dataTypeClass = Long.class)
    })
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
            @ApiResponse(code = 404, message = "해당 드라이버를 찾을 수 없습니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<CourseIdResponse> createCourse(@PathVariable(value = "driverId") Long driverId, @RequestBody @Valid CourseRequest request) throws BaseException {
        return new BaseResponse<>(CourseIdResponse.of(driverAdminService.createCourse(driverId, request)));
    }

    /**
     * (관리자) 드라이버 코스 조회
     *
     */
    @GetMapping("/admin/driver/course/{driverId}") // + query parameter courseId 추가
    @ApiOperation(value = "(관리자) 드라이버 코스 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
            @ApiImplicitParam(name = "driverId", value = "driver_id", required = true, paramType = "path", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "courseId", value = "course_id", required = true, paramType = "query", dataTypeClass = Long.class)
    })
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
            @ApiResponse(code = 404, message = "해당 드라이버를 찾을 수 없습니다."),
            @ApiResponse(code = 404, message = "해당 코스를 찾을 수 없습니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<CourseDetailsResponse> getCourse(@PathVariable(value = "driverId") Long driverId, @RequestParam Long courseId) throws BaseException {
        return new BaseResponse<>(driverAdminService.getCourseDetails(driverId, courseId));
    }



    /**
     * (관리자) 드라이버 코스 수정
     *
     */
    @PutMapping("/admin/driver/course/{driverId}")  // + query parameter courseId 추가
    @ApiOperation(value = "(관리자) 드라이버 코스 수정")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
            @ApiImplicitParam(name = "driverId", value = "driver_id", required = true, paramType = "path", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "courseId", value = "course_id", required = true, paramType = "query", dataTypeClass = Long.class)
    })
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
            @ApiResponse(code = 404, message = "해당 드라이버를 찾을 수 없습니다."),
            @ApiResponse(code = 404, message = "해당 코스를 찾을 수 없습니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<String> changeCourse(@PathVariable(value = "driverId") Long driverId, @RequestParam Long courseId, @RequestBody @Valid CourseRequest request) throws BaseException {
        driverAdminService.changeCourse(driverId, courseId, request);
        return new BaseResponse<>("성공");
    }


    /**
     * (관리자) 드라이버 코스 삭제
     *
     */
    @DeleteMapping("/admin/driver/course/{driverId}")  // + query parameter courseId 추가
    @ApiOperation(value = "(관리자) 드라이버 코스 삭제")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
            @ApiImplicitParam(name = "driverId", value = "driver_id", required = true, paramType = "path", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "courseId", value = "course_id", required = true, paramType = "query", dataTypeClass = Long.class)
    })
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
            @ApiResponse(code = 404, message = "해당 드라이버를 찾을 수 없습니다."),
            @ApiResponse(code = 404, message = "해당 코스를 찾을 수 없습니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<String> deleteCourse(@PathVariable(value = "driverId") Long driverId, @RequestParam Long courseId) throws BaseException {
        driverAdminService.deleteCourse(driverId, courseId);
        return new BaseResponse<>("성공");
    }
}

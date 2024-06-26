package mallang_trip.backend.domain.notification.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import mallang_trip.backend.domain.notification.service.NotificationService;
import mallang_trip.backend.domain.notification.dto.NotificationListResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Notification API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @ApiOperation(value = "내 알림 조회")
    @GetMapping
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @PreAuthorize("isAuthenticated()") // 로그인 사용자
    public BaseResponse<NotificationListResponse> get() throws BaseException {
        return new BaseResponse<>(notificationService.getNotifications());
    }

    @ApiOperation(value = "알림 확인 처리")
    @PutMapping("/{notification_id}")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @PreAuthorize("isAuthenticated()") // 로그인 사용자
    public BaseResponse<String> check(
        @PathVariable(value = "notification_id") Long notificationId) throws BaseException {
        notificationService.check(notificationId);
        return new BaseResponse<>("성공");
    }

    @ApiOperation(value = "알림 전체 확인 처리")
    @PutMapping("/all")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @PreAuthorize("isAuthenticated()") // 로그인 사용자
    public BaseResponse<String> checkAll() throws BaseException {
        notificationService.checkAll();
        return new BaseResponse<>("성공");
    }

    @ApiOperation(value = "알림 삭제")
    @DeleteMapping("/{notification_id}")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @PreAuthorize("isAuthenticated()") // 로그인 사용자
    public BaseResponse<String> delete(
        @PathVariable(value = "notification_id") Long notificationId) throws BaseException {
        notificationService.delete(notificationId);
        return new BaseResponse<>("성공");
    }
}

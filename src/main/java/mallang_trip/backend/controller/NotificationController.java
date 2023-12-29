package mallang_trip.backend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponse;
import mallang_trip.backend.domain.dto.notification.NotificationListResponse;
import mallang_trip.backend.service.NotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("isAuthenticated()") // 로그인 사용자
    public BaseResponse<NotificationListResponse> getNotifications() throws BaseException {
        return new BaseResponse<>(notificationService.getNotifications());
    }

    @ApiOperation(value = "알림 확인 처리")
    @PutMapping("/{notification_id}")
    @PreAuthorize("isAuthenticated()") // 로그인 사용자
    public BaseResponse<String> checkNotification(
        @PathVariable(value = "notification_id") Long notificationId) throws BaseException {
        notificationService.check(notificationId);
        return new BaseResponse<>("성공");
    }
}

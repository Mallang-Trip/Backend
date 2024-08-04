package mallang_trip.backend.domain.notification.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.notification.dto.FirebaseRequest;
import mallang_trip.backend.domain.notification.dto.FirebaseTest;
import mallang_trip.backend.domain.notification.dto.FirebaseUpdateDeleteRequest;
import mallang_trip.backend.domain.notification.service.FirebaseService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "Firebase API")
@RestController
@RequiredArgsConstructor
public class FirebaseController {

    private final FirebaseService firebaseService;

    @ApiOperation(value ="Firebase 토큰 삭제")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @DeleteMapping("/firebase")
    @PreAuthorize("isAuthenticated()")
    public BaseResponse<String> deleteFirebaseToken(@RequestBody @Valid FirebaseUpdateDeleteRequest request) throws BaseException {
        firebaseService.deleteToken(request);
        return new BaseResponse<>("성공");
    }

    @ApiOperation(value ="Firebase 토큰 추가")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @PutMapping("/firebase")
    @PreAuthorize("isAuthenticated()")
    public BaseResponse<String> updateFirebaseToken(@RequestBody @Valid FirebaseUpdateDeleteRequest request) throws BaseException {
        firebaseService.updateToken(request);
        return new BaseResponse<>("성공");
    }

    @ApiOperation(value ="Firebase Push 알림 테스트")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @PostMapping("/firebase/push")
    @PreAuthorize("isAuthenticated()")
    public BaseResponse<String> sendPushNotification(@RequestBody @Valid FirebaseTest request) throws BaseException {
        return new BaseResponse<>(firebaseService.sendPushMessageTest(request.getFirebaseToken(), request.getTitle(), request.getBody(), request.getUrl()));
    }

}

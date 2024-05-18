package mallang_trip.backend.domain.notification.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.notification.dto.FirebaseRequest;
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

    @ApiOperation(value ="Firebase 토큰 등록")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @PostMapping("/firebase")
    @PreAuthorize("isAuthenticated()")
    public BaseResponse<String> registerFirebaseToken(@RequestBody @Valid FirebaseRequest request) throws BaseException {
        firebaseService.saveToken(request.getFirebaseToken());
        return new BaseResponse<>("성공");
    }

    @ApiOperation(value ="Firebase 토큰 삭제")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @DeleteMapping("/firebase")
    @PreAuthorize("isAuthenticated()")
    public BaseResponse<String> deleteFirebaseToken() throws BaseException {
        firebaseService.deleteToken();
        return new BaseResponse<>("성공");
    }

    @ApiOperation(value ="Firebase 토큰 갱신")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @PutMapping("/firebase")
    @PreAuthorize("isAuthenticated()")
    public BaseResponse<String> updateFirebaseToken(@RequestBody @Valid FirebaseRequest request) throws BaseException {
        firebaseService.updateToken(request.getFirebaseToken());
        return new BaseResponse<>("성공");
    }
}

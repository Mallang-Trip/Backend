package mallang_trip.backend.domain.admin.controller;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.admin.dto.UserInfoForAdminResponse;
import mallang_trip.backend.domain.admin.service.UserAdminService;
import mallang_trip.backend.domain.admin.dto.GrantAdminRoleRequest;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "User Admin API")
@RestController
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;

    /**
     * (관리자) 회원 정보 목록 조회
     *
     */
    @GetMapping("/admin/user/list/{nicknameOrId}")
    @ApiOperation(value = "(관리자) 회원 정보 목록 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
            @ApiImplicitParam(name = "nicknameOrId", value = "닉네임 또는 아이디", required = false, paramType = "query", dataTypeClass = String.class)
    })
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<List<UserInfoForAdminResponse>> getUserList(@RequestParam(required = false) String nicknameOrId) throws BaseException {
        return new BaseResponse<>(userAdminService.getUserList(nicknameOrId));
    }

    /**
     * (관리자) 관리자 권한 회원 목록 조회
     *
     */
    @GetMapping("/admin/role")
    @ApiOperation(value = "(관리자) 관리자 권한 회원 목록 조회")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<List<UserInfoForAdminResponse>> getAdminList() throws BaseException {
        return new BaseResponse<>(userAdminService.getAdminList());
    }

    /**
     * (관리자) 회원 관리자 권한 부여
     *
     */
    @PostMapping("/admin/role")
    @ApiOperation(value = "(관리자) 회원 관리자 권한 부여")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
            @ApiResponse(code = 404, message = "해당 유저를 찾을 수 없습니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<String> grantAdminRole(@RequestBody @Valid GrantAdminRoleRequest request) throws BaseException {
        userAdminService.grantAdminRole(request);
        return new BaseResponse<>("성공");
    }

    /**
     * (관리자) 회원 관리자 권한 해제
     */
    @DeleteMapping("/admin/role/{userId}")
    @ApiOperation(value = "(관리자) 회원 관리자 권한 해제")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
            @ApiImplicitParam(name = "userId", value = "user_id", required = true, paramType = "path", dataTypeClass = Long.class)
    })
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
            @ApiResponse(code = 404, message = "해당 유저를 찾을 수 없습니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<String> revokeAdminRole(@PathVariable(value = "userId") Long userId) throws BaseException {
        userAdminService.revokeAdminRole(userId);
        return new BaseResponse<>("성공");
    }
}

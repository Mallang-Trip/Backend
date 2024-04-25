package mallang_trip.backend.domain.admin.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.party.dto.PartyBriefResponse;
import mallang_trip.backend.domain.party.dto.PartyDetailsResponse;
import mallang_trip.backend.domain.party.service.PartySearchService;
import mallang_trip.backend.domain.party.service.PartyService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Party Management API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/party")
public class PartyManagementController {

    private final PartySearchService partySearchService;
    private final PartyService partyService;

    @ApiOperation(value = "(관리자) 파티 목록 조회")
    @GetMapping
    @ApiImplicitParams({
        @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
        @ApiImplicitParam(name = "status", value = "canceled, before_reservation, after_reservation, finished 중 하나", required = true, paramType = "query", dataTypeClass = String.class)
    })
    @ApiResponses({
        @ApiResponse(code = 400, message = "잘못된 요청입니다."),
        @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
        @ApiResponse(code = 403, message = "권한이 없는 사용자입니다."),
        @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
        @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<List<PartyBriefResponse>> getPartiesByStatusForAdmin(
        @RequestParam(value = "status") String status) throws BaseException {
        return new BaseResponse<>(partySearchService.getPartiesByAdmin(status));
    }

    @ApiOperation(value = "(관리자) 파티 상세 조회")
    @GetMapping("/{party_id}")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
        @ApiImplicitParam(name = "party_id", value = "party_id", required = true, paramType = "path", dataTypeClass = Long.class)
    })
    @ApiResponses({
        @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
        @ApiResponse(code = 403, message = "권한이 없는 사용자입니다."),
        @ApiResponse(code = 404, message = "해당 파티를 찾을 수 없습니다."),
        @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
        @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<PartyDetailsResponse> viewPartyForAdmin(
        @PathVariable(value = "party_id") Long partyId) throws BaseException {
        return new BaseResponse<>(partySearchService.viewPartyForAdmin(partyId));
    }


    @ApiOperation(value = "(관리자) 드라이버 레디 상태 조작")
    @GetMapping("/driver-ready/{party_id}")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
        @ApiImplicitParam(name = "party_id", value = "party_id", required = true, paramType = "path", dataTypeClass = Long.class),
        @ApiImplicitParam(name = "ready", value = "레디 상태 (true or false)", required = true, paramType = "query", dataTypeClass = Boolean.class)
    })
    @ApiResponses({
        @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
        @ApiResponse(code = 403, message = "권한이 없는 사용자입니다."),
        @ApiResponse(code = 404, message = "해당 파티를 찾을 수 없습니다."),
        @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
        @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<String> changeDriverReady(
        @PathVariable(value = "party_id") Long partyId, @RequestParam(value = "ready") Boolean ready) throws BaseException {
        partyService.changeDriverReady(partyId, ready);
        return new BaseResponse<>("성공");
    }
}

package mallang_trip.backend.domain.party.controller;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.party.dto.PartyRegionDriversResponse;
import mallang_trip.backend.domain.party.dto.PartyRegionRequest;
import mallang_trip.backend.domain.party.dto.PartyRegionResponse;
import mallang_trip.backend.domain.party.service.PartyRegionService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "Party Region API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/party")
public class PartyRegionController {

    private final PartyRegionService partyRegionService;

    /**
     * (관리자) 가고 싶은 지역 추가
     */
    @ApiOperation(value = "(관리자) 가고 싶은 지역 추가")
    @PostMapping("/region")
    @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<String> addRegion(@RequestBody PartyRegionRequest request) throws BaseException {
        partyRegionService.addRegion(request);
        return new BaseResponse<>("성공");
    }

    /**
     * (관리자) 가고 싶은 지역 삭제
     *
     */
    @ApiOperation(value = "(관리자) 가고 싶은 지역 삭제")
    @DeleteMapping("/region/{region_id}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
            @ApiImplicitParam(name = "region_id", value = "지역 ID", required = true, paramType = "path", dataTypeClass = Long.class)
    })
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<String> deleteRegion(@PathVariable Long region_id) throws BaseException {
        partyRegionService.deleteRegion(region_id);
        return new BaseResponse<>("성공");
    }

    /**
     * (관리자) 가고 싶은 지역 수정
     */
    @ApiOperation(value = "(관리자) 가고 싶은 지역 수정")
    @PutMapping("/region/{region_id}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
            @ApiImplicitParam(name = "region_id", value = "지역 ID", required = true, paramType = "path", dataTypeClass = Long.class)
    })
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<String> updateRegion(@PathVariable Long region_id, @RequestBody PartyRegionRequest request) throws BaseException {
        partyRegionService.updateRegion(region_id, request);
        return new BaseResponse<>("성공");
    }

    /**
     * 가고 싶은 지역 리스트 조회
     * 관리자 , 일반 사용자 모두
     */
    @ApiOperation(value = "가고 싶은 지역 리스트 조회 - 관리자, 일반 사용자 모두")
    @ApiImplicitParam(name = "region", value = "지역 이름", required = false, paramType = "query", dataTypeClass = String.class)
    @GetMapping("/region/{region}")
    public BaseResponse<List<PartyRegionResponse>> getRegions(@RequestParam(required = false) String region) throws BaseException {
        return new BaseResponse<>(partyRegionService.getRegions(region));
    }

    /**
     * (관리자) 가고 싶은 지역 드라이버 목록 페이지
     *
     */
    @ApiOperation(value = "(관리자) 가고 싶은 지역 드라이버 목록 페이지")
    @GetMapping("/region/{region_id}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
            @ApiImplicitParam(name = "region_id", value = "지역 ID", required = true, paramType = "path", dataTypeClass = Long.class)
    })
    @ApiResponses({
            @ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
            @ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
            @ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
            @ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
    public BaseResponse<List<PartyRegionDriversResponse>> getDrivers(@PathVariable Long region_id) throws BaseException {
        return new BaseResponse<>(partyRegionService.getDrivers(region_id));
    }
}

package mallang_trip.backend.domain.region.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.region.dto.RegionDriverResponse;
import mallang_trip.backend.domain.region.dto.RegionRequest;
import mallang_trip.backend.domain.region.dto.RegionResponse;
import mallang_trip.backend.domain.region.service.RegionService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Region API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/region")
public class RegionController {

	private final RegionService regionService;

	@ApiOperation(value = "(관리자) 가고 싶은 지역 추가")
	@PostMapping
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
		@ApiResponse(code = 409, message = "이미 존재하는 지역 이름입니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<String> create(@RequestBody RegionRequest request) throws BaseException {
		regionService.create(request);
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "(관리자) 가고 싶은 지역 삭제")
	@DeleteMapping("/{region_id}")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
		@ApiImplicitParam(name = "region_id", value = "region_id", required = true, paramType = "path", dataTypeClass = Long.class)
	})
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
		@ApiResponse(code = 409, message = "해당 지역에 드라이버가 존재합니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<String> delete(@PathVariable(value = "region_id") Long regionId)
		throws BaseException {
		regionService.delete(regionId);
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "(관리자) 가고 싶은 지역 수정")
	@PutMapping("/{region_id}")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
		@ApiImplicitParam(name = "region_id", value = "region_id", required = true, paramType = "path", dataTypeClass = Long.class)
	})
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
		@ApiResponse(code = 409, message = "해당 지역에 드라이버가 존재합니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<String> modify(@PathVariable(value = "region_id") Long regionId,
		@RequestBody RegionRequest request) throws BaseException {
		regionService.modify(regionId, request);
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "가고 싶은 지역 검색")
	@ApiImplicitParam(name = "keyword", value = "지역 이름 (미입력 시 전체 조회)", required = false, paramType = "query", dataTypeClass = String.class)
	@GetMapping
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<List<RegionResponse>> search(
		@RequestParam(required = false) String keyword) throws BaseException {
		return new BaseResponse<>(regionService.search(keyword));
	}

	@ApiOperation(value = "(관리자) 가고 싶은 지역 드라이버 목록 페이지")
	@GetMapping("/driver/{region_id}")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
		@ApiImplicitParam(name = "region_id", value = "region_id", required = true, paramType = "path", dataTypeClass = Long.class)
	})
	@ApiResponses({
		@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
		@ApiResponse(code = 403, message = "권한이 없거나, 정지된 사용자입니다."),
		@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
		@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<List<RegionDriverResponse>> getDrivers(
		@PathVariable(value = "region_id") Long regionId) throws BaseException {
		return new BaseResponse<>(regionService.getDrivers(regionId));
	}
}

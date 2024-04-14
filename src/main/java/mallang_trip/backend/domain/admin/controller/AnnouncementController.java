package mallang_trip.backend.domain.admin.controller;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.admin.constant.AnnouncementType;
import mallang_trip.backend.domain.admin.service.AnnouncementService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import mallang_trip.backend.domain.admin.dto.AnnouncementBriefResponse;
import mallang_trip.backend.domain.admin.dto.AnnouncementDetailsResponse;
import mallang_trip.backend.domain.admin.dto.AnnouncementIdResponse;
import mallang_trip.backend.domain.admin.dto.AnnouncementRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Api(tags = "Announcement API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/announcement")
public class AnnouncementController {

	private final AnnouncementService announcementService;

	@ApiOperation(value = "(관리자)공지 등록")
	@PostMapping
	@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class)
	@ApiResponses({
			@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
			@ApiResponse(code = 403, message = "정지된 사용자입니다."),
			@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
			@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public BaseResponse<AnnouncementIdResponse> create(@RequestBody AnnouncementRequest request)
		throws BaseException {
		return new BaseResponse<>(announcementService.create(request));
	}

	@ApiOperation(value = "(관리자)공지 삭제")
	@DeleteMapping("/{announcement_id}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
			@ApiImplicitParam(name = "announcement_id", value = "announcement_id", required = true, paramType = "path", dataTypeClass = Long.class)
	})
	@ApiResponses({
			@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
			@ApiResponse(code = 403, message = "삭제 권한이 없는 사용자입니다."),
			@ApiResponse(code = 404, message = "해당 게시글을 찾을 수 없습니다."),
			@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
			@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public BaseResponse<String> delete(@PathVariable(value = "announcement_id") Long announcementId)
		throws BaseException {
		announcementService.delete(announcementId);
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "(관리자)공지 수정")
	@PutMapping("/{announcement_id}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "access-token", value = "Access Token", required = true, paramType = "header", dataTypeClass = String.class),
			@ApiImplicitParam(name = "announcement_id", value = "announcement_id", required = true, paramType = "path", dataTypeClass = Long.class)
	})
	@ApiResponses({
			@ApiResponse(code = 401, message = "인증되지 않은 사용자입니다."),
			@ApiResponse(code = 403, message = "수정 권한이 없거나, 정지된 사용자입니다."),
			@ApiResponse(code = 404, message = "해당 게시글을 찾을 수 없습니다."),
			@ApiResponse(code = 10002, message = "유효하지 않은 Refresh Token 입니다."),
			@ApiResponse(code = 10003, message = "만료된 Refresh Token 입니다.")
	})
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public BaseResponse<String> modify(@PathVariable(value = "announcement_id") Long announcementId,
		@RequestBody AnnouncementRequest request) throws BaseException {
		announcementService.modify(announcementId, request);
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "공지 목록 조회")
	@GetMapping
	@ApiImplicitParam(name = "type", value = "공지 타입 (Announcement | FAQ)", required = true, paramType = "query", dataTypeClass = AnnouncementType.class)
	@PreAuthorize("permitAll()")
	public BaseResponse<Page<AnnouncementBriefResponse>> get(@RequestParam AnnouncementType type,
		@PageableDefault(size = 6) Pageable pageable) throws BaseException {
		return new BaseResponse<>(announcementService.get(type, pageable));
	}

	@ApiOperation(value = "공지 상세 조회")
	@GetMapping("/{announcement_id}")
	@ApiImplicitParam(name = "announcement_id", value = "announcement_id", required = true, paramType = "path", dataTypeClass = Long.class)
	@PreAuthorize("permitAll()")
	public BaseResponse<AnnouncementDetailsResponse> view(
		@PathVariable(value = "announcement_id") Long announcementId) throws BaseException {
		return new BaseResponse<>(announcementService.view(announcementId));
	}
}

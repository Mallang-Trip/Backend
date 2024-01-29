package mallang_trip.backend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.constant.AnnouncementType;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponse;
import mallang_trip.backend.domain.dto.admin.AnnouncementBriefResponse;
import mallang_trip.backend.domain.dto.admin.AnnouncementDetailsResponse;
import mallang_trip.backend.domain.dto.admin.AnnouncementIdResponse;
import mallang_trip.backend.domain.dto.admin.AnnouncementRequest;
import mallang_trip.backend.service.admin.AnnouncementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Announcement API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/announcement")
public class AnnouncementController {

	private final AnnouncementService announcementService;

	@ApiOperation(value = "(관리자)공지 등록")
	@PostMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public BaseResponse<AnnouncementIdResponse> create(@RequestBody AnnouncementRequest request)
		throws BaseException {
		return new BaseResponse<>(announcementService.create(request));
	}

	@ApiOperation(value = "(관리자)공지 삭제")
	@DeleteMapping("/{announcement_id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public BaseResponse<String> delete(@PathVariable(value = "announcement_id") Long announcementId)
		throws BaseException {
		announcementService.delete(announcementId);
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "공지 목록 조회")
	@GetMapping
	@PreAuthorize("permitAll()")
	public BaseResponse<Page<AnnouncementBriefResponse>> get(@RequestParam AnnouncementType type,
		@PageableDefault(size = 6) Pageable pageable) throws BaseException {
		return new BaseResponse<>(announcementService.get(type, pageable));
	}

	@ApiOperation(value = "공지 상세 조회")
	@GetMapping("/{announcement_id}")
	@PreAuthorize("permitAll()")
	public BaseResponse<AnnouncementDetailsResponse> view(
		@PathVariable(value = "announcement_id") Long announcementId) throws BaseException {
		return new BaseResponse<>(announcementService.view(announcementId));
	}
}

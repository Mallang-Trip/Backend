package mallang_trip.backend.domain.party.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import mallang_trip.backend.domain.party.dto.PartyBriefResponse;
import mallang_trip.backend.domain.party.dto.PartyDetailsResponse;
import mallang_trip.backend.domain.party.service.PartyDibsService;
import mallang_trip.backend.domain.party.service.PartyHistoryService;
import mallang_trip.backend.domain.party.service.PartySearchService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Party Search API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/party")
public class PartySearchController {

	private final PartySearchService partySearchService;
	private final PartyHistoryService partyHistoryService;
	private final PartyDibsService partyDibsService;

	@ApiOperation(value = "파티 검색")
	@GetMapping("/search")
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<List<PartyBriefResponse>> searchParty(@RequestParam String region,
		@RequestParam Integer headcount, @RequestParam String startDate,
		@RequestParam String endDate, @RequestParam Integer maxPrice) throws BaseException {
		return new BaseResponse<>(
			partySearchService.searchRecruitingParties(region, headcount, startDate, endDate,
				maxPrice));
	}

	@ApiOperation(value = "최근 본 파티 조회")
	@GetMapping("/history")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<List<PartyBriefResponse>> getPartyHistory() throws BaseException {
		return new BaseResponse<>(partyHistoryService.getPartyHistory());
	}

	@ApiOperation(value = "(유저) 내 파티 조회")
	@GetMapping("/my")
	@PreAuthorize("hasRole('ROLE_USER')") // 일반 사용자
	public BaseResponse<List<PartyBriefResponse>> getMyParties() throws BaseException {
		return new BaseResponse<>(partySearchService.getMyPartiesByMember());
	}

	@ApiOperation(value = "(드라이버) 내 파티 조회")
	@GetMapping("/driver")
	@PreAuthorize("hasRole('ROLE_DRIVER')") // 드라이버
	public BaseResponse<List<PartyBriefResponse>> getMyPartiesByDriver() throws BaseException {
		return new BaseResponse<>(partySearchService.getMyPartiesByDriver());
	}

	@ApiOperation(value = "파티 상세 조회")
	@GetMapping("/{party_id}")
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<PartyDetailsResponse> viewParty(
		@PathVariable(value = "party_id") Long partyId) throws BaseException {
		return new BaseResponse<>(partySearchService.getPartyDetails(partyId));
	}

	@ApiOperation(value = "찜한 파티 조회")
	@GetMapping("/dibs")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<List<PartyBriefResponse>> getPartyDibs() throws BaseException {
		return new BaseResponse<>(partyDibsService.getMyPartyDibs());
	}

	@ApiOperation(value = "파티 찜하기")
	@PostMapping("/dibs/{party_id}")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> createPartyDibs(@PathVariable(value = "party_id") Long partyId)
		throws BaseException {
		partyDibsService.createPartyDibs(partyId);
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "파티 찜 취소하기")
	@DeleteMapping("/dibs/{party_id}")
	@PreAuthorize("isAuthenticated()") // 로그인 사용자
	public BaseResponse<String> deletePartyDibs(@PathVariable(value = "party_id") Long partyId)
		throws BaseException {
		partyDibsService.deletePartyDibs(partyId);
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "(관리자) 파티 목록 조회")
	@GetMapping("/admin")
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<List<PartyBriefResponse>> getPartiesByStatusForAdmin(
		@RequestParam(value = "status") String status) throws BaseException {
		return new BaseResponse<>(partySearchService.getPartiesByAdmin(status));
	}

	@ApiOperation(value = "(관리자) 파티 상세 조회")
	@GetMapping("/admin/{party_id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자
	public BaseResponse<PartyDetailsResponse> viewPartyForAdmin(
		@PathVariable(value = "party_id") Long partyId) throws BaseException {
		return new BaseResponse<>(partySearchService.viewPartyForAdmin(partyId));
	}
}

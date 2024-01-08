package mallang_trip.backend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponse;
import mallang_trip.backend.domain.dto.party.PartyBriefResponse;
import mallang_trip.backend.service.party.PartyHistoryService;
import mallang_trip.backend.service.party.PartySearchService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Party Search API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/party/search")
public class PartySearchController {

	private final PartySearchService partySearchService;
	private final PartyHistoryService partyHistoryService;

	@ApiOperation(value = "파티 검색")
	@GetMapping
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
}

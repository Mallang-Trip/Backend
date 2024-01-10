package mallang_trip.backend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponse;
import mallang_trip.backend.domain.dto.party.PartyBriefResponse;
import mallang_trip.backend.domain.dto.party.PartyDetailsResponse;
import mallang_trip.backend.service.party.PartyHistoryService;
import mallang_trip.backend.service.party.PartySearchService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}

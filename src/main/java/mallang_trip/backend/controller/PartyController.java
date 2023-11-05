package mallang_trip.backend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponse;
import mallang_trip.backend.domain.dto.Party.JoinPartyRequest;
import mallang_trip.backend.domain.dto.Party.PartyBriefResponse;
import mallang_trip.backend.domain.dto.Party.PartyDetailsResponse;
import mallang_trip.backend.domain.dto.Party.PartyIdResponse;
import mallang_trip.backend.domain.dto.Party.PartyRequest;
import mallang_trip.backend.domain.dto.course.CourseRequest;
import mallang_trip.backend.service.PartyService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@Api(tags = "Party API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/party")
public class PartyController {

	private final PartyService partyService;

	@PostMapping("/start")
	@ApiOperation(value = "파티 생성")
	public BaseResponse<PartyIdResponse> createParty(@RequestBody PartyRequest request)
		throws BaseException {
		return new BaseResponse<>(partyService.createParty(request));
	}

	@PostMapping("/accept/start/{id}")
	@ApiOperation(value = "(드라이버) 파티 생성 수락/거절")
	public BaseResponse<String> acceptCreateParty(@PathVariable Long id,
		@RequestParam Boolean accept) throws BaseException {
		partyService.acceptCreateParty(id, accept);
		return new BaseResponse<>("성공");
	}

	@PostMapping("/join")
	@ApiOperation(value = "파티 가입하기")
	public BaseResponse<String> joinParty(@RequestBody JoinPartyRequest request)
		throws BaseException {
		partyService.joinParty(request);
		return new BaseResponse<>("성공");
	}

	@PostMapping("/course/{partyId}")
	@ApiOperation(value = "코스 변경 제안")
	public BaseResponse<String> changeCourse(@PathVariable Long partyId,
		@RequestBody CourseRequest request) throws BaseException {
		partyService.proposeCourseChange(partyId, request);
		return new BaseResponse<>("성공");
	}

	@PostMapping("/accept/{proposalId}")
	@ApiOperation(value = "파티 가입, 코스 변경 수락/거절")
	public BaseResponse<String> acceptProposal(@PathVariable Long proposalId,
		@RequestParam Boolean accept) throws BaseException {
		partyService.acceptProposal(proposalId, accept);
		return new BaseResponse<>("성공");
	}

	@GetMapping("/list")
	@ApiOperation(value = "모집중인 파티 조회")
	public BaseResponse<List<PartyBriefResponse>> findParties(@RequestParam String region,
		@RequestParam Integer headcount, @RequestParam String startDate,
		@RequestParam String endDate, @RequestParam Integer maxPrice) throws BaseException {
		return new BaseResponse<>(
			partyService.findParties(region, headcount, startDate, endDate, maxPrice));
	}

	@GetMapping("/my")
	@ApiOperation(value = "내 파티 조회")
	public BaseResponse<List<PartyBriefResponse>> getMyParties() throws BaseException {
		return new BaseResponse<>(partyService.getMyParties());
	}

	@GetMapping("/history")
	@ApiOperation(value = "내가 본 파티 조회")
	public BaseResponse<List<PartyBriefResponse>> getPartyHistories() throws BaseException {
		return new BaseResponse<>(partyService.getHistory());
	}

	@GetMapping("/view/{partyId}")
	@ApiOperation(value = "파티 상세 조회")
	public BaseResponse<PartyDetailsResponse> getPartyDetails(@PathVariable Long partyId)
		throws BaseException {
		return new BaseResponse<>(partyService.getPartyDetails(partyId));
	}

	@PutMapping("/course/{proposalId}")
	@ApiOperation(value = "코스변경 다시 제안하기")
	public BaseResponse<String> reProposeCourseChange(@PathVariable Long proposalId,
		@RequestBody CourseRequest request) throws BaseException {
		partyService.reProposeCourseChange(proposalId, request);
		return new BaseResponse<>("성공");
	}

	@PutMapping("/join/{proposalId}")
	@ApiOperation(value = "파티가입 다시 제안하기")
	public BaseResponse<String> reProposeJoinParty(@PathVariable Long proposalId,
		@RequestBody JoinPartyRequest request) throws BaseException {
		partyService.reProposeJoinParty(proposalId, request);
		return new BaseResponse<>("성공");
	}

	@DeleteMapping("/proposal/{proposalId}")
	@ApiOperation(value = "제안 취소")
	public BaseResponse<String> cancelProposal(@PathVariable Long proposalId) throws BaseException {
		partyService.cancelProposal(proposalId);
		return new BaseResponse<>("성공");
	}
}

package mallang_trip.backend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponse;
import mallang_trip.backend.domain.dto.party.ChangeCourseRequest;
import mallang_trip.backend.domain.dto.party.CreatePartyRequest;
import mallang_trip.backend.domain.dto.party.JoinPartyRequest;
import mallang_trip.backend.domain.dto.party.PartyIdResponse;
import mallang_trip.backend.service.party.PartyService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Party API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/party")
public class PartyController {

	private final PartyService partyService;

	@ApiOperation(value = "파티 생성 신청")
	@PostMapping("/create")
	@PreAuthorize("hasRole('ROLE_USER')") // 일반 사용자
	public BaseResponse<PartyIdResponse> createParty(@RequestBody CreatePartyRequest request)
		throws BaseException {
		return new BaseResponse<>(partyService.createParty(request));
	}

	@ApiOperation(value = "파티 생성 취소")
	@DeleteMapping("/create/{party_id}")
	@PreAuthorize("hasRole('ROLE_USER')") // 일반 사용자
	public BaseResponse<String> cancelCreateParty(@PathVariable("party_id") Long partyId)
		throws BaseException {
		partyService.cancelCreateParty(partyId);
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "(드라이버) 파티 생성 수락 or 거절")
	@PostMapping("/create/{party_id}")
	@PreAuthorize("hasRole('ROLE_DRIVER')") // 드라이버
	public BaseResponse<String> acceptCreateParty(@PathVariable(value = "party_id") Long partyId,
		@RequestParam Boolean accept) throws BaseException {
		partyService.acceptCreateParty(partyId, accept);
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "파티 가입 신청")
	@PostMapping("/join/{party_id}")
	@PreAuthorize("hasRole('ROLE_USER')") // 일반 사용자
	public BaseResponse<String> joinParty(@PathVariable(value = "party_id") Long partyId,
		@RequestBody JoinPartyRequest request) throws BaseException {
		partyService.requestPartyJoin(partyId, request);
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "코스 변경 제안")
	@PostMapping("/change/{party_id}")
	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_DRIVER')") // 일반 사용자, 드라이버
	public BaseResponse<String> changeCourse(@PathVariable(value = "party_id") Long partyId,
		@RequestBody ChangeCourseRequest request) throws BaseException {
		partyService.requestCourseChange(partyId, request);
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "파티가입신청 또는 코스변경제안 취소")
	@DeleteMapping("/proposal/{proposal_id}")
	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_DRIVER')") // 일반 사용자, 드라이버
	public BaseResponse<String> cancelProposal(@PathVariable(value = "proposal_id") Long proposalId)
		throws BaseException {
		partyService.cancelProposal(proposalId);
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "파티가입신청 또는 코스변경제안 투표")
	@PostMapping("/proposal/{proposal_id}")
	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_DRIVER')") // 일반 사용자, 드라이버
	public BaseResponse<String> voteProposal(@PathVariable(value = "proposal_id") Long proposalId,
		@RequestParam Boolean accept) throws BaseException {
		partyService.voteProposal(proposalId, accept);
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "파티 레디하기 또는 레디 취소")
	@PostMapping("/ready/{party_id}")
	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_DRIVER')") // 일반 사용자, 드라이버
	public BaseResponse<String> ready(@PathVariable(value = "party_id") Long partyId,
		@RequestParam Boolean accept) throws BaseException {
		partyService.setReady(partyId, accept);
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "(예약 전) 파티 탈퇴")
	@DeleteMapping("/quit/{party_id}")
	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_DRIVER')") // 일반 사용자, 드라이버
	public BaseResponse<String> quit(@PathVariable(value = "party_id") Long partyId)
		throws BaseException {
		partyService.quitPartyBeforeReservation(partyId);
		return new BaseResponse<>("성공");
	}

	@ApiOperation(value = "(예약 후) 파티 탈퇴")
	@DeleteMapping("/reservation/{party_id}")
	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_DRIVER')") // 일반 사용자, 드라이버
	public BaseResponse<String> cancelReservation(@PathVariable(value = "party_id") Long partyId)
		throws BaseException {
		partyService.cancelReservation(partyId);
		return new BaseResponse<>("성공");
	}
}

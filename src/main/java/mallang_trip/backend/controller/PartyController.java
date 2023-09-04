package mallang_trip.backend.controller;

import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponse;
import mallang_trip.backend.domain.dto.Party.JoinPartyRequest;
import mallang_trip.backend.domain.dto.Party.PartyIdResponse;
import mallang_trip.backend.domain.dto.Party.PartyRequest;
import mallang_trip.backend.service.PartyService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
@RequiredArgsConstructor
@RequestMapping("/party")
public class PartyController {

	private final PartyService partyService;

	@PostMapping
	public BaseResponse<PartyIdResponse> createParty(@RequestBody PartyRequest request)
		throws BaseException {
		return new BaseResponse<>(partyService.createParty(request));
	}

	@PostMapping("/driver/accept/{id}")
	public BaseResponse<String> acceptCreateParty(@PathVariable Long id,
		@RequestParam Boolean accept) throws BaseException {
		partyService.acceptCreateParty(id, accept);
		return new BaseResponse<>("성공");
	}

	@PostMapping("/join")
	public BaseResponse<String> joinParty(@RequestBody JoinPartyRequest request)
		throws BaseException {
		partyService.joinParty(request);
		return new BaseResponse<>("성공");
	}

}

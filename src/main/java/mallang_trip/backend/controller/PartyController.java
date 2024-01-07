package mallang_trip.backend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.controller.io.BaseException;
import mallang_trip.backend.controller.io.BaseResponse;
import mallang_trip.backend.domain.dto.party.JoinPartyRequest;
import mallang_trip.backend.domain.dto.party.PartyBriefResponse;
import mallang_trip.backend.domain.dto.party.PartyDetailsResponse;
import mallang_trip.backend.domain.dto.party.PartyIdResponse;
import mallang_trip.backend.domain.dto.party.PartyRequest;
import mallang_trip.backend.domain.dto.course.CourseRequest;
import mallang_trip.backend.service.party.PartySearchService;
import mallang_trip.backend.service.party.PartyService;
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

@Api(tags = "Party API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/party")
public class PartyController {

	private final PartyService partyService;

}

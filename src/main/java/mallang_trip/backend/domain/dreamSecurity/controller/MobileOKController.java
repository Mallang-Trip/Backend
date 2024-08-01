package mallang_trip.backend.domain.dreamSecurity.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.dreamSecurity.dto.MobileOKStdResponse;
import mallang_trip.backend.domain.dreamSecurity.service.MobileOKService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "mobileOK API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/mobileOK")
public class MobileOKController {

	private final MobileOKService mobileOKService;

	@ApiOperation(value = "본인인증-표준창 인증요청")
	@PostMapping
	@PreAuthorize("permitAll()") // anyone
	public MobileOKStdResponse std_request(HttpSession session) throws BaseException {
		return mobileOKService.mobileOK_std_request(session);
	}

	@ApiOperation(value = "본인인증-표준창 검증결과 요청")
	@PostMapping("/result")
	@PreAuthorize("permitAll()") // anyone
	public BaseResponse<String> std_result(@RequestBody String result, HttpSession session)
		throws BaseException {
		mobileOKService.mobileOK_std_result(result, session);
		return new BaseResponse<>("성공");
	}
}

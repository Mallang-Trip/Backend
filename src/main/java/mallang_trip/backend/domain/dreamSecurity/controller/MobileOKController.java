package mallang_trip.backend.domain.dreamSecurity.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import mallang_trip.backend.domain.dreamSecurity.dto.MobileOKStdResponse;
import mallang_trip.backend.domain.dreamSecurity.dto.MobileOKStdResultResponse;
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
	public MobileOKStdResponse std_request() throws BaseException {
		return mobileOKService.mobileOK_std_request();
	}

	@ApiOperation(value = "본인인증-표준창 검증결과 요청")
	@PostMapping("/result")
	@PreAuthorize("permitAll()") // anyone
	@ApiResponses({
		@ApiResponse(code = 401, message = "본인확인 결과인증 후 10분 경과"),
		@ApiResponse(code = 403, message = "19세 미만은 가입이 불가능합니다."),
		@ApiResponse(code = 409, message = "이미 가입된 아이디가 존재합니다."),
		@ApiResponse(code = 500, message = "본인인증 서버 통신 실패.")
	})
	public BaseResponse<MobileOKStdResultResponse> std_result(@RequestBody String result)
		throws BaseException {
		return new BaseResponse<>(mobileOKService.mobileOK_std_result(result));
	}
}

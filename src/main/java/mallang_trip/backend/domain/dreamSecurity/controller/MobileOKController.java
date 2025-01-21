package mallang_trip.backend.domain.dreamSecurity.controller;

import java.util.Map;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mallang_trip.backend.domain.dreamSecurity.dto.MobileOKStdResponse;
import mallang_trip.backend.domain.dreamSecurity.dto.MobileOKStdResultResponse;
import mallang_trip.backend.domain.dreamSecurity.service.MobileOKService;
import mallang_trip.backend.global.io.BaseException;
import mallang_trip.backend.global.io.BaseResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j(topic = "PASS")
@Api(tags = "mobileOK API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/mobileOK")
public class MobileOKController {

	private final MobileOKService mobileOKService;

	@ApiOperation(value = "본인인증-표준창 인증요청")
	@PostMapping
	@PreAuthorize("permitAll()") // anyone
	public MobileOKStdResponse std_request(@RequestHeader(value = "User-Agent", required = false) String userAgent) throws BaseException {

		log.info("agent: {}", userAgent);

		MobileOKStdResponse mobileOKStdResponse = mobileOKService.mobileOK_std_request();

		log.info("mobileOKStdResponse: {}", mobileOKStdResponse);
		return mobileOKStdResponse;
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
	public BaseResponse<MobileOKStdResultResponse> std_result(
		@RequestHeader(value = "User-Agent", required = false) String userAgent,
		@RequestBody String result)
		throws BaseException {

		log.info("agent: {}", userAgent);
		log.info("PASS 인증 결과 파라미터: {}", result);

		MobileOKStdResultResponse mobileOKStdResultResponse = mobileOKService.mobileOK_std_result(result);
		log.info("PASS 인증 결과 반환: {}", mobileOKStdResultResponse);

		return new BaseResponse<>(mobileOKStdResultResponse);
	}
}

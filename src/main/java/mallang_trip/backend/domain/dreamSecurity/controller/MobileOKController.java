package mallang_trip.backend.domain.dreamSecurity.controller;

import javax.servlet.http.HttpServletRequest;

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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
	public ResponseEntity<?> std_request(HttpServletRequest request) throws BaseException {

		MobileOKStdResponse mobileOKStdResponse = mobileOKService.mobileOK_std_request();

		log.info("mobileOKStdResponse: {}", mobileOKStdResponse);
		return ResponseEntity.ok(mobileOKStdResponse);
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
	public ResponseEntity<MobileOKStdResultResponse> std_result(
		HttpServletRequest request,
		@RequestBody String result)
		throws BaseException {

		String agent = request.getHeader("User-Agent");// 사용자 기기 정보 추출
		String iosRedirectUri = "https://mallangtrip.com/signup";
		log.info("인증 표준창. agent: {}", agent);
		log.info("PASS 인증 결과 파라미터: {}", result);

		MobileOKStdResultResponse mobileOKStdResultResponse = mobileOKService.mobileOK_std_result(result);
		log.info("PASS 인증 결과 반환: {}", mobileOKStdResultResponse.toString());

		/*쿼리파라미터 추가*/
		iosRedirectUri += "?impUid=" + mobileOKStdResultResponse.getImpUid();
		iosRedirectUri += "&statusCode=200";

		/*ios 리다이렉트*/
		log.info("redirect uri: {}", iosRedirectUri);
		return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
			.header("Location", iosRedirectUri)// 말랑트립 메인페이지로 리다이렉트(임시)
			.build();

		/**사용자 기기 인식*/
		// if(agent!=null && (agent.contains("iPhone") || agent.contains("iPad") ||agent.contains("Macintosh"))) {
		// 	log.info("ios 유저입니다. 요청을 리다이렉트 합니다. agent: {}, redirect uri: {}", agent, iosRedirectUri);
		// 	return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
		// 		.header("Location", iosRedirectUri)// 말랑트립 메인페이지로 리다이렉트(임시)
		// 		.body(mobileOKStdResultResponse);
		// }
		//
		// return ResponseEntity.ok(mobileOKStdResultResponse);
	}
}

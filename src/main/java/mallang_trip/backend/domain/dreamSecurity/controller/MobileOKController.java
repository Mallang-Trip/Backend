package mallang_trip.backend.domain.dreamSecurity.controller;

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
	public ResponseEntity<MobileOKStdResponse> std_request() throws BaseException {

		MobileOKStdResponse mobileOKStdResponse = mobileOKService.mobileOK_std_request();

		log.info("mobileOKStdResponse: {}", mobileOKStdResponse.toString());
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
	public ResponseEntity<Void> std_result(@RequestBody String result)
		throws BaseException {
		MobileOKStdResultResponse mobileOKStdResultResponse = null;
		int statusCode = 200;

		String iosRedirectUri = "https://mallangtrip.com/signup";

		/*예외 코드를 쿼리파라미터 형태로 반환하기 위한 작업*/
		try{
			mobileOKStdResultResponse = mobileOKService.mobileOK_std_result(result);
		} catch (BaseException e) {
			statusCode = e.getStatus().getStatusCode();
		} catch (RuntimeException e) {
			statusCode = 500;// 예상치 못한 예외
			log.error("예상하지 못한 예외 발생! cause: {}, message: {}", e.getCause().getMessage(), e.getMessage() );
		}

		/*쿼리파라미터 추가*/
		if(mobileOKStdResultResponse == null) {// 인증 실패
			iosRedirectUri += "?statusCode=" + statusCode;
			log.warn("pass 인증 실패. redirect uri: {}", iosRedirectUri);
		} else {// 인증 성공
			iosRedirectUri += "?impUid=" + mobileOKStdResultResponse.getImpUid();
			iosRedirectUri += "&statusCode=" + statusCode;
			log.info("pass 인증 성공. redirect uri: {}", iosRedirectUri);
		}

		return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
			.header("Location", iosRedirectUri)// 말랑트립 회원가입 페이지로 리다이렉트
			.build();
	}
}

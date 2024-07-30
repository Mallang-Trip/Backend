package mallang_trip.backend.domain.dreamSecurity.service;

import static mallang_trip.backend.domain.dreamSecurity.exception.IdentificationException.SESSION_ERROR;
import static mallang_trip.backend.domain.dreamSecurity.exception.IdentificationException.TOKEN_TIMEOUT;
import static mallang_trip.backend.global.io.BaseResponseStatus.Internal_Server_Error;

import feign.template.UriUtils;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mallang_trip.backend.domain.dreamSecurity.dto.MobileOKStdResponse;
import mallang_trip.backend.global.io.BaseException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dreamsecurity.mobileOK.*;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MobileOKService {

	private BufferedReader bufferedReader;

	@Value("${dream-security.password}")
	private String password;

	private final String clientPrefix = "MALLANG";

	private mobileOKKeyManager initMobileOK() {
		mobileOKKeyManager mobileOK = new mobileOKKeyManager();

		try {
			// 리소스 파일을 ClassPathResource로 로드
			Resource resource = new ClassPathResource("dreamsecurity/mok_keyInfo.dat");

			// InputStream을 통해 리소스를 읽어들임
			try (InputStream inputStream = resource.getInputStream()) {
				// 임시 파일 생성
				File tempFile = File.createTempFile("mok_keyInfo", ".dat");

				// 임시 파일에 리소스 파일의 내용을 복사
				try (FileOutputStream outStream = new FileOutputStream(tempFile)) {
					byte[] buffer = new byte[4096];
					int bytesRead;

					while ((bytesRead = inputStream.read(buffer)) != -1) {
						outStream.write(buffer, 0, bytesRead);
					}
				}

				// mobileOK의 keyInit 메서드 호출
				mobileOK.keyInit(tempFile.getAbsolutePath(), password);

				// 임시 파일 삭제
				Files.deleteIfExists(tempFile.toPath());
			}

		} catch (IOException e) {
			log.error("mobileOK init failed: " + e.getMessage());
			throw new BaseException(Internal_Server_Error);
		} catch (MobileOKException e) {
			log.error("mobileOK init failed: " + e.getErrorCode() + "|" + e.getMessage());
			throw new BaseException(Internal_Server_Error);
		}

		return mobileOK;
	}

	/**
	 * 본인확인-표준창 인증요청
	 */
	public MobileOKStdResponse mobileOK_std_request(HttpSession session) {
		mobileOKKeyManager mobileOK = initMobileOK();

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

		// 본인확인-표준창 거래요청정보 생성
		String clientTxId = clientPrefix + UUID.randomUUID().toString().replaceAll("-", "");
		session.setAttribute("sessionClientTxId", clientTxId);
		String reqClientInfo = clientTxId + "|" + formatter.format(cal.getTime());

		// 생성된 거래정보 암호화
		String encryptReqClientInfo;
		try{
			encryptReqClientInfo = mobileOK.RSAEncrypt(reqClientInfo);
		} catch (MobileOKException e){
			log.info("mobileOK encryption failed: " + e.getErrorCode() + "|" + e.getMessage());
			throw new BaseException(Internal_Server_Error);
		}

		// 거래 요청 정보 반환
		return MobileOKStdResponse.builder()
			.usageCode("01001") /* 01001 : 회원가입, 01002 : 정보변경, 01003 : ID찾기, 01004 : 비밀번호찾기, 01005 : 본인확인용, 01006 : 성인인증, 01007 : 상품구매/결제, 01999 : 기타 */
			.serviceId(mobileOK.getServiceId()) /* 본인확인 이용기관 서비스 ID (키파일에 serviceId 포함 됨) */
			.encryptReqClientInfo(encryptReqClientInfo) /* 암호화된 본인확인 거래 요청 정보 */
			.serviceType("telcoAuth") /* 이용상품 코드, telcoAuth : 휴대폰본인확인 (SMS인증시 인증번호 발송 방식 “SMS”)*/
			.retTransferType("MOKToken") /* 본인확인 결과 타입, "MOKToken"  : 개인정보 응답결과를 이용기관 서버에서 본인확인 서버에 요청하여 수신 후 처리 */
			.returnUrl("https://dev.mallangtrip-server.com/api/mobileOK/result") /* 본인확인 결과 수신 URL "https://" 포함한 URL 입력 */
			.build();
	}

	/**
	 * 본인확인-표준창 검증결과 요청
	 */
	public String mobileOK_std_result(String result, HttpSession session) {
		try {
			result = UriUtils.decode(result, StandardCharsets.UTF_8);
			result = UriUtils.decode(result, StandardCharsets.UTF_8);
			result = result.substring("data=".length());

			mobileOKKeyManager mobileOK = initMobileOK();

			// 본인확인 인증결과 MOKToken API 요청 URL
			String targetUrl = "https://scert.mobile-ok.com/gui/service/v1/result/request";  // 개발
			// String targetUrl = "https://cert.mobile-ok.com/gui/service/v1/result/request";  // 운영

			// 본인확인 결과 타입별 결과 처리
			JSONObject resultJSON = new JSONObject(result);
			String encryptMOKKeyToken = resultJSON.optString("encryptMOKKeyToken", null);
			String encryptMOKResult = resultJSON.optString("encryptMOKResult", null);
			/* 본인확인 결과 타입 : MOKToken */
			if (encryptMOKKeyToken != null) {
				JSONObject requestData = new JSONObject();
				requestData.put("encryptMOKKeyToken", encryptMOKKeyToken);
				String responseData = sendPost(targetUrl, requestData.toString());
				if (responseData == null) {
					log.error("-1|본인확인 MOKToken 인증결과 응답이 없습니다.");
					throw new BaseException(Internal_Server_Error);
				}
				JSONObject responseJSON = new JSONObject(responseData);
				encryptMOKResult = responseJSON.getString("encryptMOKResult");
			}
			else {
				/* 본인확인 결과 타입 : MOKResult */
				if (encryptMOKResult == null) {
					log.error("-2|본인확인 MOKResult 값이 없습니다.");
					throw new BaseException(Internal_Server_Error);
				}
			}

			// 본인확인 결과 JSON 정보 파싱
			JSONObject decrpytResultJson = null;
			try {
				decrpytResultJson = new JSONObject(mobileOK.getResultJSON(encryptMOKResult));
			} catch (MobileOKException e) {
				return e.getErrorCode() + "|" + e.getMessage();
			}

			// 본인확인 결과 복호화

			/* 사용자 이름 */
			String userName = decrpytResultJson.optString("userName", null);
			/* 이용기관 ID */
			String siteID = decrpytResultJson.optString("siteID", null);
			/* 이용기관 거래 ID */
			String clientTxId = decrpytResultJson.optString("clientTxId", null);
			/* 본인확인 거래 ID */
			String txId = decrpytResultJson.optString("txId", null);
			/* 서비스제공자(인증사업자) ID */
			String providerId = decrpytResultJson.optString("providerId", null);
			/* 이용 서비스 유형 */
			String serviceType = decrpytResultJson.optString("serviceType", null);
			/* 시용자 CI */
			String ci = decrpytResultJson.optString("ci", null);
			/* 사용자 DI */
			String di = decrpytResultJson.optString("di", null);
			/* 사용자 전화번호 */
			String userPhone = decrpytResultJson.optString("userPhone", null);
			/* 사용자 생년월일 */
			String userBirthday = decrpytResultJson.optString("userBirthday", null);
			/* 사용자 성별 (1: 남자, 2: 여자) */
			String userGender = decrpytResultJson.optString("userGender", null);
			/* 사용자 국적 (0: 내국인, 1: 외국인) */
			String userNation = decrpytResultJson.optString("userNation", null);
			/* 본인확인 인증 종류 */
			String reqAuthType = decrpytResultJson.getString("reqAuthType");
			/* 본인확인 요청 시간 */
			String reqDate = decrpytResultJson.getString("reqDate");
			/* 본인확인 인증 서버 */
			String issuer = decrpytResultJson.getString("issuer");
			/* 본인확인 인증 시간 */
			String issueDate = decrpytResultJson.getString("issueDate");

			String sessionClientTxId = (String) session.getAttribute("sessionClientTxId");

			/* 5. 이용기관 응답데이터 셔션 및 검증유효시간 처리  */

			// 세션 내 요청 clientTxId 와 수신한 clientTxId 가 동일한지 비교(권고)
			if (!sessionClientTxId.equals(clientTxId)) {
				throw new BaseException(SESSION_ERROR);
			}
			// 검증정보 유효시간 검증 (본인확인 결과인증 후 10분 이내 검증 권고) */
			String dataFormat = "yyyy-MM-dd HH:mm:ss";
			SimpleDateFormat formatter = new SimpleDateFormat(dataFormat);

			Date currentTime = formatter.parse(formatter.format(new Date()));
			Date targetTime = formatter.parse(issueDate);

			long diff = (currentTime.getTime() - targetTime.getTime()) / 1000;
			if (diff > 600) {
				throw new BaseException(TOKEN_TIMEOUT);
			}

			/* 6. 이용기관 서비스 기능 처리 */

			// - 이용기관에서 수신한 개인정보 검증 확인

			// - 이용기관에서 수신한 CI 확인

			/* 7. 본인확인 결과 응답 */

			// 복호화된 개인정보는 DB보관 또는 세션보관하여 개인정보 저장시 본인확인에서 획득한 정보로 저장하도록 처리 필요
			// 개인정보를 웹브라우져에 전달할 경우 외부 해킹에 의해 유출되지 않도록 보안처리 필요

			JSONObject outputJson = new JSONObject();
			outputJson.put("resultCode", "2000");
			outputJson.put("resultMsg", "성공");
			outputJson.put("userName", userName);
			return outputJson.toString();
		} catch (Exception e) {
			log.error("본인확인-표준창 인증요청 실패: " + e.getMessage());
			throw new BaseException(Internal_Server_Error);
		}
	}

	/* 본인확인 서버 통신 예제 함수 */
	public String sendPost(String dest, String jsonData) {
		HttpURLConnection connection = null;
		DataOutputStream dataOutputStream = null;
		try {
			URL url = new URL(dest);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
			connection.setDoOutput(true);

			dataOutputStream = new DataOutputStream(connection.getOutputStream());
			dataOutputStream.write(jsonData.getBytes("UTF-8"));

			bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuffer responseData = new StringBuffer();
			String info;
			while ((info = bufferedReader.readLine()) != null) {
				responseData.append(info);
			}
			return responseData.toString();
		} catch (Exception e) {
			log.error("본인확인 서버 통신 실패: " + e.getMessage());
		}  finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}

				if (dataOutputStream != null) {
					dataOutputStream.close();
				}

				if (connection != null) {
					connection.disconnect();
				}
			} catch (Exception e) {
				log.error("본인확인 서버 통신 실패: " + e.getMessage());
			}
		}
		return null;
	}
}

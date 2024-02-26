package mallang_trip.backend.global.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException {

        String exception = (String) request.getAttribute("exception");

        if (exception == null) {
            setResponseJson(response, "인증되지 않은 사용자입니다.", 401);
        } else if (exception.equals("10003")) {
            setResponseJson(response, "JWT가 만료되었습니다.", 10003);
        } else {
            setResponseJson(response, "JWT에 오류가 있습니다.", 10002);
        }

    }

    private void setResponseJson(HttpServletResponse response, String message, int code)
        throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        JSONObject responseJson = new JSONObject();
        try {
            responseJson.put("message", message);
            responseJson.put("statusCode", code);
        } catch (JSONException e) {
            log.error("JSON 생성 에러 {}", e);
        }
        response.getWriter().print(responseJson);
    }
}

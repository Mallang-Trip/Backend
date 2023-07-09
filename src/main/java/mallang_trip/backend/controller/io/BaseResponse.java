package mallang_trip.backend.controller.io;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"code", "message", "payload"})
public class BaseResponse<T> {
    private final String message;
    private final int statusCode;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T payload;

    // 요청에 성공한 경우
    public BaseResponse(T payload) {
        this.statusCode = BaseResponseStatus.Success.getStatusCode();
        this.message = BaseResponseStatus.Success.getMessage();
        this.payload = payload;
    }

    // 요청에 실패한 경우
    public BaseResponse(BaseResponseStatus status) {
        this.statusCode = status.getStatusCode();
        this.message = status.getMessage();
    }

    // validation 오류 발생한 경우
    public BaseResponse(MethodArgumentNotValidException e) {
        if(e.getFieldError() == null || e.getFieldError().getDefaultMessage() == null) {
            this.statusCode = BaseResponseStatus.Bad_Request.getStatusCode();
            this.message = BaseResponseStatus.Bad_Request.getMessage();
        } else {
            this.statusCode = BaseResponseStatus.valueOf(e.getFieldError().getDefaultMessage()).getStatusCode();
            this.message = BaseResponseStatus.valueOf(e.getFieldError().getDefaultMessage()).getMessage();
        }

    }

    @Builder
    public BaseResponse(String message, int code) {
        this.message = message;
        this.statusCode = code;
    }
}

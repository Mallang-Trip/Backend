package mallang_trip.backend.controller.io;

import io.jsonwebtoken.ExpiredJwtException;
import javax.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionController {

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(BaseException.class)
    @ResponseBody
    public BaseResponse<?> invalidRequestHandler(BaseException e) {
        return new BaseResponse<>(e.getStatus());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ExceptionResponse invalidRequest(MethodArgumentNotValidException e) {
        return ExceptionResponse.builder().message("잘못된 요청입니다.").build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public ExceptionResponse invalidRequest(MissingServletRequestParameterException e) {
        return ExceptionResponse.builder().message("잘못된 요청입니다.").build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    @ResponseBody
    public ExceptionResponse invalidRequest(InvalidDataAccessApiUsageException e) {
        return ExceptionResponse.builder().message("잘못된 요청입니다.").build();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseBody
    public ExceptionResponse invalidRequest(DataIntegrityViolationException e) {
        return ExceptionResponse.builder().message("데이터 제약조건 위반: " + e.getMessage()).build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public ExceptionResponse invalidRequest(ConstraintViolationException e) {
        return ExceptionResponse.builder().message("요청 형식이 올바르지 않습니다.").build();
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseBody
    public ExceptionResponse invalidRequest(ExpiredJwtException e) {
        return ExceptionResponse.builder().message("만료된 JWT 토큰입니다.").build();
    }
}

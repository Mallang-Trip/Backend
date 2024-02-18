package mallang_trip.backend.domains.global.io;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class BaseException extends RuntimeException {
    private BaseResponseStatus status;

    public BaseException(BaseResponseStatus status) {
        this.status = status;
        log.info("exception type : " + status);
    }

}

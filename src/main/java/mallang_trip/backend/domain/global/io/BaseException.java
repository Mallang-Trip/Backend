package mallang_trip.backend.domain.global.io;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class BaseException extends RuntimeException {
    private ResponseStatus status;

    public BaseException(ResponseStatus status) {
        this.status = status;
        log.info("exception type : " + status);
    }

}
